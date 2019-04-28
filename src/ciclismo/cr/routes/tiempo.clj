(ns ciclismo.cr.routes.tiempo
  (:require [cheshire.core :refer [generate-string]]
            [ciclismo.models.crud :refer [db Query Update]]
            [ciclismo.models.util :refer [current_time_internal]]
            [selmer.parser :refer [render-file]]))

;;Start tomar tiempo
(defn get-timer [request]
  (render-file "cr/pre_timer.html" {:title "Tomar Tiempo"}))

(def timer-sql
  "SELECT
   contrareloj.id as id,
   cartas.no_participacion as numero,
   cartas.nombre as nombre,
   categorias.descripcion as categoria,
   TIME_FORMAT(contrareloj.empezar,'%H:%i:%s') as empezar,
   TIME_FORMAT(contrareloj.terminar,'%H:%i:%s') as terminar
   FROM contrareloj
   JOIN cartas on cartas.id = contrareloj.cartas_id
   LEFT join categorias on categorias.id = contrareloj.categorias_id
   WHERE contrareloj.carreras_id = ?
   ORDER BY cartas.no_participacion,cartas.nombre,categorias.descripcion")

(defn display-timer [{params :params}]
  (let [carreras_id  (:carreras_id params)
        carrera_desc (:descripcion (first (Query db ["SELECT descripcion FROM carreras where id = ?" carreras_id])))
        rows         (Query db [timer-sql carreras_id])]
    (render-file "cr/timer.html" {:title (str "Tomar Tiempo para: " carrera_desc)
                                  :rows  rows})))
;;End tomar tiempo

(defn empezar-time [id]
  (let [current-time (current_time_internal)
        result       (Update db :contrareloj {:empezar current-time} ["id = ?" id])]
    (if (seq result)
      (generate-string {:time (str current-time)})
      (generate-string {:time "Not able to generate time!"}))))

(defn terminar-time [id]
  (let [current-time (current_time_internal)
        result       (Update db :contrareloj {:terminar current-time} ["id = ?" id])]
    (if (seq result)
      (generate-string {:time (str current-time)})
      (generate-string {:time "Not able to generate time!"}))))

(defn empezar-edit [{params :params}]
  (let [id        (:id params)
        edit-time (:tiempo params)
        result    (Update db :contrareloj {:empezar edit-time} ["id = ?" id])]
    (if (seq result)
      (generate-string {:time (str edit-time)})
      (generate-string {:time "Not able to generate time!"}))))

(defn terminar-edit [{params :params}]
  (let [id        (:id params)
        edit-time (:tiempo params)
        result    (Update db :contrareloj {:terminar edit-time} ["id = ?" id])]
    (if (seq result)
      (generate-string {:time (str edit-time)})
      (generate-string {:time "Not able to generate time!"}))))
