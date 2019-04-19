(defproject ciclismo "0.1.0"
  :description "Ciclismo Mexicali"
  :url "http://0.0.0.0"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [clojure-csv/clojure-csv "2.0.2"]
                 [compojure "1.6.1" :exclusions [commons-codec]]
                 [lib-noir "0.9.9"]
                 [com.draines/postal "2.0.3"]
                 [cheshire "5.8.1"]
                 [clj-pdf "2.3.3" :exclusions [xalan commons-codec]]
                 [pdfkit-clj "0.1.7" :exclusions [commons-codec commons-logging]]
                 [clj-time "0.15.1"]
                 [date-clj "1.0.1"]
                 [clojurewerkz/money "1.10.0"]
                 [org.clojure/java.jdbc "0.7.9"]
                 [org.clojure/data.codec "0.1.1"]
                 [mysql/mysql-connector-java "8.0.15"]
                 [selmer "1.12.12" :exclusions [commons-codec]]
                 [inflections "0.13.2" :exclusions [commons-codec]]
                 [ring/ring-devel "1.7.1" :exclusions [commons-codec ring/ring-codec]]
                 [ring/ring-core "1.7.1" :exclusions [commons-codec ring/ring-codec]]
                 [ring/ring-anti-forgery "1.3.0"]
                 [ring/ring-defaults "0.3.2"]]
  :main ^:skip-aot ciclismo.core
  :uberjar-name "cc.jar"
  :target-path "target/%s"
  :ring {:handler cliclismo.core/app
         :auto-reload? true
         :auto-refresh? true}
  :resources-paths ["shared" "resources"]
  :profiles {:uberjar {:aot :all}})
