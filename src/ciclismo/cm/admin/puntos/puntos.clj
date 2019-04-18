(ns ciclismo.cm.admin.puntos.puntos
  (:require [cheshire.core :refer [generate-string]]
            [ciclismo.models.crud :refer [db Query Save]]
            [ciclismo.models.grid :refer :all]
            [ciclismo.models.util :refer [fix-id parse-int user-level]]
            [selmer.parser :refer [render-file]]))

(def carreras_id (atom nil))

(defn carreras-descripcion []
  (:descripcion (first (Query db ["SELECT descripcion FROM carreras WHERE id = ?" @carreras_id]))))

(defn puntos []
  (render-file "cm/admin/puntos/pre_puntos.html" {:title "Actualizar Puntos"}))

(defn process-puntos [{params :params}]
  (if-not (nil? (:carrera_id params)) (reset! carreras_id (:carrera_id params)))
  (render-file "cm/admin/puntos/puntos.html"
               {:title       (str "Actualizar Puntos: " (carreras-descripcion))
                :carreras_id (:carrera_id params)}))

;;Start ciclistas_puntos grid
(defn crear-puntos [carreras_id]
  (let [rows (Query db ["SELECT * FROM cartas WHERE carreras_id = ?" carreras_id])]
    (doseq [row rows]
      (let [cartas_id (str (:id row))
            puntos    (str 1)
            postvars  {:cartas_id cartas_id
                       :puntos_p  puntos}
            result    (Save db :puntos postvars ["cartas_id = ?" cartas_id])]))))

(def search-columns
  ["cartas.id"
   "cartas.no_participacion"
   "cartas.nombre"
   "categorias.descripcion"
   "puntos.puntos_p"
   "puntos.puntos_1"
   "puntos.puntos_2"
   "puntos.puntos_3"])

(def aliases-columns
  ["cartas.id as id"
   "cartas.no_participacion"
   "cartas.nombre"
   "categorias.descripcion as categoria"
   "puntos.puntos_p"
   "puntos.puntos_1"
   "puntos.puntos_2"
   "puntos.puntos_3"])

(defn grid-json [{params :params}]
  (if-not (nil? (:carreras_id params))
    (do
      (reset! carreras_id (:carreras_id params))
      (crear-puntos (:carreras_id params))))
  (try
    (let [table    "cartas"
          scolumns (convert-search-columns search-columns)
          aliases  aliases-columns
          join     "left join puntos on puntos.cartas_id = cartas.id
                join categorias on categorias.id = cartas.categoria"
          search   (grid-search (:search params nil) scolumns)
          search   (grid-search-extra search (str "cartas.carreras_id = " @carreras_id))
          order    (grid-sort (:sort params nil) (:order params nil))
          order    (grid-sort-extra order "categoria ASC,nombre ASC")
          offset   (grid-offset (parse-int (:rows params)) (parse-int (:page params)))
          rows     (grid-rows table aliases join search order offset)]
      (generate-string rows))
    (catch Exception e (.getMessage e))))
;;End ciclistas_puntos grid

;;Start form
(def form-sql
  "SELECT
   p.id,
   p.nombre,
   p.categoria,
   s.puntos_p,
   s.puntos_1,
   s.puntos_2,
   s.puntos_3
   FROM cartas p
   JOIN puntos s on s.cartas_id = p.id
   WHERE p.id = ?")

(defn form-json [id]
  (let [row (Query db [form-sql id])]
    (generate-string (first row))))

(defn puntos-save [{params :params}]
  (try
    (let [cartas_id (fix-id (:id params))
          postvars  {:cartas_id cartas_id
                     :puntos_1  (:puntos_1 params)
                     :puntos_2  (:puntos_2 params)
                     :puntos_3  (:puntos_3 params)}
          result    (Save db :puntos postvars ["cartas_id = ?" cartas_id])]
      (if (seq result)
        (generate-string {:success "Correctamente Processado!"})
        (generate-string {:error "No se pudo processar!"})))
    (catch Exception e (.getMessage e))))
;;End form
