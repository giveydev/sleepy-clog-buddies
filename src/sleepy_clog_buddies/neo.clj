(ns sleepy-clog-buddies.neo
  (:use environ.core)
  (:require [clojurewerkz.neocons.rest :as neorest]))

(defn neoconnect []
  (neorest/connect! (env :neo4j-url)))
