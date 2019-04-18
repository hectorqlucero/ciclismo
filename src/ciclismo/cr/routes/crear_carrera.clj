(ns ciclismo.cr.routes.crear-carrera
  (:require [ciclismo.models.crud :refer [db Query Save]]
            [ring.util.response :refer [redirect]]
            [selmer.parser :refer [render-file]]))

(defn get-carrera [request]
  (render-file "cr/pre_carrera.html" {:title "Crear Carrera Contra Reloj"}))

(def crear_carrera-sql
  "SELECT
   id,
   carreras_id,
   categoria
   FROM cartas
   WHERE carreras_id = ?
   ORDER BY no_participacion ")

(defn process-carrera [{params :params}]
  (let [carreras_id (:carreras_id params)
        crow        (first (Query db ["SELECT * FROM carreras WHERE id = ?" carreras_id]))
        rows        (Query db [crear_carrera-sql carreras_id])]
    (doseq [row rows]
      (let [cartas_id     (str (:id row))
            carreras_id   (str (:carreras_id row))
            categorias_id (str (:categoria row))
            id            (:id
                           (first
                            (Query db
                                   ["SELECT id from contrareloj
                            WHERE cartas_id = ? AND carreras_id = ? AND categorias_id = ?" cartas_id carreras_id categorias_id])))
            postvars      {:id            (str id)
                           :cartas_id     cartas_id
                           :carreras_id   carreras_id
                           :categorias_id categorias_id}
            result        (Save db :contrareloj postvars ["id = ?" id])])))
  (redirect "/contrareloj"))
