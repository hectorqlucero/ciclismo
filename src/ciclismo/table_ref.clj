(ns ciclismo.table_ref
  (:require [cheshire.core :refer [generate-string]]
            [ciclismo.models.crud :refer [db Query]]
            [ciclismo.models.util :refer [current_year parse-int]]
            [compojure.core :refer [defroutes GET]]))

(def get_users-sql
  "SELECT id AS value, concat(firstname,' ',lastname) AS text FROM users order by firstname,lastname")

(def get_cuadrantes-sql
  "SELECT id AS value, name AS text FROM cuadrantes order by name")

(def get_carreras-sql
  "SELECT id AS value, CONCAT(descripcion,' - ',DATE_FORMAT(fecha,'%m/%d/%Y')) as text FROM carreras WHERE status = 'T' ORDER BY id")

(def get_carreras_all-sql
  "SELECT id AS value, CONCAT(descripcion,' - ',DATE_FORMAT(fecha,'%m/%d/%Y')) as text FROM carreras ORDER BY id")

(defn months []
  (list
   {:value 1 :text "Enero"}
   {:value 2 :text "Febrero"}
   {:value 3 :text "Marzo"}
   {:value 4 :text "Abril"}
   {:value 5 :text "Mayo"}
   {:value 6 :text "Junio"}
   {:value 7 :text "Julio"}
   {:value 8 :text "Agosto"}
   {:value 9 :text "Septiembre"}
   {:value 10 :text "Octubre"}
   {:value 11 :text "Noviembre"}
   {:value 12 :text "Diciembre"}))

(defn years [p n]
  (let [year   (parse-int (current_year))
        pyears (for [n (range (parse-int p) 0 -1)] {:value (- year n) :text (- year n)})
        nyears (for [n (range 0 (+ (parse-int n) 1))] {:value (+ year n) :text (+ year n)})
        years  (concat pyears nyears)]
    years))

(defn appointment-options []
  (list
   {:value "T" :text "Pendiente"}
   {:value "X" :text "Remover"}
   {:value "O" :text "Completado en tiempo"}
   {:value "L" :text "Completedo tarde"}
   {:value "E" :text "Completed antes de tiempo"}
   {:value "S" :text "Re-programmado por usuario"}
   {:value "Z" :text "Cancelado por usuario"}))

(defn nivel-options []
  (list
   {:value "P" :text "Principiantes"}
   {:value "M" :text "Medio"}
   {:value "A" :text "Avanzado"}
   {:value "T" :text "TODOS"}))

(defn get-help []
  (str "<a href='/uploads/lucero-systems.pdf'></a>"))

(defn categorias []
  (Query db "SELECT id AS value, descripcion AS text FROM categorias ORDER BY id"))

(defn carreras-primero []
  (let [puntos (:puntos_1 (first (Query db "SELECT puntos_1 FROM carreras WHERE status='T'")))
        result (list {:value puntos :text (str puntos " puntos")})]
    result))

(defn carreras-segundo []
  (let [puntos (:puntos_2 (first (Query db "SELECT puntos_2 FROM carreras WHERE status='T'")))
        result (list {:value puntos :text (str puntos " puntos")})]
    result))

(defn carreras-tercero []
  (let [puntos (:puntos_3 (first (Query db "SELECT puntos_3 FROM carreras WHERE status='T'")))
        result (list {:value puntos :text (str puntos " puntos")})]
    result))

;; Start carreras_categorias
(def carreras_categorias-sql
  "SELECT
   carreras_categorias.categorias_id as value,
   categorias.descripcion as text
   FROM carreras_categorias
   LEFT JOIN categorias on categorias.id = carreras_categorias.categorias_id
   WHERE carreras_categorias.carreras_id = ?
   AND carreras_categorias.status = 'T'")

(defn carreras_categorias [carreras_id]
  (Query db [carreras_categorias-sql carreras_id]))
;; End carreras_categorias

(def nombres-sql
  "SELECT nombre as value,nombre as text FROM cartas ORDER BY nombre")

(def correos-sql
  "SELECT email as value,email as text FROM cartas ORDER BY email")

(defn build-link [carreras_id categorias_id]
  (str "/table_ref/carreras/categorias/" carreras_id "/" categorias_id))

(defn carreras [carreras_id]
  (let [categorias (carreras_categorias carreras_id)]
    (for [categoria categorias]
      (str "
<div id='categoria-parent' class='easyui-accordion' data-options='fit:false,selected:false'>
<div title='" (categoria :text) "' data-options='fit:false,href:\"" (build-link carreras_id (categoria :value)) "\"'></div></div>"))))

(def carreras-categorias-sql
  "SELECT
p.nombre,
p.categoria as categorias_id,
s1.descripcion as categoria,
SUM((IFNULL(s.puntos_p,0) + IFNULL(s.puntos_1,0) + IFNULL(s.puntos_2,0) + IFNULL(s.puntos_3,0))) as puntos
FROM cartas p
JOIN puntos s on s.cartas_id = p.id
JOIN categorias s1 on s1.id = p.categoria
WHERE
p.carreras_id = ?
AND p.categoria = ?
GROUP BY p.carreras_id,p.email,p.categoria
ORDER BY p.carreras_id,puntos DESC
")

(defn carreras-categorias [carreras_id categorias_id]
  (let [rows (Query db [carreras-categorias-sql carreras_id categorias_id])
        table-head (str "<table class='table'><thead class='thead-dark'><tr><th>Nombre</th><th>Puntos</th></tr></thead><tbody>")
        table-body (apply str (for [row rows]
                                (str "<tr><td>" (row :nombre) "</td><td>" (row :puntos) "</td></tr>")))
        table-foot (str "</tbody></table>")
        the-table (str table-head table-body table-foot)]
    the-table))

(defroutes table_ref-routes
  (GET "/table_ref/carreras/:carreras_id" [carreras_id] (apply str (carreras carreras_id)))
  (GET "/table_ref/carreras/categorias/:carreras_id/:categorias_id" [carreras_id categorias_id] (carreras-categorias carreras_id categorias_id))
  (GET "/table_ref/get_users" [] (generate-string (Query db [get_users-sql])))
  (GET "/table_ref/get_cuadrantes" [] (generate-string (Query db [get_cuadrantes-sql])))
  (GET "/table_ref/months" [] (generate-string (months)))
  (GET "/table_ref/years/:pyears/:nyears" [pyears nyears] (generate-string (years pyears nyears)))
  (GET "/table_ref/appointment_options" [] (generate-string (appointment-options)))
  (GET "/table_ref/nivel_options" [] (generate-string (nivel-options)))
  (GET "/table_ref/help" [] (get-help))
  (GET "/table_ref/categorias" [] (generate-string (categorias)))
  (GET "/table_ref/carreras_categorias/:carreras_id" [carreras_id] (generate-string (carreras_categorias carreras_id)))
  (GET "/table_ref/primero" [] (generate-string (carreras-primero)))
  (GET "/table_ref/segundo" [] (generate-string (carreras-segundo)))
  (GET "/table_ref/tercero" [] (generate-string (carreras-tercero)))
  (GET "/table_ref/carreras" [] (generate-string (Query db get_carreras-sql)))
  (GET "/table_ref/carreras_all" [] (generate-string (Query db get_carreras_all-sql)))
  (GET "/table_ref/nombres" [] (generate-string (Query db nombres-sql)))
  (GET "/table_ref/correos" [] (generate-string (Query db correos-sql))))
