(ns sleepy-clog-buddies.relationship
  (:use environ.core)
  (:use cheshire.core)
  (:use ring.util.response)
  (:use clojure.walk)
  (:require [clojurewerkz.neocons.rest.nodes :as nodes]
            [clojurewerkz.neocons.rest.relationships :as rels]
            [clojurewerkz.neocons.rest.cypher :as cypher]
            [sleepy-clog-buddies.shared :as scb-shared]))

(defn getid [urlstr] (last (clojure.string/split urlstr #"\/")))

(defn relresponse [rel] (assoc (:data rel)
                        "id" (:id rel) "type" (:type rel)
                        "start" (getid (:start rel)) "end" (getid (:end rel))))

(defn get-relationship [id]
  (scb-shared/neoconnect)
  (let 
    [rel 
      (try
        (rels/get (read-string id))
        (catch Exception e ["relationship_not_found"]))]
    (cond
      (= rel ["relationship_not_found"]) {:status 404 :body rel}
      :else (response (relresponse rel)))))

(defn create-relationship [fromid toid reltype attributes]
  (scb-shared/neoconnect)
  (let [
    from (nodes/get (read-string fromid))
    to (nodes/get (read-string toid))
    db_ids (str (:db_id (:data from)) "-" (:db_id (:data to)))
    ; arguments: from node, to node, relationship type, index name, key name, value (combinaiton of db_ids, then properties
    rel (rels/create-unique-in-index from to reltype reltype "db_ids" db_ids attributes)]
    (get-relationship (str (:id rel)))))

(defn update-relationship [id attributes]
  (scb-shared/neoconnect)
  (let
    [relationship-attributes (merge (:data (rels/get (read-string id))) (keywordize-keys attributes))]
    (rels/update (read-string id) relationship-attributes)
    (get-relationship id)))

(defn delete-relationship [id] 
  (scb-shared/neoconnect)
  (rels/delete (read-string id))
  {:status 204})
