(ns sleepy-clog-buddies.node
  (:use environ.core)
  (:use cheshire.core)
  (:use ring.util.response)
  (:use clojure.walk)
  (:require [clojurewerkz.neocons.rest.nodes :as nodes]
            [clojurewerkz.neocons.rest.relationships :as rels]
            [clojurewerkz.neocons.rest.cypher :as cypher]
            [sleepy-clog-buddies.shared :as scb-shared]))

(defn get-all-nodes-of-type [nodetype]
  (scb-shared/neoconnect)
  (let
    [nodes (cypher/tquery (str "START n=node:" nodetype "('*:*') RETURN n as info, ID(n) as id;"))]
    (response (map #(assoc (:data (get % "info")) "id" (get % "id")) nodes))))

(defn get-node [id]
  (scb-shared/neoconnect)
  (let 
    [node 
      (try
        (nodes/get (read-string id))
        (catch Exception e ["node_not_found"]))]
    (cond
      (= node ["node_not_found"]) {:status 404 :body node}
      :else (response (assoc (:data node) "id" (:id node))))))

(defn create-node [nodetype attributes]
  (scb-shared/neoconnect)
  (let [node
    (let [node-attributes (assoc attributes "nodetype" nodetype)]
      (nodes/create-unique-in-index nodetype "db_id" (get attributes "db_id") node-attributes))]
    (get-node (str (:id node)))))

; need to handle not found nodes
(defn update-node [id attributes]
  (scb-shared/neoconnect)
  (let
    [node-attributes (merge (:data (nodes/get (read-string id))) (keywordize-keys attributes))]
    (nodes/update (read-string id) node-attributes)
    (get-node id)))

; need to handle not found nodes
(defn delete-node [id] 
  (scb-shared/neoconnect)
  (nodes/delete (read-string id))
  {:status 204})
