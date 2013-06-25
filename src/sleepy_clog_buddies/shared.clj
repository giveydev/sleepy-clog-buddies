(ns sleepy-clog-buddies.shared
  (:use environ.core)
  (:require [clojurewerkz.neocons.rest :as neorest]))

(defn neoconnect []
  (neorest/connect! (env :neo4j-url)))

; Define allow types of node here. This is the only validation this far - any data is allowed
(def nodetypes
  {"users"      "user"
   "charities"  "charity"
   "moments"    "moment"
   "businesses" "business"})
