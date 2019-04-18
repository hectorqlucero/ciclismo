(ns ciclismo.cr.routes
  (:require [ciclismo.cr.routes.contrareloj :as contrareloj]
            [ciclismo.cr.routes.crear-carrera :as crear-carrera]
            [ciclismo.cr.routes.tiempo :as tiempo]
            [ciclismo.cr.routes.reportes.resultados :as resultados]
            [ciclismo.cr.routes.reportes.cresultados :as cresultados]
            [ciclismo.cr.routes.reportes.oresultados :as oresultados]
            [compojure.core :refer [defroutes GET POST]]))

(defroutes cr-routes
  (GET "/contrareloj" request [] (contrareloj/contra-reloj request))
  (GET "/contrareloj/crear/carrera" request [] (crear-carrera/get-carrera request))
  (POST "/contrareloj/crear/carrera" request [] (crear-carrera/process-carrera request))
  (GET "/contrareloj/tomar/tiempo" request [] (tiempo/get-timer request))
  (POST "/contrareloj/tomar/tiempo" request [] (tiempo/display-timer request))
  (GET "/contrareloj/empezar/tiempo/:id" [id] (tiempo/empezar-time id))
  (GET "/contrareloj/terminar/tiempo/:id" [id] (tiempo/terminar-time id))
  (GET "/contrareloj/resultados" request [] (resultados/resultados request))
  (POST "/contrareloj/resultados" request [] (resultados/process request))
  (GET "/contrareloj/cresultados" request [] (cresultados/resultados request))
  (POST "/contrareloj/cresultados" request [] (cresultados/process request))
  (GET "/contrareloj/oresultados" request [] (oresultados/resultados request))
  (POST "/contrareloj/oresultados" request [] (oresultados/process request)))
