(ns ciclismo.cc.routes.padron
  (:require [cheshire.core :refer [generate-string]]
            [noir.response :refer [redirect]]
            [noir.session :as session]
            [ciclismo.cm.routes.sql :refer [cartas-sql totales-sql]]
            [ciclismo.models.uploads :refer [upload-photo]]
            [ciclismo.models.crud :refer [db Query Save]]
            [ciclismo.models.email :refer [host send-email]]
            [ciclismo.models.util
             :refer
             [capitalize-words 
              parse-int
              check-token
              create-token
              get-photo
              get-reset-url
              format-date-internal
              format-date-external 
              get-session-id 
              get-padron-id]]
            [selmer.parser :refer [render-file]]))

(defn main []
  (let [title "Hacer click en el menu \"Entrar\" para accessar el sitio!"
        padron (get-padron-id)]
    (render-file "cc/main.html" {:title title
                                 :padron padron})))

(defn page-error [request]
  (render-file "escuela.html" {:error "No se encuentra la pagina que buscas!"}))

(defn buscar [request]
  (render-file "cc/registrar/buscar.html" {:title "Busqueda de Padron"
                                           :padron (get-padron-id)}))

(defn buscar! [{params :params}]
  (let [padron (or (:padron params) "0")
        password  (:password params)
        result    (Query db ["SELECT id FROM padron  WHERE id = ? AND password = ?" padron  password])]
    (if (seq result)
      (do
        (session/put! :padron padron)
        (session/put! :is_authenticated true)
        (generate-string {:url (str "/padron/" padron)}))
      (generate-string {:url "/padron"}))))

(defn registrar [request]
  (if (get-padron-id)
    (render-file "404.html" {:error "Existe una session, no se puede crear una nuevo Padron."
                             :return-url "/padron"})
    (render-file "cc/registrar/registrar.html" {:title "Padron de Ciclistas"
                                                :padron nil
                                                :foto (get-photo "padron" "foto" "id" nil)})))

(defn create-data [params]
  {:nombre (params :nombre)
   :apell_paterno (capitalize-words (:apell_paterno params))
   :apell_materno (capitalize-words (:apell_materno params))
   :ciudad (capitalize-words (params :ciudad))
   :telfono (params :telefono)
   :email (clojure.string/lower-case (params :email))
   :sexo (params :sexo)
   :direccion (capitalize-words (params :direccion))
   :foto (params :foto)
   :nacional (or (params :nacional) "F")
   :uci (or (params :uci) "F")
   :uciid (or (params :uciid) "F")
   :categoria (params :categoria)
   :licencia (params :licencia)
   :registro_tipo (params :registro_tipo)
   :asociacion (capitalize-words (params :asociacion))
   :nacionalidad (capitalize-words (params :nacionalidad))
   :equipo (params :equipo)
   :t_nacional (or (params :t_nacional) "F")
   :t_continental (or (params :t_continental) "F")
   :curp (params :curp)
   :calle_numero (params :calle_numero)
   :colonia (capitalize-words (params :colonia))
   :municipio (capitalize-words (params :municipio))})

(defn create-registrar-email [postvars]
  (let [nombre (str (:nombre postvars) " " (:apell_paterno postvars) " " (:apell_materno postvars))
        email (:email postvars)
        subject "Tu padron ha sido registrado correctamente"
        content (str "<strong>Hola</strong> " nombre ",</br></br>"
                     "Tu padron # " (:padron postvars) " se registro correctamente.  Haz click en salir despues de revisar que los datos sean correctos.</br></br>Sinceramente,</br>Federación Mexicana de ciclismo A.C.")
        body {:from "ciclismobc@fastmail.com"
              :to email
              :subject subject
              :body [{:type "text/html;charset=utf-8"
                      :content content}]}]
    body))

(defn registrar! [{params :params}]
  (let [padron (or (:padron params) "0")
        file      (:file params)
        foto      (upload-photo file padron)
        postvars  (assoc (create-data params) :padron padron :foto foto)
        email-body (create-registrar-email postvars)
        result    (Save db :alumnos postvars ["padron = ?" padron])]
    (if (seq result)
      (do
        (session/put! :padron padron)
        (session/put! :is_authenticated true)
        (future (send-email host email-body))
        (generate-string {:url (str "/padron/" padron)}))
      (generate-string {:error "No se pudo actualizar el Registro!"}))))

;; Start padron
(def padron-sql
  "
  SELECT
  id,
  nombre,
  apell_paterno,
  apell_materno,
  pais,
  ciudad,
  telefono,
  email,
  sexo,
  direccion,
  foto,
  nacional,
  uci,
  uciid,
  categoria,
  licencia,
  registro_tipo,
  asociacion,
  nacionalidad,
  equipo,
  t_nacional,
  t_continental,
  curp,
  calle_numero,
  colonia,
  municipio,
  estado,
  codigo_postal,
  DATE_FORMAT(fecha_nacimiento, '%m/%d/%Y') as fecha_nacimiento
  FROM padron
  WHERE id = ?
  ")

(defn padron [padron]
  (if (= (parse-int padron) (parse-int (get-padron-id)))
    (do
      (let [row (first (Query db [padron-sql padron]))]
        (render-file "cc/padron/padron.html"
                     {:title "Padron de Ciclistas"
                      :padron padron
                      :foto (get-photo "padron" "foto" "id" padron)
                      :row (generate-string row)})))
    (redirect "/")))
;; End padron

(defn padron! [{params :params}]
  (let [padron (or (:padron params) "0")
        file      (:file params)
        foto      (upload-photo file padron)
        postvars  (assoc (create-data params) :padron padron :foto foto)
        result    (Save db :alumnos postvars ["id = ?" padron])]
    (if (seq result)
      (do
        (session/put! :padron padron)
        (session/put! :is_authenticated true)
        (generate-string {:url (str "/padron/" padron)}))
      (generate-string {:error "No se pudo actualizar el Registro!"}))))
