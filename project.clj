(defproject bookthing "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-core "1.6.3"]
                 [hiccup "1.0.5"]
                 [mount "0.1.12"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [migratus "1.0.6"]
                 [buddy/buddy-hashers "1.3.0"]
                 [org.postgresql/postgresql "42.2.2"]
                 [org.clojure/java.jdbc "0.7.6"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler bookthing.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
