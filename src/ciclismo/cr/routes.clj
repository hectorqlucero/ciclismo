(ns ciclismo.cr.routes
  (:require [ciclismo.cr.routes.contrareloj :as contrareloj]
            [ciclismo.cr.routes.crear-carrera :as crear-carrera]
            [ciclismo.cr.routes.crear-tiempos :as crear-tiempos]
            [ciclismo.cr.routes.tiempo :as tiempo]
            [ciclismo.cr.routes.reportes.resultados :as resultados]
            [ciclismo.cr.routes.reportes.cresultados :as cresultados]
            [ciclismo.cr.routes.reportes.oresultados :as oresultados]
            [compojure.core :refer [defroutes GET POST]]))

(defroutes cr-routes
  (GET "/contrareloj" request [] (contrareloj/contra-reloj request))
  (GET "/contrareloj/crear/carrera" request [] (crear-carrera/get-carrera request))
  (POST "/contrareloj/crear/carrera" request [] (crear-carrera/process-carrera request))
  (GET "/contrareloj/crear/tiempos" request [] (crear-tiempos/get-tiempos request))
  (POST "/contrareloj/crear/tiempos" request [] (crear-tiempos/process-tiempos request))
  (GET "/contrareloj/tiempos/reporte/:carreras_id/:min_number/:max_number" [carreras_id min_number max_number] (crear-tiempos/print-processar carreras_id min_number max_number))
  (GET "/contrareloj/tiempos/error/:min_number/:max_number/:max_count" [min_number max_number max_count] (crear-tiempos/process-error min_number max_number max_count))
  (GET "/contrareloj/tomar/tiempo" request [] (tiempo/get-timer request))
  (POST "/contrareloj/tomar/tiempo" request [] (tiempo/display-timer request))
  (GET "/contrareloj/empezar/tiempo/:id" [id] (tiempo/empezar-time id))
  (GET "/contrareloj/terminar/tiempo/:id" [id] (tiempo/terminar-time id))
  (POST "/contrareloj/empezar/editar" request [] (tiempo/empezar-edit request))
  (POST "/contrareloj/terminar/editar" request [] (tiempo/terminar-edit request))
  (GET "/contrareloj/resultados" request [] (resultados/resultados request))
  (POST "/contrareloj/resultados" request [] (resultados/process request))
  (GET "/contrareloj/cresultados" request [] (cresultados/resultados request))
  (POST "/contrareloj/cresultados" request [] (cresultados/process request))
  (GET "/contrareloj/oresultados" request [] (oresultados/resultados request))
  (POST "/contrareloj/oresultados" request [] (oresultados/process request)))
