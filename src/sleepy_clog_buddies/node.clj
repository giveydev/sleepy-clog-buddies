(ns sleepy-clog-buddies.node
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
    [nodes (cypher/tquery (str "START n=node(*) WHERE n.nodetype = \"" nodetype "\" RETURN n as info, ID(n) as id;"))]
    (response (map #(assoc (:data (get % "info")) "id" (get % "id")) nodes))))

(defn get-node [id]
  (neoconnect)
  (let 
    [node 
      (try
        (nodes/get (read-string id))
        (catch Exception e ["node_not_found"]))]
    (cond
      (= node ["node_not_found"]) {:status 404 :body node}
      :else (response (assoc (:data node) "id" (:id node))))))

(defn create-node [nodetype attributes]
  (neoconnect)
  (let [node
    (let [node-attributes (assoc attributes "nodetype" nodetype)]
      (nodes/create node-attributes))]
    (get-node (str (:id node)))))

; need to handle not found nodes
(defn update-node [id attributes]
  (neoconnect)
  (let
    [node-attributes (merge (:data (nodes/get (read-string id))) (keywordize-keys attributes))]
    (nodes/update (read-string id) node-attributes)
    (get-node id)))

; need to handle not found nodes
(defn delete-node [id] 
  (neoconnect)
  (nodes/delete (read-string id))
  {:status 204})
