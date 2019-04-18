(ns ciclismo.cm.routes.home
  (:require [cheshire.core :refer [generate-string]]
            [ciclismo.models.crud :refer [db Query]]
            [ciclismo.models.util :refer [get-session-id]]
            [noir.session :as session]
            [noir.util.crypt :as crypt]
            [noir.response :refer [redirect]]
            [selmer.parser :refer [render-file]]))

(defn slide [request]
  (render-file "cm/carousel.html" {:title "Reto San Felipe"}))

(defn login [_]
  (if-not (nil? (get-session-id))
    (redirect "/eventos")
    (render-file "cm/login.html" {:title "Accesar al Sitio!"})))

(defn login! [username password]
  (let [row    (first (Query db ["select * from users where username = ?" username]))
        active (:active row)]
    (if (= active "T")
      (do
        (if (crypt/compare password (:password row))
          (do
            (session/put! :user_id (:id row))
            (generate-string {:url "/eventos"}))
          (generate-string {:error "Hay problemas para accesar el sitio!"})))
      (generate-string {:error "El usuario esta inactivo!"}))))

(defn logoff []
  (session/clear!)
  (redirect "/"))
