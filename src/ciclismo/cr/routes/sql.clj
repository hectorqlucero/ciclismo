(ns ciclismo.cr.routes.sql)

(def results-sql
  "SELECT
   p.categorias_id as categorias_id,
   s0.descripcion as categoria,
   p.carreras_id as carreras_id,
   s1.distancia as distancia,
   s.no_participacion as numero,
   s.nombre as nombre,
   TIME_FORMAT(p.empezar, '%H:%i:%s') as empezar,
   TIME_FORMAT(p.terminar, '%H:%i:%s') as terminar,
   TIME_FORMAT(SUBTIME(p.terminar,p.empezar),'%H:%i:%s') as result,
   TIME_TO_SEC(SUBTIME(p.terminar,p.empezar)) as seconds
   FROM contrareloj p
   JOIN cartas s on s.id = p.cartas_id
   JOIN categorias s0 on s0.id = p.categorias_id
   JOIN carreras s1 on s1.id = p.carreras_id
   WHERE p.empezar IS NOT NULL
   AND p.terminar IS NOT NULL
   AND p.carreras_id = ?
   ORDER BY s0.descripcion,result")

(def cresults-sql
  "SELECT
   s0.descripcion as categoria,
   p.carreras_id as carreras_id,
   s1.distancia as distancia,
   s.no_participacion as numero,
   s.nombre as nombre,
   TIME_FORMAT(p.empezar, '%H:%i:%s') as empezar,
   TIME_FORMAT(p.terminar, '%H:%i:%s') as terminar,
   TIME_FORMAT(SUBTIME(p.terminar,p.empezar),'%H:%i:%s') as result,
   TIME_TO_SEC(SUBTIME(p.terminar,p.empezar)) as seconds
   FROM contrareloj p
   JOIN cartas s on s.id = p.cartas_id
   JOIN categorias s0 on s0.id = p.categorias_id
   JOIN carreras s1 on s1.id = p.carreras_id
   WHERE p.empezar IS NOT NULL
   AND p.terminar IS NOT NULL
   AND p.carreras_id = ?
   AND p.categorias_id = ?
   ORDER BY result")

(def oresults-sql
  "SELECT
   s0.descripcion as categoria,
   p.carreras_id as carreras_id,
   s1.distancia as distancia,
   s.no_participacion as numero,
   s.nombre as nombre,
   TIME_FORMAT(p.empezar, '%H:%i:%s') as empezar,
   TIME_FORMAT(p.terminar, '%H:%i:%s') as terminar,
   TIME_FORMAT(SUBTIME(p.terminar,p.empezar),'%H:%i:%s') as result,
   TIME_TO_SEC(SUBTIME(p.terminar,p.empezar)) as seconds
   FROM contrareloj p
   JOIN cartas s on s.id = p.cartas_id
   JOIN categorias s0 on s0.id = p.categorias_id
   JOIN carreras s1 on s1.id = p.carreras_id
   WHERE p.empezar IS NOT NULL
   AND p.terminar IS NOT NULL
   AND p.carreras_id = ?
   ORDER BY result")
