(ns ciclismo.cm.admin.reportes.treporte
  (:require [ciclismo.cm.routes.sql :refer [totales-categoria-sql totales-sql]]
            [ciclismo.models.crud :refer [db Query]]
            [ciclismo.models.util :refer [create-categorias]]
            [selmer.parser :refer [render-file]]))

(def total-rows
  (Query db totales-sql))

(def categoria-rows
  (create-categorias total-rows))

(defn get-limited-row [categorias_id]
  (let [rows (Query db [totales-categoria-sql categorias_id])]
    rows))

(defn totales-categoria []
  (let [rows  total-rows
        crows categoria-rows]
    (for [crow crows]
      (get-limited-row (crow :categorias_id)))))

(defn totales-report []
  (render-file "cm/admin/reportes/treporte.html"
               {:title "Totales de Lideres"
                :rows  (flatten (totales-categoria))
                :crows categoria-rows}))
