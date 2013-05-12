(ns sleepy-clog-buddies.user
  (:use environ.core)
  (:use cheshire.core)
  (:use ring.util.response)
  (:use clojure.walk)
  (:require [clojurewerkz.neocons.rest :as neorest]
            [clojurewerkz.neocons.rest.nodes :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships]
            [clojurewerkz.neocons.rest.cypher :as cypher]))

(defn neoconnect []
  (neorest/connect! (env :neo4j-url)))

(defn get-all-nodes-of-type [nodetype]
  (neoconnect)
  (let
    [users (cypher/tquery (str "START n=node(*) WHERE n.nodetype = \"" nodetype "\" RETURN n as info, ID(n) as id;"))]
    (response (map #(assoc (:data (get % "info")) "id" (get % "id")) users))))

(defn get-user [id]
  (neoconnect)
  (let 
    [user 
      (try
        (nodes/get (read-string id))
        (catch Exception e ["user_not_found"]))]
    (cond
      (= user ["user_not_found"]) {:status 404 :body user}
      :else (response (assoc (:data user) "id" (:id user))))))

(defn create-new-user [attributes]
  (neoconnect)
  (let [user 
    (let [user-attributes (assoc attributes "nodetype" "user")]
      (nodes/create user-attributes))]
    (get-user (str (:id user)))))

; need to handle not found users
(defn update-user [id attributes]
  (neoconnect)
  (let
    [user-attributes (merge (:data (nodes/get (read-string id))) (keywordize-keys attributes))]
    (nodes/update (read-string id) user-attributes)
    (get-user id)))

; need to handle not found users
(defn delete-user [id] 
  (neoconnect)
  (nodes/delete (read-string id))
  {:status 204})
