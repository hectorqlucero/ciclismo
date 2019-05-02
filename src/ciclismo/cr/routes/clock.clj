(ns ciclismo.cr.routes.clock
  (:require [selmer.parser :refer [render-file]]))

(defn get-clock [request]
  (render-file "cr/clock.html" {:title "Reloj Sincronizado"}))
