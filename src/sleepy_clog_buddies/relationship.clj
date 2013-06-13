(ns sleepy-clog-buddies.relationship
  (:use environ.core)
  (:use cheshire.core)
  (:use ring.util.response)
  (:use clojure.walk)
  (:require [clojurewerkz.neocons.rest :as neorest]
            [clojurewerkz.neocons.rest.nodes :as nodes]
            [clojurewerkz.neocons.rest.relationships :as rels]
            [clojurewerkz.neocons.rest.cypher :as cypher]))

(defn getid [urlstr] (last (clojure.string/split urlstr #"\/")))

(defn relresponse [rel] (assoc (:data rel)
                        "id" (:id rel) "type" (:type rel)
                        "start" (getid (:start rel)) "end" (getid (:end rel))))

(defn neoconnect []
  (neorest/connect! (env :neo4j-url)))

(defn get-relationship [id]
  (neoconnect)
  (let 
    [rel 
      (try
        (rels/get (read-string id))
        (catch Exception e ["relationship_not_found"]))]
    (cond
      (= rel ["relationship_not_found"]) {:status 404 :body rel}
      :else (response (relresponse rel)))))

(defn create-relationship [fromid toid reltype attributes]
  (neoconnect)
  (let [
    from (nodes/get (read-string fromid))
    to (nodes/get (read-string toid))
    bson_ids (str (:bson_id (:data from)) "-" (:bson_id (:data to)))
    ; arguments: from node, to node, relationship type, index name, key name, value (combinaiton of bson_ids, then properties
    rel (rels/create-unique-in-index from to reltype reltype "bson_ids" bson_ids attributes)]
    (get-relationship (str (:id rel)))))

(defn update-relationship [id attributes]
  (neoconnect)
  (let
    [relationship-attributes (merge (:data (rels/get (read-string id))) (keywordize-keys attributes))]
    (rels/update (read-string id) relationship-attributes)
    (get-relationship id)))

(defn delete-relationship [id] 
  (neoconnect)
  (rels/delete (read-string id))
  {:status 204})
