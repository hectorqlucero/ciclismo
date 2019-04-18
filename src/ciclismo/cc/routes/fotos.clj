(ns ciclismo.cc.routes.fotos
  (:require [selmer.parser :refer [render-file]])
  (:import java.util.UUID))

(defn slide [request]
  (render-file "cc/fotos.html"
               {:title "Serial Ciclista Municipal De Mexicali 2019"
                :uuid (str (UUID/randomUUID))}))
