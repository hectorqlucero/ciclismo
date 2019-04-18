(ns ciclismo.routes
  (:require [compojure.core :refer [defroutes]]
            [ciclismo.table_ref :refer [table_ref-routes]]
            [ciclismo.cm.routes :refer [cm-routes]]
            [ciclismo.cc.routes :refer [cc-routes]]
            [ciclismo.cr.routes :refer [cr-routes]]))

(defroutes ciclismo-routes
  table_ref-routes
  cm-routes
  cc-routes
  cr-routes)
