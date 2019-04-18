(ns ciclismo.cm.routes.sql)

(def totales-categoria-sql
  "SELECT
     p.email,
     p.nombre,
     p.categoria as categorias_id,
     s1.descripcion as categoria,
     SUM((IFNULL(s.puntos_p,0) + IFNULL(s.puntos_1,0) + IFNULL(s.puntos_2,0) + IFNULL(s.puntos_3,0))) as puntos
     FROM cartas p
     JOIN puntos s on s.cartas_id = p.id
     JOIN categorias s1 on s1.id = p.categoria
     WHERE p.categoria = ?
     GROUP by p.email,p.categoria
     ORDER BY puntos DESC,p.nombre
     LIMIT 3")

(def totales-categoria1-sql
  "SELECT
     p.carreras_id,
     p.email,
     p.nombre,
     p.categoria as categorias_id,
     s1.descripcion as categoria,
     SUM((IFNULL(s.puntos_p,0) + IFNULL(s.puntos_1,0) + IFNULL(s.puntos_2,0) + IFNULL(s.puntos_3,0))) as puntos
     FROM cartas p
     JOIN puntos s on s.cartas_id = p.id
     JOIN categorias s1 on s1.id = p.categoria
     WHERE p.categoria = ?
     GROUP by p.carreras_id,p.email,p.categoria
     ORDER BY p.carreras_id,puntos DESC")

(def cartas-sql
  "SELECT
   id,
   categoria,
   no_participacion,
   nombre,
   sexo,
   edad,
   equipo,
   telefono,
   email,
   tutor,
   carreras_id
   FROM cartas
   WHERE email = ?
   AND categoria = ?
   AND carreras_id = ?")

(def eventos-sql
  "
  SELECT
  id,
  DAY(fecha) as day,
  CASE WHEN DAYNAME(fecha) = 'Sunday' THEN 'Domingo' WHEN DAYNAME(fecha) = 'Monday' THEN 'Lunes' WHEN DAYNAME(fecha) = 'Tuesday' THEN 'Martes' WHEN DAYNAME(fecha) = 'Wednesday' THEN 'Miercoles' WHEN DAYNAME(fecha) = 'Thursday' THEN 'Jueves' WHEN DAYNAME(fecha) = 'Friday' THEN 'Viernes' WHEN DAYNAME(fecha) = 'Saturday' THEN 'Sabado' END AS fecha_dow,
  DATE_FORMAT(fecha,'%m/%d/%Y') AS fecha,
  descripcion,
  descripcion_corta,
  punto_reunion,
  TIME_FORMAT(hora,'%h:%i %p') as hora,
  leader
  FROM rodadas
  WHERE
  repetir = 'F'
  AND rodada = 'F'
  AND YEAR(fecha) = ?
  AND MONTH(fecha) = ?
  ORDER BY
  DAY(fecha),
  hora ")

(def rodadas-sql
  "SELECT
  id,
  descripcion_corta as title,
  descripcion as description,
  DATE_FORMAT(hora,'%h:%i %p') as hora,
  CONCAT(fecha,'T',hora) as start,
  punto_reunion as donde,
  CASE WHEN nivel = 'P' THEN 'Principiantes' WHEN nivel = 'M' THEN 'Medio' WHEN nivel = 'A' THEN 'Avanzado' WHEN nivel = 'T' THEN 'TODOS' END as nivel,
  distancia as distancia,
  velocidad as velocidad,
  leader as leader,
  leader_email as email,
  rodada as rodada,
  repetir,
  CONCAT('/entrenamiento/rodadas/asistir/',id) as url
  FROM rodadas
  WHERE rodada = 'T'
  ORDER BY fecha,hora ")

(def totales-sql
  "SELECT
     p.email,
     p.nombre,
     s1.id as categorias_id,
     s1.descripcion as categoria,
     SUM((IFNULL(s.puntos_p,0) + IFNULL(s.puntos_1,0) + IFNULL(s.puntos_2,0) + IFNULL(s.puntos_3,0))) as puntos
     FROM cartas p
     JOIN puntos s on s.cartas_id = p.id
     JOIN categorias s1 on s1.id = p.categoria
     GROUP by p.email,p.categoria
     ORDER BY s1.descripcion,puntos DESC,p.nombre")

(def ptotales-sql
  "SELECT
   p.email,
   p.nombre,
   s1.descripcion as categoria,
   SUM((IFNULL(s.puntos_p,0) + IFNULL(s.puntos_1,0) + IFNULL(s.puntos_2,0) + IFNULL(s.puntos_3,0))) as puntos
   FROM cartas p
   JOIN puntos s on s.cartas_id = p.id
   JOIN categorias s1 on s1.id = p.categoria
   GROUP BY p.email,p.categoria
   ORDER BY s1.descripcion,puntos DESC,p.nombre")

(def pdf-sql
  "SELECT
  s.descripcion as categoria,
  p.no_participacion,
  p.nombre,
  p.equipo,
  p.telefono,
  p.email,
  p.tutor
  FROM cartas p
  JOIN categorias s on s.id = p.categoria
  WHERE p.id = ?")

(defn build-body-p1 [carreras-row]
  (str "El que suscribe, por mi propio derecho, expresamente manifiesto que es mi deseo participar en el evento denominado
  \"" (:descripcion carreras-row) "\" ," (:donde carreras-row) ". Así mismo me comprometo y obligo a no ingresar cualquier
  Área RESTRINGIDA(S) (entendida como aquella que requiera la autorización expresa mediante la expedición de
  credencial o permiso por parte del comité organizador, en razón de lo anterior al firmar el presente escrito acepto todos y
  cada uno de los términos y condiciones estipulados en el presente escrito:"))

(defn build-body-p2 [row carreras-row]
  (str "Yo " (:nombre row) ", Con el número de participación " (:no_participacion row) " por el solo hecho de firmar
este documento, accepto cualquier y todos los riesgos y peligros que sobre mi persona recaigan en cuanto a mi participación
en Evento ciclista denominado \"" (:descripcion carreras-row) "\". Por lo tanto, yo soy el único responsable de mi salud.
cualquier consecuencia, accidentes, perjuicios, deficiencias que puedan causar, de cualquier manera, posibles alteraciones
a mi salud, integridad fisica, o inclusive muerte. Por esa razón libero de cualquier responsabilidad al respecto al Comité
Organizador de dicho Evento y/o a las Asociaciones que lo integran, asi como a sus directores, patrocinadores y
representantes, y por medio de este conducto renuncio, sin limitación alguna a cualquier derecho, demanda o
indemnización al respecto. Reconozco y acepto que Comité Organizador del Evento \"" (:descripcion carreras-row) \" "
no son ni serán consideradas responsables por cualquier desperfecto, pérdida o robo relacionados con mis pertenencias
personales."))

(defn build-blank-p2 [carreras-row]
  (str "Yo _______________, Con el número de participación _____ por el solo hecho de firmar
este documento, accepto cualquier y todos los riesgos y peligros que sobre mi persona recaigan en cuanto a mi participación
en Evento ciclista denominado \"" (:descripcion carreras-row) "\". Por lo tanto, yo soy el único responsable de mi salud.
cualquier consecuencia, accidentes, perjuicios, deficiencias que puedan causar, de cualquier manera, posibles alteraciones
a mi salud, integridad fisica, o inclusive muerte. Por esa razón libero de cualquier responsabilidad al respecto al Comité
Organizador de dicho Evento y/o a las Asociaciones que lo integran, asi como a sus directores, patrocinadores y
representantes, y por medio de este conducto renuncio, sin limitación alguna a cualquier derecho, demanda o
indemnización al respecto. Reconozco y acepto que Comité Organizador del Evento \"" (:descripcion carreras-row) \" "
no son ni serán consideradas responsables por cualquier desperfecto, pérdida o robo relacionados con mis pertenencias
personales."))

(defn build-body-p3 [carreras-row]
  (str "También reconozco y acepto que como participante del EVENTO \"" (:descripcion carreras-row) "\", deberé portar en
  todo momento el número de participante proporcionado por los organizadores del EVENTO, en el entendido que dicho
  número no podrán ser transferidas o intercambiadas con cualquier tercero bajo ningún concepto, por lo que si no cuento
  con la misma, los organizadores del EVENTO, podrán retirarme del mismo, leberándolos de toda responsabilidad, asi como
  renunciando a ejercer cualquier acción legal en su contra por las acciones tomadas a este respecto."))

(defn build-body-p4 [carreras-row]
  (str "Así mismo, autorizo al comité organizador del evento \"" (:descripcion carreras-row) "\", y/o a quien ésta designe, el uso
  de mi imagen y voz, ya sea parcial o totalmente, en cuanto a todo lo relacionado en el Evento, de cualquier manera y en
  cualquier momento. Por este conducto reconozco que sé y entiendo todas las regulaciones del Evento, incluyendo y sin
  limitarse al reglamento de competencia expedido por el Comité Organizador. Igualmente, manifiesto bajo protesta de decir
  verdad que mi equipo de competencia es obligatorio su uso en la competencia (Bicicleta, guantes, casco, zapatos,
  zapatillas, pedales, cadena etc.) los cuales reúnen y cumple con todos los requisitos reglamentarios aplicables, sin
  perjuicios de la facultad que se tenga para revisar su bicicleta y los demás establecidos en lo mencionado en este
  documento."))

(defn carrera-reporte-sql [carreras_id categories]
  (str "SELECT
   categorias.descripcion,
   cartas.no_participacion,
   cartas.nombre,
   cartas.equipo
   FROM cartas
   LEFT JOIN categorias on categorias.id = cartas.categoria
   WHERE
   categoria IN(" categories ")
   AND carreras_id = " carreras_id "
   ORDER BY categorias.descripcion,nombre"))
