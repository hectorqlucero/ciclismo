(ns ciclismo.cc.routes.reportes
  (:require [ciclismo.cm.routes.sql
             :refer
             [ptotales-sql totales-categoria-sql totales-categoria1-sql totales-sql]]
            [ciclismo.models.crud :refer [db Query]]
            [ciclismo.models.util :refer [create-categorias]]
            [selmer.parser :refer [render-file]]))

(def total-rows
  (Query db totales-sql))

(def categoria-rows
  (create-categorias total-rows))

(defn get-limited-row [categorias_id]
  (let [rows (Query db [totales-categoria-sql categorias_id])]
    rows))

(defn get-limited-carrera-row [categorias_id]
  (let [rows (Query db [totales-categoria1-sql categorias_id])]
    rows))

(defn totales-categoria []
  (let [rows  total-rows
        crows categoria-rows]
    (for [crow crows]
      (get-limited-row (crow :categorias_id)))))

(defn totales-categoria-carrera []
  (let [rows  total-rows
        crows categoria-rows]
    (for [crow crows]
      (get-limited-carrera-row (crow :categorias_id)))))

(defn leaders []
  (render-file "cc/leaders.html"
               {:title "Totales de Lideres"
                :rows  (flatten (totales-categoria))
                :crows categoria-rows}))

(defn rtotal []
  (render-file "cc/rtotal.html"
               {:title "Puntuación Total Serial"
                :rows (Query db ptotales-sql)}))

(defn carreras []
  (render-file "cc/carreras.html"
               {:title "Resultados Por Carrera y Categoria"
                :rows  (Query db "SELECT id,descripcion FROM carreras ORDER BY id")
                :items (flatten (totales-categoria-carrera))
                :crows categoria-rows}))
