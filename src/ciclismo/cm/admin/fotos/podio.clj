(ns ciclismo.cm.admin.fotos.podio
  (:require [ciclismo.models.crud :refer [config]]
            [clojure.java.io :as io]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [redirect]]
            [selmer.parser :refer [render-file]]))

(def UPLOADS (str (config :uploads) "mp/"))

(defn main [_]
  (render-file "cm/admin/fotos/podio.html"
               {:title "Subir Fotos"}))

(defn upload-file [file rfile]
  (let [tempfile  (file :tempfile)
        real-name (file :filename)
        size      (:size file)
        type      (:content-type file)
        extension (peek (clojure.string/split type #"\/"))
        extension (if (= extension "jpeg") "jpg" "jpg")
        filename  (str rfile "." extension)
        result    (if-not (zero? size)
                    (do (io/copy tempfile (io/file (str UPLOADS filename)))))]
    result))

(defn upload-picture [{params :params}]
  (doseq [n (range 1 15)]
    (let [rfile (str "r" n)
          kfile (keyword rfile)
          file  (kfile params)]
      (upload-file file rfile)))
  (redirect "/registro/fotos"))
