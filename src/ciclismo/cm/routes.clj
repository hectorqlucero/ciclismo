(ns ciclismo.cm.routes
  (:require [cheshire.core :refer [generate-string]]
            [ciclismo.cm.routes.home :as home]
            [ciclismo.cm.routes.eventos :as eventos]
            [ciclismo.cm.routes.rodadas :as rodadas]
            [ciclismo.cm.routes.talleres :as talleres]
            [ciclismo.cm.routes.cuadrantes :as cuadrantes]
            [ciclismo.cm.routes.entrenamiento.entrenamiento :as entrenamiento]

            [ciclismo.models.crud :refer [db Query]]
            [compojure.core :refer [defroutes GET POST]]))

(defroutes cm-routes
  (GET "/" request [] (home/slide request))
  (GET "/login" request [] (home/login request))
  (POST "/login" [username password] (home/login! username password))
  (GET "/logoff" [] (home/logoff))
  (GET "/eventos" request [] (eventos/eventos request))
  (GET "/eventos/:year/:month" [year month] (eventos/display-eventos year month))
  (GET "/eventos/print/:year/:month" [year month] (eventos/eventos-print year month))
  (GET "/main" request [] (rodadas/main request))
  (GET "/talleres/reporte" [] (talleres/talleres-reporte))
  (GET "/cuadrantes/reporte" [] (cuadrantes/cuadrantes-reporte))
  (GET "/entrenamiento/rodadas" [] (entrenamiento/rodadas))
  (POST "/entrenamiento/rodadas/json/grid" request [] (entrenamiento/grid-json request))
  (GET "/entrenamiento/rodadas/json/form/:id" [id] (entrenamiento/form-json id))
  (GET "/entrenamiento/rodadas/asistir/:id" [id] (entrenamiento/form-asistir id))
  (POST "/entrenamiento/rodadas/asistir" request [] (entrenamiento/form-asistir-save request))
  (POST "/entrenamiento/rodadas/save" request [] (entrenamiento/rodadas-save request))
  (POST "/entrenamiento/rodadas/delete" request [] (entrenamiento/rodadas-delete request)))
