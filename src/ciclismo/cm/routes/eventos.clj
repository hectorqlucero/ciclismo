(ns ciclismo.cm.routes.eventos
  (:require [ciclismo.cm.routes.sql :refer [eventos-sql rodadas-sql]]
            [ciclismo.models.crud :refer [db Query]]
            [ciclismo.models.util
             :refer
             [current_year get-month-name parse-int zpl]]
            [ciclismo.table_ref :refer [months]]
            [clj-pdf.core :refer :all]
            [clojure.java.io :refer [output-stream]]
            [ring.util.io :refer [piped-input-stream]]
            [selmer.parser :refer [render-file]]))

(defn eventos [request]
  (let [rows  (Query db rodadas-sql)
        title (str "CALENDARIO " (current_year))
        year  (str (current_year))]
    (render-file "cm/eventos.html" {:title title
                                    :rows  rows
                                    :year  year})))

;; Start display eventos
(def column-to-field
  (apply hash-map
         (mapcat
          #(vector (% :value) (% :text))
          (months))))

(defn display-eventos [year month]
  (let [rows (Query db [eventos-sql year month])
        rows (map #(assoc % :day (zpl (% :day) 2)) rows)]
    (render-file "cm/calendario.html" {:title (column-to-field (parse-int month))
                                       :year  year
                                       :month month
                                       :rows  rows})))
;; End display eventos

;;Start events print month
(def t1
  (template
   (list
    [:cell {:align :center :style :bold} (str $day)]
    [:cell {:border false :align :left :style :bold} (str $descripcion_corta)
     [:table  {:background-color [222 222 222]
               :widths [11 89]}
      [[:cell {:border false :align :left :style :bold} "LUGAR | "] [:cell {:border false :align :left} (str $punto_reunion)]]
      [[:cell {:border false :align :left :style :bold} "FECHA | "] [:cell {:border false :align :left} (str $fecha " (" $fecha_dow ")")]]
      [[:cell {:border false :align :left :style :bold} "HORA  | "] [:cell {:border false :align :left} (str $hora)]]
      [[:cell {:border false :colspan 2 :align :left :style :bold} (str $leader)]]]])))

(defn execute-report [year month]
  (let [h1 (clojure.string/upper-case (get-month-name (parse-int month)))
        rows (Query db [eventos-sql year month])]
    (piped-input-stream
     (fn [output-stream]
       (pdf [{:title h1
              :header h1}
             (into
              [:table {:border false :background-color [233 233 233]
                       :widths [10 50]}]
              (t1 rows))] output-stream)))))

(defn eventos-print [year month]
  (let [file-name (str "evento_" year "_" month ".pdf")]
    {:headers {"Content-type" "application/pdf"
               "Content-disposition" (str "attachment;filename=" file-name)}
     :body (execute-report year month)}))
;;End events print month
