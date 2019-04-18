(ns ciclismo.cm.admin.reportes.creporte
  (:require [ciclismo.models.crud :refer [db Query]]
            [clj-pdf.core :refer :all]
            [clojure.java.io :refer [output-stream]]
            [ring.util.io :refer [piped-input-stream]]
            [selmer.parser :refer [render-file]]))

(def creporte-template
  (template
   (list
    [:cell {:align :justified} (str $descripcion)]
    [:cell {:align :left} (str $no_participacion)]
    [:cell {:align :left} (str $nombre)]
    [:cell {:align :left} (str $equipo)]
    [:cell {:align :left} "             "])))

(defn carrera-reporte-sql [carreras_id categories]
  (str "SELECT
   categorias.descripcion,
   cartas.no_participacion,
   cartas.nombre,
   cartas.equipo
   FROM cartas
   LEFT JOIN categorias on categorias.id = cartas.categoria
   WHERE
   categoria IN(" categories ")
   AND carreras_id = " carreras_id "
   ORDER BY categorias.descripcion,nombre"))

(defn execute-creporte [carrera_id categorias]
  (let [h1         (:descripcion (first (Query db ["SELECT descripcion FROM carreras WHERE id = ?" carrera_id])))
        h2         "CARRERA POR CATEGORIA(S)"
        categorias (apply str (interpose #"," (map #(str "'" % "'") categorias)))
        rows       (Query db (carrera-reporte-sql carrera_id categorias))]
    (piped-input-stream
     (fn [output-stream]
       (pdf
        [{:title         "Reporte de Carrera"
          :header        {:x 20
                          :y 830
                          :table
                          [:pdf-table
                           {:border           false
                            :width-percent    100
                            :horizontal-align :center}
                           [100]
                           [[:pdf-cell {:style :bold :size 16 :align :center} h1]]
                           [[:pdf-cell {:style :bold :size 16 :align :center} h2]]]}
          :footer        "page"
          :left-margin   10
          :right-margin  10
          :top-margin    70
          :bottom-margin 25
          :size          :a4
          :orientation   :portrait
          :font          {:family :helvetica :size 10}
          :align         :center
          :pages         true}
         (into
          [:table
           {:cell-border true
            :style       :normal
            :size        10
            :border      true
            :widths      [25 9 34 20 12]
            :header      [{:backdrop-color [233 233 233]}
                          [:paragraph {:align :justified} "CATEGORIA"]
                          [:paragraph {:align :left} "NUMERO"]
                          [:paragraph {:align :left} "NOMBRE"]
                          [:paragraph {:align :center} "EQUIPO"]
                          [:paragraph {:align :left} "LUGAR"]]}]
          (creporte-template rows))]

        output-stream)))))

(defn creporte-processar [{params :params}]
  (let [file-name  (str "Reporte de Carrera")
        carrera_id (:carrera_id params)
        categorias (:categoria_id params)]
    {:headers {"Content-type"        "application/pdf"
               "Content-disposition" (str "attachment;filename=" file-name)}}
    :body (execute-creporte carrera_id categorias)))

(defn creporte [{params :params}]
  (render-file "cm/admin/reportes/creporte.html" {:title "Reporte de Carrera"}))
