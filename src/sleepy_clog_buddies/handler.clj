(ns sleepy-clog-buddies.handler
  (:use environ.core)
  (:use compojure.core)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]
            [sleepy-clog-buddies.node :as scb-node]
            [sleepy-clog-buddies.relationship :as scb-rel]))

; Define allow types of node here. This is the only validation this far - any data is allowed
(def nodetypes
  {"users"      "user"
   "charities"  "charity"
   "moments"    "moment"
   "businesses" "business"})
(def noderoutes (re-pattern (clojure.string/join "|" (keys nodetypes))))

(defroutes app-routes

  (POST "/create_relationship/:fromid/:reltype/:toid"
    {{fromid :fromid toid :toid reltype :reltype} :params body :body} 
    (scb-rel/create-relationship fromid toid reltype body))

  (context "/relationships/:id" [id] (defroutes relationship-routes
    (GET "/" [] (scb-rel/get-relationship id))
    (PUT    "/" {body :body} (scb-rel/update-relationship id body))
    (DELETE "/" [] (scb-rel/delete-relationship id))))

  (context "/nodes/:id" [id] (defroutes node-routes
    (GET "/" [] (scb-node/get-node id))
    (PUT    "/" {body :body} (scb-node/update-node id body))
    (DELETE "/" [] (scb-node/delete-node id))))
           
  (context ["/:nodetype", :nodetype noderoutes] [nodetype] (defroutes nodetype-routes
    (GET "/" [] (scb-node/get-all-nodes-of-type (nodetypes nodetype)))
    (POST "/" {body :body} (scb-node/create-node (nodetypes nodetype) body))))

  (GET "/" [] (response {"listen" "shh"}))
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))
