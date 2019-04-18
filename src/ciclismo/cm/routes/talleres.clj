(ns ciclismo.cm.routes.talleres
  (:require [ciclismo.models.crud :refer [db Query]]
            [selmer.parser :refer [render-file]]))

(def sql
  "SELECT * FROM taller ORDER BY nombre")

(defn talleres-reporte []
  (let [rows (Query db sql)]
    (render-file "cm/talleres.html" {:title "Talleres de bicicletas"
                                     :rows  rows})))
