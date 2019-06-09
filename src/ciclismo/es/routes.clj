(ns ciclismo.es.routes
  (:require [compojure.core :refer [defroutes GET POST]]
            [ciclismo.es.routes.registro :as registro]))

(defroutes es-routes
  (GET "/escuela" request [] (registro/buscar request))
  (POST "/escuela" request [] (registro/buscar! request))
  (GET "/escuela/registrar" request [] (registro/registrar request))
  (POST "/escuela/registrar" request [] (registro/registrar! request))
  (GET "/escuela/matricula/:matricula" [matricula] (registro/matricula matricula))
  (POST "/escuela/matricula" request [] (registro/matricula! request))
  (GET "/escuela/rpaswd" request [] (registro/reset-password request))
  (POST "/escuela/rpaswd" request [] (registro/reset-password! request))
  (GET "/escuela/reset_password/:token" [token] (registro/reset-jwt token))
  (POST "/escuela/reset_password" request [] (registro/reset-jwt! request))
  (GET "/escuela/logoff" request [] (registro/logoff request))
  (GET "/escuela/r_alumnos" request [] (registro/r-alumnos request)))
