(ns sleepy-clog-buddies.handler
  (:use environ.core)
  (:use compojure.core)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]
            [sleepy-clog-buddies.node :as scb-node]
            [sleepy-clog-buddies.relationship :as scb-rel]))
  ;(doseq [nodetype '("users" "charities")]
  ;  (println nodetype))

(defroutes app-routes

  (POST "/create_relationship/:fromid/:reltype/:toid"
    {{fromid :fromid toid :toid reltype :reltype} :params body :body} 
    (scb-rel/create-relationship fromid toid reltype body))

  (context "/relationships/:id" [id] (defroutes relationship-routes
    (GET "/" [] (scb-rel/get-relationship id))))

  (context "/nodes/:id" [id] (defroutes node-routes
    (GET "/" [] (scb-node/get-node id))
    (PUT    "/" {body :body} (scb-node/update-node id body))
    (DELETE "/" [] (scb-node/delete-node id))))
           
  (context "/users" [] (defroutes users-routes
    (GET "/" [] (scb-node/get-all-nodes-of-type "user"))
    (POST "/" {body :body} (scb-node/create-node "user" body))))

  (context "/charities" [] (defroutes users-routes
    (GET "/" [] (scb-node/get-all-nodes-of-type "charity"))
    (POST "/" {body :body} (scb-node/create-node "charity" body))))

  (GET "/" [] (response {"listen" "shh"}))
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))
