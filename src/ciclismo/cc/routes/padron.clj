(ns ciclismo.cc.routes.padron
  (:require [cheshire.core :refer [generate-string]]
            [ciclismo.cm.routes.sql :refer [cartas-sql totales-sql]]
            [ciclismo.models.crud :refer [db Query Save]]
            [ciclismo.models.email :refer [host send-email]]
            [ciclismo.models.util
             :refer
             [capitalize-words format-date-external get-session-id]]
            [selmer.parser :refer [render-file]]))

(defn padron []
  (let [title "Padron de Ciclistas"]
    (render-file "cc/padron.html" {:title title})))
