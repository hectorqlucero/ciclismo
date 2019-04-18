(ns ciclismo.cm.admin.reportes.reconocimiento
  (:require [ciclismo.cm.routes.sql :refer [ptotales-sql]]
            [ciclismo.models.crud :refer [db Query]]
            [pdfkit-clj.core :refer :all]
            [selmer.parser :refer [render-file]]))

(defn imprimir-r []
  (render-file "cm/admin/reportes/reconocimiento.html"
               {:title "Imprimir Reconocimientos"
                :rows  (Query db ptotales-sql)}))

;; Start reconocimiento pdf
(defn pdf-report [nombre categoria]
  (as-stream (gen-pdf (render-file "cm/admin/reportes/reporte.html"
                                   {:nombre    nombre
                                    :categoria categoria})
                      :page-size "letter"
                      :orientation "landscape")))

(defn reconocimiento-pdf [nombre categoria]
  (let [file-name (str nombre "_reconocimento.pdf")]
    {:headers {"Content-type"        "application/pdf"
               "Content-disposition" (str "attachment;filename=" file-name)}
     :body    (pdf-report nombre categoria)}))
;; End testing reconocimiento pdf
