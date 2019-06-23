(ns ciclismo.cm.routes.cuadrantes
  (:require [ciclismo.models.crud :refer [db Query]]
            [ciclismo.models.util :refer [capitalize-words]]
            [selmer.parser :refer [render-file]]))

(def sql
  "SELECT 
   id,
   name,
   leader,
   leader_phone,
   leader_cell,
   leader_email,
   notes,
   CASE WHEN status = 'T' THEN 'Activo' ELSE 'Inactivo' END AS status
   FROM cuadrantes ORDER BY name")

(defn cuadrantes-reporte []
  (let [rows (Query db sql)
        rows (map #(assoc % :leader (capitalize-words (:leader %))) rows)]
    (render-file "cm/cuadrantes.html" {:title "Talleres de bicicletas"
                                       :rows  rows})))
