(ns ciclismo.cm.admin.corredores.corredores
  (:require [cheshire.core :refer [generate-string]]
            [ciclismo.models.crud :refer [db Delete Query Save]]
            [ciclismo.models.grid :refer :all]
            [ciclismo.models.util :refer [fix-id 
                                          parse-int 
                                          capitalize-words
                                          format-date-internal]]
            [selmer.parser :refer [render-file]]))

(defn corredores
  []
  (render-file "cm/admin/corredores/index.html" {:title "Corredores"}))

;;start taller grid
(def search-columns
  ["id"
   "nombre"
   "apell_paterno"
   "apell_materno"
   "pais"
   "ciudad"
   "telefono"
   "email"])

(def aliases-columns
  ["id"
   "nombre"
   "apell_paterno"
   "apell_materno"
   "pais"
   "ciudad"
   "telefono"
   "email"])

(defn grid-json
  [{params :params}]
  (try
    (let [table    "corredores"
          scolumns (convert-search-columns search-columns)
          aliases  aliases-columns
          join     ""
          search   (grid-search (:search params nil) scolumns)
          order    (grid-sort (:sort params nil) (:order params nil))
          order    (grid-sort-extra order "nombre")
          offset   (grid-offset (parse-int (:rows params)) (parse-int (:page params)))
          rows     (grid-rows table aliases join search order offset)]
      (generate-string rows))
    (catch Exception e (.getMessage e))))
;;end taller grid

;;start taller form
(def form-sql
  "SELECT
   id,
   nombre,
   apell_paterno,
   apell_materno,
   pais,
   ciudad,
   telefono,
   email,
   sexo,
   DATE_FORMAT(fecha_nacimiento,'%m/%d/%Y') as fecha_nacimiento,
   direccion
   FROM corredores
   WHERE id = ?")

(defn form-json
  [id]
  (let [row (Query db [form-sql id])]
    (generate-string (first row))))
;;end cuadrante form

(defn corredores-save
  [{params :params}]
  (let [id       (fix-id (:id params))
        postvars {:id          id
                  :nombre           (capitalize-words (:nombre params))
                  :apell_paterno    (capitalize-words (:apell_paterno params))
                  :apell_materno    (capitalize-words (:apell_materno params))
                  :pais             (capitalize-words (:pais params))
                  :ciudad           (capitalize-words (:ciudad params))
                  :telefono         (:telefono params)
                  :email            (:email params)
                  :sexo             (clojure.string/upper-case (:sexo params))
                  :fecha_nacimiento (format-date-internal (:fecha_nacimiento params))
                  :direccion        (capitalize-words (:direccion params))}
        result   (Save db :corredores postvars ["id = ?" id])]
    (if (seq result)
      (generate-string {:success "Correctamente Processado!"})
      (generate-string {:error "No se pudo processar!"}))))

(defn corredores-delete
  [{params :params}]
  (let [id     (:id params nil)
        result (if-not (nil? id)
                 (Delete db :corredores ["id = ?" id])
                 nil)]
    (if (seq result)
      (generate-string {:success "Removido appropiadamente!"})
      (generate-string {:error "No se pudo remover!"}))))
