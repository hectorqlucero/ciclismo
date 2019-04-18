(ns ciclismo.cr.routes.reportes.resultados
  (:require [ciclismo.cr.routes.sql :refer [results-sql]]
            [ciclismo.models.crud :refer [db Query]]
            [ciclismo.models.util :refer [create-categorias calculate-speed]]
            [selmer.parser :refer [render-file]]))

(defn resultados [request]
  (render-file "cr/pre_resultados.html" {:title "Ver Resultados Todas las Categorias"}))

(defn process [{params :params}]
  (let [carreras_id   (:carreras_id params)
        carreras_desc (:descripcion (first (Query db ["SELECT descripcion FROM carreras WHERE id = ?" carreras_id])))
        rows          (Query db [results-sql carreras_id])
        crows         (create-categorias rows)
        rows          (map #(assoc % :speed (str (calculate-speed (:distancia %) (:seconds %)) " km/h")) rows)]
    (render-file "cr/resultados.html" {:title (str "Resultados: " carreras_desc  " (Distancia: " (:distancia (first rows)) " Metros)")
                                                :crows crows
                                                :rows  rows})))
