(ns sleepy-clog-buddies.query
  (:use environ.core)
  (:use cheshire.core)
  (:use ring.util.response)
  (:use clojure.walk)
  (:require [clojurewerkz.neocons.rest.cypher :as cypher]
            [sleepy-clog-buddies.shared :as scb-shared]))

(defn construct-start [attrs]
  (let
    [start-str
      (cond
        (contains? attrs "db_id")
          (str ":" (scb-shared/nodetypes (get attrs "nodetype")) "('db_id:" (get attrs "db_id") "')")
        (contains? attrs "id")
          (str "(" (get attrs "id") ")")
        (contains? attrs "nodetype")
          (str ":" (scb-shared/nodetypes (get attrs "nodetype")) "('*:*')")
        :else "(*)")]
    (str "START n=node" start-str)))

(defn construct-match [attrs]
  nil)

; send full match string
; e.g., {"id":71960, "match": "(n)-[:DONATESTO]->(x)<-[:DONATESTO]-(o)", "match_return" : "distinct o as info, ID(o) as id" }
(defn construct-match-simple [attrs]
  (cond
    (contains? attrs "match")
      (str "MATCH " (get attrs "match"))
    :else nil))

; this works with strings only, with a LIKE "this%" type query
(defn where-string-sections [where-field-values]
  (map #(str "n." (get % "field") "! =~ '(?i)" (get % "value") ".*'") where-field-values))

; send criteria to search on
; currently disabled - using construct-where-simple instead
; currently only handles string type searches
; e.g., {"nodetype":"users", "where": [ {"field":"first_name", "value":"n"},  {"field":"last_name", "value":"r"} ] }
(defn construct-where [attrs]
  (cond
    (contains? attrs "where")
      (let
        [where-list (where-string-sections (get attrs "where"))]
        (str "WHERE " (clojure.string/join " and " where-list)))
    :else nil))

; send full where string
; e.g., {"nodetype":"users", "where": "n.first_name! =~ '(?i)n.*'" }
(defn construct-where-simple [attrs]
  (cond
    (contains? attrs "where")
      (str "WHERE " (get attrs "where"))
    :else nil))

; e.g., {"id":71960, "match": "(n)-[:DONATESTO]->(x)<-[:DONATESTO]-(o)", "match_return" : "distinct o as info, ID(o) as id" }
(defn construct-return [attrs]
  (cond
    (contains? attrs "match_return")
      (str "RETURN " (get attrs "match_return"))
    :else (str "RETURN n as info, ID(n) as id;")))

(defn construct-query-string [attrs]
  (println scb-shared/nodetypes)
  (let
    [query-string-components
      (list
        (construct-start attrs)
        (construct-match-simple attrs)
        (construct-where-simple attrs)
        (construct-return attrs))]
    (clojure.string/join " " (remove nil? query-string-components))))

(defn cypher-query [attrs]
  (scb-shared/neoconnect)
  (let
    [nodes 
      (let [query-string (construct-query-string attrs)]
        (println query-string)
        (cypher/tquery query-string))]
    (response (map #(assoc (:data (get % "info")) "id" (get % "id")) nodes))))

