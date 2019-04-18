(ns ciclismo.cr.routes.reportes.cresultados
  (:require [ciclismo.cr.routes.sql :refer [cresults-sql]]
            [ciclismo.models.crud :refer [db Query]]
            [ciclismo.models.util :refer [calculate-speed]]
            [selmer.parser :refer [render-file]]))

(defn resultados [request]
  (render-file "cr/cpre_resultados.html" {:title "Ver Resultados Por Categoria"}))

(defn process [{params :params}]
  (let [carreras_id   (:carreras_id params)
        carreras_desc (:descripcion (first (Query db ["SELECT descripcion FROM carreras WHERE id = ?" carreras_id])))
        categorias_id (:categoria params)
        rows          (Query db [cresults-sql carreras_id categorias_id])
        rows          (map #(assoc % :speed (str (calculate-speed (:distancia %) (:seconds %)) " km/h")) rows)]
    (render-file "cr/c_resultados.html" {:title (str "Resultados/Categoria: " carreras_desc  " (Distancia: " (:distancia (first rows)) " Metros)")
                                         :rows  rows})))
