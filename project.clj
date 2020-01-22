(defproject ciclismo "0.1.0"
  :description "Ciclismo Mexicali"
  :url "https://github.com/hectorqlucero/ciclismo"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [clojure-csv/clojure-csv "2.0.2"]
                 [compojure "1.6.1" :exclusions [commons-codec]]
                 [lib-noir "0.9.9"]
                 [com.draines/postal "2.0.3"]
                 [cheshire "5.9.0"]
                 [clj-pdf "2.4.0" :exclusions [xalan commons-codec]]
                 [clj-jwt "0.1.1"]
                 [pdfkit-clj "0.1.7" :exclusions [commons-codec commons-logging]]
                 [clj-time "0.15.2"]
                 [date-clj "1.0.1"]
                 [clojurewerkz/money "1.10.0"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.clojure/data.codec "0.1.1"]
                 [mysql/mysql-connector-java "8.0.19"]
                 [selmer "1.12.18" :exclusions [commons-codec]]
                 [inflections "0.13.2" :exclusions [commons-codec]]
                 [ring/ring-devel "1.8.0" :exclusions [commons-codec ring/ring-codec]]
                 [ring/ring-core "1.8.0" :exclusions [commons-codec ring/ring-codec]]
                 [ring/ring-anti-forgery "1.3.0"]
                 [ring/ring-defaults "0.3.2"]]
  :main ^:skip-aot ciclismo.core
  :aot [ciclismo.core]
  :plugins [[lein-ancient "0.6.10"]
            [lein-pprint "1.1.2"]]
  :uberjar-name "cc.jar"
  :target-path "target/%s"
  :ring {:handler       cliclismo.core/app
         :auto-reload?  true
         :auto-refresh? false}
  :profiles {:uberjar {:aot :all}})
