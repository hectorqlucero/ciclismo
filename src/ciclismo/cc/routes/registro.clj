(ns ciclismo.cc.routes.registro
  (:require [cheshire.core :refer [generate-string]]
            [ciclismo.cm.routes.sql :refer [cartas-sql totales-sql]]
            [ciclismo.models.crud :refer [db Query Save]]
            [ciclismo.models.email :refer [host send-email]]
            [ciclismo.models.util
             :refer
             [capitalize-words format-date-external get-session-id]]
            [selmer.parser :refer [render-file]]))

(def carreras_id (atom nil))

(defn carreras-row [] (first (Query db ["SELECT * FROM carreras WHERE id = ?" @carreras_id])))

(defn registro []
  (let [crow (carreras-row)]
    (render-file "cc/registro.html"
                 {:title (str (:descripcion crow))
                  :fecha (format-date-external (str (:fecha crow)))
                  :user  (or (get-session-id) "Anonimo")
                  :rows  (Query db totales-sql)
                  :banco (str (:banco carreras-row))
                  :banco_cuenta (str (:banco_cuenta carreras-row))
                  :banco_instrucciones (str (:banco_instrucciones carreras-row))
                  :organizador (str (:organizador carreras-row))})))

(defn processar [{params :params}]
  (if-not (nil? (:carreras_id params)) (reset! carreras_id (:carreras_id params)))
  (let [email        (:email params)
        categoria    (:categoria params)
        carreras-row (carreras-row)
        row          (first (Query db [cartas-sql email categoria @carreras_id]))
        item         {:email               email
                      :carreras_id         @carreras_id
                      :categoria           categoria
                      :banco               (str (:banco carreras-row))
                      :banco_cuenta        (str (:banco_cuenta carreras-row))
                      :banco_instrucciones (str (:banco_instrucciones carreras-row))
                      :organizador         (str (:organizador carreras-row))}
        result       (if (seq row) 1 0)
        row          (if (seq row) row {:email               email
                                        :carreras_id         @carreras_id
                                        :categoria           categoria
                                        :banco               (str (:banco carreras-row))
                                        :banco_cuenta        (str (:banco_cuenta carreras-row))
                                        :banco_instrucciones (str (:banco_instrucciones carreras-row))
                                        :organizador         (str (:organizador carreras-row))})]
    (render-file "cc/registrar.html" {:title  (str (:descripcion carreras-row))
                                      :user   (or (get-session-id) "Anonimo")
                                      :item   item
                                      :row    (generate-string row)
                                      :exists result})))

;; Start registro-save
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

(defn registro-save [{params :params}]
  (try
    (let [id          (:id params)
          corredor_id (:corredor_id params)
          crow        (first (Query db ["SELECT * FROM corredores WHERE id = ?" corredor_id]))
          send_email  (or (params :send_email) "F")
          categoria   (:categoria params)
          email       (clojure.string/lower-case (:email crow))
          carreras_id @carreras_id
          nombre      (capitalize-words (str (:nombre crow) " " (:apell_paterno crow) " " (:apell_materno crow)))
          telefono    (:telefono crow)
          equipo      (clojure.string/upper-case (:equipo params))
          edad        nil
          email-body  (get-email-body carreras_id nombre email edad telefono equipo categoria)
          postvars    {:id               id
                       :categoria        categoria
                       :no_participacion (:no_participacion params)
                       :equipo           equipo
                       :tutor            (capitalize-words (:tutor params))
                       :carreras_id      carreras_id
                       :corredores_id    corredor_id}
          result      (Save db :cartas postvars ["id = ?" id])]
      (if (seq result)
        (do
          (if (= send_email "T") (send-email host email-body))
          (generate-string {:success "Correctamente Processado!<br>Revise los datos con cuidado antes de cerrar esta pagina. En el encabezado aparece la informacion para realizar su pago. Muchas Gracias!"}))
        (generate-string {:error "No se pudo processar!"})))
    (catch Exception e (.getMessage e))))
;; End registro-save
