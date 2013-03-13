(ns sleepy-clog-buddies.handler
  (:use compojure.core)
  (:use cheshire.core)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]
            [clojurewerkz.neocons.rest :as neorest]
            [clojurewerkz.neocons.rest.nodes :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships]
            [clojurewerkz.neocons.rest.cypher :as cypher]))

(defn get-all-users []
  (neorest/connect! "http://localhost:7474/db/data/")
  (let
    [res (cypher/tquery "START x = node({ids}) RETURN x.userid, x.name" {:ids [1 2]})]
    (response res) )
  )

(defroutes app-routes
  (context "/users" [] (defroutes users-routes
    (GET "/" [] (get-all-users))))
  (GET "/" [] (response {"listen" "shh"}))
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))