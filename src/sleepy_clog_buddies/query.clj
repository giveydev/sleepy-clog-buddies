(ns sleepy-clog-buddies.query
  (:use environ.core)
  (:use cheshire.core)
  (:use ring.util.response)
  (:use clojure.walk)
  (:require [clojurewerkz.neocons.rest.cypher :as cypher]
            [sleepy-clog-buddies.shared :as scb-shared]))

(defn construct-start []
  (let
    [attrs sleepy-clog-buddies.query/attrs 
     start-str
      (cond
        (contains? attrs "db_id")
          (str ":" (scb-shared/nodetypes (get attrs "nodetype")) "('db_id:" (get attrs "db_id") "')")
        (contains? attrs "id")
          (str "(" (get attrs "id") ")")
        (contains? attrs "nodetype")
          (str ":" (scb-shared/nodetypes (get attrs "nodetype")) "('*:*')")
        :else "(*)")]
    (println start-str)
    (str "START n=node" start-str)))

(defn construct-return []
  (str "RETURN n as info, ID(n) as id;"))

(defn construct-query-string []
  (println scb-shared/nodetypes)
  (str (construct-start) " " (construct-return)))

(defn cypher-query [attributes]
  (def attrs attributes)
  (scb-shared/neoconnect)
  (let
    [nodes 
      (let [query-string (construct-query-string)]
        (println query-string)
        (cypher/tquery query-string))]
    (response (map #(assoc (:data (get % "info")) "id" (get % "id")) nodes))))

