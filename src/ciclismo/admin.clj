(ns ciclismo.admin
  (:require [ciclismo.cm.admin.eventos.eventos :as eventos]
            [ciclismo.cm.admin.talleres.talleres :as talleres]
            [ciclismo.cm.admin.cuadrantes.cuadrantes :as cuadrantes]
            [ciclismo.cm.admin.usuarios.usuarios :as usuarios]
            [ciclismo.cm.admin.categorias.categorias :as categorias]
            [ciclismo.cm.admin.carreras.carreras :as carreras]
            [ciclismo.cm.admin.corredores.corredores :as corredores]
            [ciclismo.cm.admin.registro.exoneracion :as exoneracion]
            [ciclismo.cm.admin.puntos.puntos :as puntos]
            [ciclismo.cm.admin.reportes.creporte :as creporte]
            [ciclismo.cm.admin.reportes.ptotal :as ptotal]
            [ciclismo.cm.admin.reportes.reconocimiento :as reconocimiento]
            [ciclismo.cm.admin.reportes.treporte :as treporte]
            [ciclismo.cm.admin.fotos.podio :as podio]
            [compojure.core :refer [defroutes GET POST]]))

(defroutes admin-routes
  ;; Start eventos
  (GET "/calendario/eventos" [] (eventos/eventos))
  (POST "/calendario/eventos/json/grid" request (eventos/grid-json request))
  (GET "/calendario/eventos/json/form/:id" [id] (eventos/form-json id))
  (POST "/calendario/eventos/save" request [] (eventos/eventos-save request))
  (POST "/calendario/eventos/delete" request [] (eventos/eventos-delete request))
  ;; End eventos
  ;; Start talleres
  (GET "/admin/taller" [] (talleres/taller))
  (POST "/admin/taller/json/grid" request [] (talleres/grid-json request))
  (GET "/admin/taller/json/form/:id" [id] (talleres/form-json id))
  (POST "/admin/taller/save" request [] (talleres/taller-save request))
  (POST "/admin/taller/delete" request [] (talleres/taller-delete request))
  ;; End talleres
  ;; Start cuadrantes
  (GET "/admin/cuadrantes" request [] (cuadrantes/cuadrantes request))
  (POST "/admin/cuadrantes/json/grid" request [] (cuadrantes/grid-json request))
  (GET "/admin/cuadrantes/json/form/:id" [id] (cuadrantes/form-json id))
  (POST "/admin/cuadrantes/save" request [] (cuadrantes/cuadrantes-save request))
  (POST "/admin/cuadrantes/delete" request [] (cuadrantes/cuadrantes-delete request))
  ;; End cusdrantes
  ;; Start usuarios
  (GET "/admin/users" request [] (usuarios/users request))
  (POST "/admin/users/json/grid" request [] (usuarios/grid-json request))
  (GET "/admin/users/json/form/:id" [id] (usuarios/form-json id))
  (POST "/admin/users/save" request [] (usuarios/users-save request))
  (POST "/admin/users/delete" request [] (usuarios/users-delete request))
  ;; End usuarios
  ;; Start categorias
  (GET "/cartas/categorias" [] (categorias/categorias))
  (POST "/cartas/categorias/json/grid" request [] (categorias/grid-json request))
  (GET "/cartas/categorias/json/form/:id" [id] (categorias/form-json id))
  (POST "/cartas/categorias/save" request [] (categorias/categorias-save request))
  ;; End categorias
  ;; Start carreras
  (GET "/admin/carreras" [] (carreras/carreras))
  (POST "/admin/carreras/json/grid" request [] (carreras/grid-json request))
  (GET "/admin/carreras/json/form/:id" [id] (carreras/form-json id))
  (POST "/admin/carreras/save" request [] (carreras/carreras-save request))
  (POST "/admin/carreras/delete" request [] (carreras/carreras-delete request))
  (GET "/admin/carreras_categorias/:carreras_id" [carreras_id] (carreras/carreras_categorias carreras_id))
  (GET "/admin/carreras_categorias_process" request [] (carreras/carreras_categorias-process request))
  ;; End carreras
  ;; Start corredores
  (GET "/admin/corredores" [] (corredores/corredores))
  (POST "/admin/corredores/json/grid" request (corredores/grid-json request))
  (GET "/admin/corredores/json/form/:id" [id] (corredores/form-json id))
  (POST "/admin/corredores/save" request [] (corredores/corredores-save request))
  (POST "/admin/corredores/delete" request [] (corredores/corredores-delete request))
  ;; End corredores
  ;; Start exoneracion
  (GET "/cartas/exoneracion" [] (exoneracion/exoneracion))
  (POST "/cartas/exoneracion" request (exoneracion/exoneracion-processar request))
  (POST "/cartas/exoneracion/json/grid" request (exoneracion/grid-json request))
  (GET "/cartas/exoneracion/json/form/:id" [id] (exoneracion/form-json id))
  (POST "/cartas/exoneracion/save" request [] (exoneracion/exoneracion-save request))
  (POST "/cartas/exoneracion/delete" request [] (exoneracion/exoneracion-delete request))
  (GET "/cartas/exoneracion/pdf/:id" [id] (exoneracion/exoneracion-pdf id))
  (GET "/cartas/blank/pdf" request [] (exoneracion/exoneracion-blank-pdf request))
  ;; End exoneracion
  ;; Start puntos
  (GET "/cartas/puntos" [] (puntos/puntos))
  (POST "/cartas/puntos" request [] (puntos/process-puntos request))
  (POST "/cartas/puntos/json/grid" request [] (puntos/grid-json request))
  (GET "/cartas/puntos/json/form/:id" [id] (puntos/form-json id))
  (POST "/cartas/puntos/save" request [] (puntos/puntos-save request))
  ;; End putons
  ;; Start creporte
  (GET "/cartas/creporte" request [] (creporte/creporte request))
  (POST "/cartas/creporte/processar" request [] (creporte/creporte-processar request))
  ;; End creporte
  (GET "/cartas/ptotal" [] (ptotal/totales))
  (GET "/cartas/treporte" [] (treporte/totales-report))
  ;; Start reconociciento
  (GET "/cartas/reconocimiento" [] (reconocimiento/imprimir-r))
  (GET "/cartas/reconocimiento/pdf/:nombre/:categoria" [nombre categoria]
    (reconocimiento/reconocimiento-pdf nombre categoria))
  ;; End reconocimiento
  ;; Start fotos
  (GET "/podio" req [] (podio/main req))
  (POST "/podio" req [] (podio/upload-picture req))
  ;; ENd fotos
  )
