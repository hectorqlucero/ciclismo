(ns ciclismo.cc.routes.padron
  (:require [cheshire.core :refer [generate-string]]
            [noir.response :refer [redirect]]
            [noir.session :as session]
            [ciclismo.cm.routes.sql :refer [cartas-sql totales-sql]]
            [ciclismo.models.uploads :refer [upload-photo]]
            [ciclismo.models.crud :refer [db Query Save Update]]
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
        result    (Query db ["SELECT id FROM corredores  WHERE id = ? AND password = ?" padron  password])]
    (if (seq result)
      (do
        (session/put! :padron padron)
        (session/put! :is_authenticated true)
        (generate-string {:url (str "/padron/edit/" padron)}))
      (generate-string {:url "/padron"}))))

(defn registrar [request]
  (if (get-padron-id)
    (render-file "404.html" {:error "Existe una session, no se puede crear una nuevo Padron."
                             :return-url "/padron"})
    (render-file "cc/registrar/registrar.html" {:title "Padron de Ciclistas"
                                                :padron nil
                                                :foto (get-photo "padron" "foto" "id" nil)})))

(defn create-registrar-data [params]
  {:nombre (params :nombre)
   :apell_paterno (capitalize-words (:apell_paterno params))
   :apell_materno (capitalize-words (:apell_materno params))
   :pais (params :pais)
   :ciudad (capitalize-words (params :ciudad))
   :telefono (params :telefono)
   :email (clojure.string/lower-case (params :email))
   :sexo (params :sexo)
   :fecha_nacimiento (format-date-internal (params :fecha_nacimiento))
   :direccion (capitalize-words (params :direccion))
   :password (params :password)})

(defn create-registrar-email [postvars]
  (let [nombre (str (:nombre postvars) " " (:apell_paterno postvars) " " (:apell_materno postvars))
        email (:email postvars)
        subject "Tu padron ha sido registrado correctamente"
        content (str "<strong>Hola</strong> " nombre ",</br></br>"
                     "Tu padron # " (:padron postvars) " se registro correctamente.  Haz click en salir despues de revisar que los datos sean correctos.</br></br>Sinceramente,</br>CICLISMO BC")
        body {:from "ciclismobc@fastmail.com"
              :to email
              :subject subject
              :body [{:type "text/html;charset=utf-8"
                      :content content}]}]
    body))

(defn registrar! [{params :params}]
  (let [padron (or (:id params) "0")
        postvars (create-registrar-data params)
        email-body (create-registrar-email postvars)
        result    (Save db :corredores postvars ["id = ?" padron])]
    (if (seq result)
      (do
        (session/put! :padron padron)
        (session/put! :is_authenticated true)
        (send-email host email-body)
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
  DATE_FORMAT(fecha_nacimiento,'%m/%d/%Y') as fecha_nacimiento,
  direccion
  FROM corredores
  WHERE id = ?
  ")

(defn padron-load [padron]
  (let [row (first (Query db [padron-sql padron]))]
    (generate-string row)))

(defn padron-edit [padron]
  (if (= (parse-int padron) (parse-int (get-padron-id)))
    (do
      (let [row (first (Query db [padron-sql padron]))]
        (render-file "cc/registrar/padron.html"
                     {:title "Padron de Ciclistas"
                      :id padron
                      :padron (get-padron-id)})))
    (redirect "/padron")))
;; End padron

(defn padron-save [{params :params}]
  (let [padron (or (:id params) "0")
        postvars  (dissoc (create-registrar-data params) :password)
        result    (Save db :corredores postvars ["id = ?" padron])]
    (if (seq result)
      (do
        (session/put! :padron padron)
        (session/put! :is_authenticated true)
        (generate-string {:url "/padron"}))
      (generate-string {:error "No se pudo actualizar el Registro!"}))))

;; Start reset-password
(defn email-body [row url]
  (let [nombre       (str (:nombre row) " " (:apell_paterno row) " " (:apell_materno row))
        padron-email (:email row)
        subject      "Resetear tu contraseña"
        content      (str "<strong>Hola</strong> " nombre ",</br></br>"
                          "Para resetear tu contraseña <strong>" "<a href='" url "'>Clic Aqui</a>" "</strong>.</br></br>"
                          "Alternativamente, puedes copiar y pegar el siguiente link en la barra de tu browser:</br></br>"
                          url "</br></br>"
                          "Este link solo sera bueno por 10 minutos.</br></br>"
                          "Si no solicito resetear su contraseña simplemente ignore este mensage.</br></br></br>"
                          "Sinceramente,</br></br>"
                          "La administracion")
        body         {:from    "escueladeodontologiamxli@fastmail.com"
                      :to      padron-email
                      :subject subject
                      :body    [{:type    "text/html;charset=utf-8"
                                 :content content}]}]
    body))

(defn reset-password [request]
  (println "get-padron-id: " (get-padron-id))
  (println "Im here....")
  (if (get-padron-id)
    (render-file "404.html" {:error "Existe una session, no se puede cambiar la contraseña"
                             :return-url "/padron"})
    (render-file "cc/registrar/rpaswd.html" {:title "Resetear Contraseña"})))

(defn reset-password! [request]
  (let [params     (:params request)
        padron      (:padron params)
        token      (create-token padron)
        url        (get-reset-url request "/padron/reset_password/" token)
        row        (first (Query db ["SELECT * FROM corredores WHERE id = ?" padron]))
        email-body (email-body row url)]
    (if (future (send-email host email-body))
      (generate-string {:url "/padron"})
      (generate-string {:error "No se pudo resetear su contraseña"}))))

(defn reset-jwt [token]
  (let [padron (check-token token)]
    (if-not (nil? padron)
      (render-file "cc/registrar/reset_password.html" {:title "Resetear Contraseña"
                                                       :row   (generate-string {:padron padron})})
      (render-file "404.html" {:title "Resetear Contraseña"
                               :return-url "/padron"
                               :error "Su token es incorrecto o ya expiro!"}))))

(defn reset-jwt! [{params :params}]
  (let [padron (or (:padron params) "0")
        postvars  {:padron padron
                   :password  (:password params)}
        result    (Update db :corredores postvars ["id = ?" padron])]
    (if (seq result)
      (generate-string {:url "/padron"})
      (generate-string {:error "No se pudo cambiar su contraseña!"}))))
;; End reset-password

(defn logoff [_]
  (session/remove! :padron)
  (session/remove! :is_authenticated)
  (redirect "/padron"))
