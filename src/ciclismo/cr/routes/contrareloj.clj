(ns ciclismo.cr.routes.contrareloj
  (:require [selmer.parser :refer [render-file]]))

(defn contra-reloj [_]
  (render-file "cr/contrareloj.html" {:title nil}))
