(ns ciclismo.cc.routes
  (:require [ciclismo.cc.routes.registro :as registro]
            [ciclismo.cc.routes.fotos :as fotos]
            [ciclismo.cc.routes.reportes :as reportes]
            [compojure.core :refer [defroutes GET POST]]))

(defroutes cc-routes
  (GET "/registro" [] (registro/registro))
  (POST "/registro/processar" request [] (registro/processar request))
  (POST "/registro/save" request [] (registro/registro-save request))
  (GET "/registro/fotos" request [] (fotos/slide request))
  (GET "/registro/rleaders" [] (reportes/leaders))
  (GET "/registro/rtotal" [] (reportes/rtotal))
  (GET "/registro/carreras" [] (reportes/carreras)))
