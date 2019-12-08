(ns ciclismo.cm.admin.registro.exoneracion
  (:require [cheshire.core :refer [generate-string]]
            [ciclismo.cm.routes.sql
             :refer
             [build-blank-p2
              build-body-p1
              build-body-p2
              build-body-p3
              build-body-p4
              pdf-sql]]
            [ciclismo.models.crud :refer [db Delete Query Save]]
            [ciclismo.models.email :refer [host send-email]]
            [ciclismo.models.grid :refer :all]
            [ciclismo.models.util
             :refer
             [capitalize-words get-session-id parse-int]]
            [clj-pdf.core :refer [pdf]]
            [clojure.java.io :refer [output-stream]]
            [ring.util.io :refer [piped-input-stream]]
            [selmer.parser :refer [render-file]]))

(def carreras_id (atom nil))

(defn carreras-row [] (first (Query db ["SELECT * FROM carreras WHERE id = ?" @carreras_id])))

(defn exoneracion []
  (render-file "cm/admin/registro/pre_index.html" {:title "Cartas - Exoneracion"
                                                   :user        (or (get-session-id) "Anonimo")}))

(defn exoneracion-processar [{params :params}]
  (if-not (nil? (:carrera_id params)) (reset! carreras_id (:carrera_id params)))
  (render-file "cm/admin/registro/index.html" {:title (str "Registro de Corredores: " (:descripcion (first (Query db ["SELECT descripcion FROM carreras WHERE id = ?" (:carrera_id params)]))))
                                               :carrera_id  @carreras_id}))
;;start exoneracion grid
(def search-columns
  ["cartas.id"
   "cartas.no_participacion"
   "categorias.descripcion"
   "corredores.nombre"
   "corredores.apell_paterno"
   "corredores.apell_materno"
   "corredores.telefono"
   "corredores.email"])

(def aliases-columns
  ["cartas.id"
   "CONCAT(DATE_FORMAT(DATE(cartas.creado),'%m/%d/%Y'),' ',TIME_FORMAT(TIME(cartas.creado),'%h:%i %p')) as creado"
   "cartas.no_participacion"
   "categorias.descripcion as categoria"
   "corredores.nombre"
   "corredores.apell_paterno"
   "corredores.apell_materno"
   "corredores.telefono"
   "corredores.email"])

(defn grid-json
  [{params :params}]
  (if-not (nil? (:carrera_id params)) (reset! carreras_id (:carrera_id params)))
  (try
    (let [table    "cartas"
          scolumns (convert-search-columns search-columns)
          aliases  aliases-columns
          join     "JOIN categorias on categorias.id = cartas.categoria
                    JOIN corredores on corredores.id = cartas.corredores_id"
          search   (grid-search (:search params nil) scolumns)
          search   (grid-search-extra search (str "cartas.carreras_id = " @carreras_id))
          order    (grid-sort (:sort params nil) (:order params nil))
          offset   (grid-offset (parse-int (:rows params)) (parse-int (:page params)))
          sql      (grid-sql table aliases join search order offset)
          rows     (grid-rows table aliases join search order offset)]
      (generate-string rows))
    (catch Exception e (.getMessage e))))
;;end exoneracion grid

;;start exoneracion form
(def form-sql
  "SELECT
   id,
   no_participacion,
   corredores_id,
   categoria,
   equipo,
   tutor
   FROM cartas
   WHERE id = ?")

(defn form-json
  [id]
  (try
    (let [row (Query db [form-sql id])]
      (generate-string (first row)))
    (catch Exception e (.getMessage e))))

;;end exoneracion form

;; Start exoneracion save
(defn get-carreras-desc [carreras_id]
  (:descripcion (first (Query db ["SELECT descripcion FROM carreras WHERE id = ?" carreras_id]))))

(defn get-categorias-desc [c]
  (:descripcion (first (Query db ["SELECT descripcion FROM categorias WHERE id = ?" c]))))

(defn get-email-body [carreras_id nombre email edad telefono equipo categoria]
  {:from    "hectorqlucero@gmail.com"
   :to      "marcopescador@hotmail.com"
   :cc      "hectorqlucero@gmail.com"
   :subject (str "Nuevo Registro para la carrera: " (get-carreras-desc carreras_id))
   :body    [{:type    "text/html;charset=utf-8"
              :content (str "Nuevo registro: <br>"
                            "<strong>Nombre:</strong> " nombre "<br>"
                            "<strong>Edad:</strong> " edad "<br>"
                            "<strong>Email:</strong> " email "<br>"
                            "<strong>Tel:</strong> " telefono "<br>"
                            "<strong>Equipo:</strong> " equipo "<br>"
                            "<strong>Categoria:</strong> " (get-categorias-desc categoria))}]})

(defn get-corredor-details [id]
  (let [row (first (Query db ["SELECT * FROM corredores WHERE id = ?" id]))]
    row))

(defn exoneracion-save
  [{params :params}]
  (try
    (let [id          (:id params)
          send_email  (or (params :send_email) "F")
          corredores_id (:corredores_id params)
          crow        (get-corredor-details corredores_id)
          categoria   (:categoria params)
          email       (clojure.string/lower-case (:email crow))
          carreras_id @carreras_id
          nombre      (capitalize-words (:nombre crow))
          telefono    (:telefono crow)
          equipo      (clojure.string/upper-case (:equipo params))
          edad        nil
          email-body  (get-email-body carreras_id nombre email edad telefono equipo categoria)
          postvars    {:id               id
                       :no_participacion (:no_participacion params)
                       :corredores_id    corredores_id 
                       :categoria        categoria
                       :equipo           equipo
                       :tutor            (capitalize-words (:tutor params))
                       :carreras_id      carreras_id}
          result      (Save db :cartas postvars ["id = ?" id])]
      (if (seq result)
        (do
          (if (= send_email "T") (send-email host email-body))
          (generate-string {:success "Correctamente Processado!"}))
        (generate-string {:error "No se pudo processar!"})))
    (catch Exception e (.getMessage e))))
;; End exoneracion save

(defn exoneracion-delete
  [{params :params}]
  (try
    (let [id     (:id params nil)
          result (if-not (nil? id) (Delete db :cartas ["id = ?" id]))]
      (if (seq result)
        (generate-string {:success "Removido appropiadamente!"})
        (generate-string {:error "No se pudo remover!"})))
    (catch Exception e (.getMessage e))))

;; Start exoneracion pdf
(defn build-body [row]
  [:table
   {:cell-border true
    :style       :normal
    :align       :center
    :width       90
    :size        9
    :border      true}
   [[:cell {:colspan 3 :align :center :style :bold} "PARTICIPANTE"]]
   [[:cell {:style :bold :colspan 2} "Categoria: " [:chunk {:style :italic} (str (:categoria row))]] [:cell {:style :bold} (str "Tipo de Bicicleta:")]]
   [[:cell {:style :bold} "Nombre completo: " [:chunk {:style :italic} (str (:nombre row))]] [:cell {:style :bold} "Equipo: " [:chunk {:style :italic} (str (:equipo row))]] [:cell {:style :bold} "Numero: " [:chunk {:style :italic} (str (:no_participacion row))]]]
   [[:cell {:style :bold} "Telefono: " [:chunk {:style :italic} (str (:telefono row))]] [:cell {:style :bold :colspan 2} "Email: " [:chunk {:style :italic} (str (:email row))]]]
   [[:cell {:style :bold :colspan 3} (str "Nombre del padre o tutor (En su caso): ") [:chunk {:style :italic} (str (:tutor row))]]]
   [[:cell {:style :bold :colspan 3} (str "Firma del participante y/o tutor: ")]]])

(defn execute-report [id]
  (let [h1   "CARTA DE EXONERACION"
        crow (carreras-row)
        row  (first (Query db [pdf-sql id]))]
    (piped-input-stream
     (fn [output-stream]
       (pdf
        [{:title         h1
          :references    {:logo (or [:image {:align :center :scale 9.5} "uploads/logo.jpg"] nil)}
          :header        {:x 20
                          :y 820
                          :table
                          [:pdf-table
                           {:border           false
                            :width-percent    100
                            :horizontal-align :center}
                           [100]
                           [[:pdf-cell [:reference :logo]]]]}
          :footer        "page" :left-margin 10
          :right-margin  10
          :top-margin    40
          :bottom-margin 25
          :size          :a4
          :font          {:family :helvetica :size 9}
          :align         :center
          :pages         true}
         [:spacer]
         [:spacer]
         [:paragraph {:align :justified :indent-left 28 :indent-right 28} (build-body-p1 crow)]
         [:spacer]
         [:paragraph {:align :justified :indent-left 28 :indent-right 28} (build-body-p2 row crow)]
         [:spacer]
         [:paragraph {:align :justified :indent-left 28 :indent-right 28} (build-body-p3 crow)]
         [:spacer]
         [:paragraph {:align :justified :indent-left 28 :indent-right 28} (build-body-p4 crow)]
         [:spacer]
         (build-body row)]
        output-stream)))))

(defn exoneracion-pdf [id]
  (let [file-name (str "exoneracion_" id ".pdf")]
    {:headers {"Content-type"        "application/pdf"
               "Content-disposition" (str "attachment;filename=" file-name)}
     :body    (execute-report id)}))
;; End exoneracion pdf

;; Start blank pdf
(defn execute-blank-report []
  (let [h1   "CARTA DE EXONERACION"
        row  {:categoria        nil
              :nombre           nil
              :no_participacion nil
              :telefono         nil
              :email            nil}
        crow (carreras-row)]
    (piped-input-stream
     (fn [output-stream]
       (pdf
        [{:title         h1
          :references    {:logo (or [:image {:align :center :scale 9.5} "uploads/logo.jpg"] nil)}
          :header        {:x 20
                          :y 820
                          :table
                          [:pdf-table
                           {:border           false
                            :width-percent    100
                            :horizontal-align :center}
                           [100]
                           [[:pdf-cell [:reference :logo]]]]}
          :footer        "page" :left-margin 10
          :right-margin  10
          :top-margin    40
          :bottom-margin 25
          :size          :a4
          :font          {:family :helvetica :size 9}
          :align         :center
          :pages         true}
         [:spacer]
         [:spacer]
         [:paragraph {:align :justified :indent-left 28 :indent-right 28} (build-body-p1 crow)]
         [:spacer]
         [:paragraph {:align :justified :indent-left 28 :indent-right 28} (build-blank-p2 crow)]
         [:spacer]
         [:paragraph {:align :justified :indent-left 28 :indent-right 28} (build-body-p3 crow)]
         [:spacer]
         [:paragraph {:align :justified :indent-left 28 :indent-right 28} (build-body-p4 crow)]
         [:spacer]
         (build-body row)]
        output-stream)))))

(defn exoneracion-blank-pdf [_]
  (let [file-name (str "exoneracion.pdf")]
    {:headers {"Content-type"        "application/pdf"
               "Content-disposition" (str "attachment;filename=" file-name)}
     :body    (execute-blank-report)}))
;; End blank pdf
