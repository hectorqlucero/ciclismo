(ns ciclismo.cm.admin.reportes.ptotal
  (:require [ciclismo.cm.routes.sql :refer [totales-sql]]
            [ciclismo.models.crud :refer [db Query]]
            [selmer.parser :refer [render-file]]))

(defn totales []
  (render-file "cm/admin/reportes/ptotal.html"
               {:title "Puntuación Total Serial"
                :rows (Query db totales-sql)}))

