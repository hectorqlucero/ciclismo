(ns ciclismo.cc.routes
  (:require [ciclismo.cc.routes.registro :as registro]
            [ciclismo.cc.routes.fotos :as fotos]
            [ciclismo.cc.routes.reportes :as reportes]
            [ciclismo.cc.routes.padron :as padron]
            [compojure.core :refer [defroutes GET POST]]))

(defroutes cc-routes
  ;;Start registro
  (GET "/registro" [] (registro/registro))
  (POST "/registro/processar" request [] (registro/processar request))
  (POST "/registro/save" request [] (registro/registro-save request))
  (GET "/registro/fotos" request [] (fotos/slide request))
  (GET "/registro/rleaders" [] (reportes/leaders))
  (GET "/registro/rtotal" [] (reportes/rtotal))
  (GET "/registro/carreras" [] (reportes/carreras))
  ;;End registro
  ;;Start padron
  (GET "/padron" [] (padron/main))
  (GET "/padron/login" request [] (padron/buscar request))
  (POST "/padron/login" request [] (padron/buscar! request))
  (GET "/padron/registrar" request [] (padron/registrar request))
  (POST "/padron/registrar" request [] (padron/registrar! request))
  ;;End padron
  )
