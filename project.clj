(defproject sleepy-clog-buddies "0.1.0-SNAPSHOT"
  :description "A RESTful interface for Neo4J"
  :url "http://www.givey.com"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.5"]
                 [ring/ring-json "0.2.0"]
                 [cheshire "5.0.2"]
                 [clojurewerkz/neocons "1.0.3"]]
  :plugins [[lein-ring "0.8.2"]]
  :ring {:handler sleepy-clog-buddies.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
