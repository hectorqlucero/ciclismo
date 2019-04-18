(ns ciclismo.cr.routes.reportes.oresultados
  (:require [ciclismo.cr.routes.sql :refer [oresults-sql]]
            [ciclismo.models.crud :refer [db Query]]
            [ciclismo.models.util :refer [calculate-speed]]
            [selmer.parser :refer [render-file]]))

(defn resultados [request]
  (render-file "cr/opre_resultados.html" {:title "Ver Resultados Overall"}))

(defn process [{params :params}]
  (let [carreras_id   (:carreras_id params)
        carreras_desc (:descripcion (first (Query db ["SELECT descripcion FROM carreras WHERE id = ?" carreras_id])))
        rows          (Query db [oresults-sql carreras_id])
        rows          (map #(assoc % :speed (str (calculate-speed (:distancia %) (:seconds %)) " km/h")) rows)]
    (render-file "cr/c_resultados.html" {:title (str "Resultados/Overall: " carreras_desc  " (Distancia: " (:distancia (first rows)) " Metros)")
                                                  :rows  rows})))
