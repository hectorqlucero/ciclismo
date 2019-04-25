(ns ciclismo.cm.admin.carreras.carreras
  (:require [cheshire.core :refer [generate-string]]
            [ciclismo.models.crud :refer [db Delete Query Save Update]]
            [ciclismo.models.grid :refer :all]
            [ciclismo.models.util
             :refer
             [capitalize-words
              fix-hour fix-id
              format-date-internal
              parse-int
              create-carreras-categorias]]
            [selmer.parser :refer [render-file]]))

(defn carreras
  []
  (render-file "cm/admin/carreras/index.html" {:title "Carreras"}))
(create-carreras-categorias)
;;start carreras grid
(def search-columns
  ["id"
   "descripcion"
   "donde"
   "DATE_FORMAT(fecha,'%m/%d/%Y')"
   "TIME_FORMAT(hora,'%h:%i %p')"
   "distancia"
   "puntos_p"
   "puntos_1"
   "puntos_2"
   "puntos_3"
   "CASE WHEN status = 'T' THEN 'Activo' ELSE 'Inactivo' END"])

(def aliases-columns
  ["id"
   "descripcion"
   "donde"
   "DATE_FORMAT(fecha,'%m/%d/%Y') as fecha"
   "TIME_FORMAT(hora,'%h:%i %p') as hora"
   "distancia"
   "puntos_p"
   "puntos_1"
   "puntos_2"
   "puntos_3"
   "CASE WHEN status = 'T' THEN 'Activo' ELSE 'Inactivo' END as status"])

(defn grid-json
  [{params :params}]
  (try
    (let [table    "carreras"
          scolumns (convert-search-columns search-columns)
          aliases  aliases-columns
          join     ""
          search   (grid-search (:search params nil) scolumns)
          order    (grid-sort (:sort params nil) (:order params nil))
          offset   (grid-offset (parse-int (:rows params)) (parse-int (:page params)))
          rows     (grid-rows table aliases join search order offset)]
      (generate-string rows))
    (catch Exception e (.getMessage e))))
;;end carreras grid

;;start carreras form
(def form-sql
  "SELECT id as id,
   descripcion,
   donde,
   banco,
   banco_cuenta,
   banco_instrucciones,
   organizador,
   DATE_FORMAT(fecha,'%m/%d/%Y') as fecha,
   TIME_FORMAT(hora,'%H:%i') as hora,
   distancia,
   puntos_p,
   puntos_1,
   puntos_2,
   puntos_3,
   status
   FROM carreras
   WHERE id = ?")

(defn form-json
  [id]
  (let [row (Query db [form-sql id])]
    (generate-string (first row))))
;;end cuadrante form

(defn carreras-save
  [{params :params}]
  (let [id        (fix-id (:id params))
        puntos_p  (:puntos_p params)
        puntos_1  (:puntos_1 params)
        puntos_2  (:puntos_2 params)
        puntos_3  (:puntos_3 params)
        status    (:status params)
        distancia (:distancia params)
        postvars  {:id                  id
                   :descripcion         (capitalize-words (:descripcion params))
                   :donde               (:donde params)
                   :banco               (:banco params)
                   :banco_cuenta        (:banco_cuenta params)
                   :banco_instrucciones (:banco_instrucciones params)
                   :organizador         (:organizador params)
                   :fecha               (format-date-internal (:fecha params))
                   :hora                (fix-hour (:hora params))
                   :distancia           distancia
                   :puntos_p            puntos_p
                   :puntos_1            puntos_1
                   :puntos_2            puntos_2
                   :puntos_3            puntos_3
                   :status              status}
        result    (Save db :carreras postvars ["id = ?" id])
        the-id    (if (nil? id) (get (first result) :generated_key nil) id)]
    (if (seq result)
      (do
        (create-carreras-categorias)
        (generate-string {:success "Correctamente Processado!"}))
      (generate-string {:error "No se pudo processar!"}))))

(defn carreras-delete
  [{params :params}]
  (let [id     (:id params nil)
        result (if-not (nil? id)
                 (Delete db :carreras ["id = ?" id])
                 nil)]
    (if (seq result)
      (generate-string {:success "Removido appropiadamente!"})
      (generate-string {:error "No se pudo remover!"}))))

;;Start carreras_categorias
(def carreras_categorias-sql
  "SELECT
   carreras_categorias.id,
   carreras_categorias.status,
   carreras_categorias.carreras_id,
   carreras_categorias.categorias_id,
   categorias.descripcion
   FROM categorias
   JOIN carreras_categorias on carreras_categorias.categorias_id = categorias.id
   WHERE carreras_categorias.carreras_id = ?
   ORDER BY categorias.id")

(defn carreras_categorias
  [carreras_id]
  (render-file "cm/admin/carreras/categorias.html" {:title    "Carreras"
                                                    :items       (Query db [carreras_categorias-sql carreras_id])
                                                    :carreras_id carreras_id}))

(defn carreras_categorias-process [{params :params}]
  (let [id            (:id params)
        carreras_id   (:carreras_id params)
        categorias_id (:categorias_id params)
        status        (:status_id params)
        postvars      {:id            id
                       :carreras_id   carreras_id
                       :categorias_id categorias_id
                       :status        status}
        result        (Update db :carreras_categorias postvars ["id = ?" id])]
    (if (seq result)
      (str "Se processor correctamente!")
      (str "No se pudo processar correctamente!"))))

;;End carreras_categorias
