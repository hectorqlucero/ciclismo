(ns ciclismo.cm.routes.rodadas
  (:require [cheshire.core :refer [generate-string]]
            [ciclismo.cm.routes.sql :refer [rodadas-sql]]
            [ciclismo.models.crud :refer [db Delete Query Query!]]
            [selmer.parser :refer [render-file]]))

(defn purge []
  (Delete db :rodadas ["fecha < CURRENT_DATE() AND repetir != 'T'"]))

(defn repeat-event []
  (let [purge-rows (Query db "SELECT id from rodadas where fecha < CURRENT_DATE()")
        purge-keys (apply str (interpose "," (map #(str (:id %)) purge-rows)))
        sql        (str "DELETE from rodadas_link where rodadas_id IN(" purge-keys ")")
        result     (if-not (clojure.string/blank? purge-keys)
                     (Query! db sql)
                     nil)]
    (Query! db "UPDATE rodadas SET fecha = DATE_ADD(fecha,INTERVAL 7 DAY) WHERE fecha < CURRENT_DATE()")))

(defn process-confirmados [rodadas_id]
  (let [rows (Query db ["select email from rodadas_link where rodadas_id = ? and asistir = ?" rodadas_id "T"])
        data (if (seq rows)
               (subs (clojure.string/triml (apply str (map #(str ", " (:email %)) rows))) 2)
               "ninguno")]
    data))

(defn build-cal-popup [row]
  (str
    "<html>"
    "<body>"
    "<div style='margin-bottom:5px;'><label><strong>Titulo: </strong>" (:title row) "</div>"
    "<div style='margin-bottom:5px;'><label><strong>Describir Rodadas: </strong>" (:description row) "</div>"
    "<div style='margin-bottom:5px;'><label><strong>Punto de reunion: </strong>" (:donde row) "</div>"
    "<div style='margin-bottom:5px;'><label><strong>Nivel: </strong>" (:nivel row) "</div>"
    "<div style='margin-bottom:5px;'><label><strong>Distancia: </strong>" (:distancia row) "</div>"
    "<div style='margin-bottom:5px;'><label><strong>Velocidad: </strong>" (:velocidad row) "</div>"
    "<div style='margin-bottom:5px;'><label><strong>Fecha/Rodada: </strong>" (:fecha row) "</div>"
    "<div style='margin-bottom:5px;'><label><strong>Salida: </strong>" (:hora row) "</div>"
    "<div style='margin-bottom:5px;'><label><strong>Lider: </strong>" (:leader row) "</div>"
    "<div style='margin-bottom:5px;'><label><strong>Lider Email: </strong>" (:email row) "</div>"
    "</body>"
    "</html>"))

(defn main [request]
  (purge)
  (repeat-event)
  (let [rows   (Query db rodadas-sql)
        rows   (map #(assoc % :modal_html (build-cal-popup %)) rows)
        events (generate-string rows)]
    (render-file "cm/main.html" {:title  (str "Calendario de Eventos - Haz clic en el evento para confirmar asistencia")
                                 :events events})))
