(ns ciclismo.cr.routes.crear-tiempos
  (:require [ciclismo.models.crud :refer [db Query Save Update]]
            [clj-time.core :as t]
            [ciclismo.models.util
             :refer
             [current_day current_month current_year parse-int]]
            [clj-pdf.core :refer :all]
            [clojure.java.io :refer [output-stream]]
            [ring.util.io :refer [piped-input-stream]]
            [ring.util.response :refer [redirect]]
            [selmer.parser :refer [render-file]]
            [clj-time.core :as t]))

(def crear_carrera-sql
  "SELECT
   id,
   carreras_id,
   categoria
   FROM cartas
   WHERE carreras_id = ?
   ORDER BY no_participacion ")

(def count-sql
  "SELECT
  MAX(id) as max_count
  FROM contrareloj
  WHERE
  carreras_id = ?")

(def contrareloj-sql
  "SELECT
  contrareloj.id,
  contrareloj.cartas_id,
  cartas.no_participacion
  FROM
  cartas
  JOIN contrareloj on contrareloj.cartas_id = cartas.id
  WHERE
  cartas.carreras_id = ?
  AND cartas.no_participacion = ?")

(def report-sql
  "SELECT
  cartas.no_participacion as numero,
  cartas.nombre as nombre,
  categorias.descripcion as categoria,
  TIME_FORMAT(contrareloj.empezar,'%H:%i:%s') as empezar
  FROM cartas
  JOIN contrareloj on contrareloj.cartas_id = cartas.id
  JOIN categorias on categorias.id = cartas.categoria
  WHERE
  cartas.carreras_id = ?
  AND contrareloj.id BETWEEN ? AND ?")

(defn process-carrera [carreras_id]
  (let [crow (first (Query db ["SELECT * FROM carreras WHERE id = ?" carreras_id]))
        rows (Query db [crear_carrera-sql carreras_id])]
    (doseq [row rows]
      (let [cartas_id     (str (:id row))
            carreras_id   (str (:carreras_id row))
            categorias_id (str (:categoria row))
            id            (:id
                           (first
                            (Query db
                                   ["SELECT id from contrareloj
                            WHERE cartas_id = ? AND carreras_id = ? AND categorias_id = ?" cartas_id carreras_id categorias_id])))
            postvars      {:id            (str id)
                           :cartas_id     cartas_id
                           :carreras_id   carreras_id
                           :categorias_id categorias_id}
            result        (Save db :contrareloj postvars ["id = ?" id])]))))

(defn get-tiempos [request]
  (render-file "cr/pre_tiempos.html" {:title "Crear Reporte Tiempos"}))

(defn get-contrareloj-id [carreras_id no_participacion]
  (:id (first (Query db [contrareloj-sql carreras_id no_participacion]))))

(defn process-records [carreras_id min_number max_number start_time intervalos]
  (let [shour        (parse-int (first (clojure.string/split start_time #":")))
        sminute      (parse-int (second (clojure.string/split start_time #":")))
        year        (parse-int (current_year))
        month       (parse-int (current_month))
        day         (parse-int (current_day))
        my-datetime (t/date-time year month day shour sminute)
        data        (range min_number (inc max_number))
        cnt         (atom 0)
        intervalo   (parse-int intervalos)]
    (doseq [n data]
      (let [m-time (t/plus my-datetime (t/minutes @cnt))
            hour   (t/hour m-time)
            minute (t/minute m-time)]
        (Update db :contrareloj {:empezar (str hour ":" minute)} ["id = ?" n])
        (swap! cnt (partial + intervalo))))))

;; Start report
(def reporte-template
  (template
   (list
    [:cell {:align :justified} (str $numero)]
    [:cell {:align :left} (str $nombre)]
    [:cell {:align :left} (str $categoria)]
    [:cell {:align :left} (str $empezar)]
    [:cell {:align :left} "             "]
    [:cell {:align :left} "             "])))

(defn get-rows [carreras_id min_number max_number]
  (Query db [report-sql (parse-int carreras_id) (parse-int min_number) (parse-int max_number)]))

(defn execute-creporte [carrera_id min_number max_number]
  (let [h1   (:descripcion (first (Query db ["SELECT descripcion FROM carreras WHERE id = ?" carrera_id])))
        h2   "CARRERA CONTRARELOJ POR NUMERO DE PARTICIPACION"
        rows (get-rows carrera_id min_number max_number)]
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
            :widths      [10 25 25 10 10 10]
            :header      [{:backdrop-color [233 233 233]}
                          [:paragraph {:align :justified} "NUMERO"]
                          [:paragraph {:align :left} "NOMBRE"]
                          [:paragraph {:align :left} "CATEGORIA"]
                          [:paragraph {:align :left} "EMPEZAR"]
                          [:paragraph {:align :center} "TERMINAR"]
                          [:paragraph {:align :left} "LUGAR"]]}]
          (reporte-template rows))]

        output-stream)))))

(defn print-processar [carreras_id min_number max_number]
  (let [file-name (str "Reporte_tiempos_" min_number "_" max_number ".pdf")]
    {:headers {"Content-type"        "application/pdf"
               "Content-disposition" (str "attachment;filename=" file-name)}
     :body    (execute-creporte carreras_id min_number max_number)}))
;; End report

(defn process-tiempos [{params :params}]
  (process-carrera (:carreras_id params)) ;; Make sure there is something to process
  (let [carreras_id (:carreras_id params)
        min_number  (parse-int (:min_number params))
        max_number  (parse-int (:max_number params))
        start_time  (:start_time params)
        intervalos  (parse-int (:intervalos params))
        max_count   (parse-int (:max_count (first (Query db [count-sql carreras_id]))))]
    (if (>= max_count max_number)
      (do
        (process-records carreras_id min_number max_number start_time intervalos)
        (redirect (str "/contrareloj/tiempos/reporte/" carreras_id "/" min_number "/" max_number)))
      (redirect (str "/contrareloj/tiempos/error/" min_number "/" max_number "/" max_count)))))

(defn process-error [min_number max_number max_count]
  (render-file "/cr/error.html" {:title      "Error!"
                                 :min_number min_number
                                 :max_number max_number
                                 :max_count max_count}))
