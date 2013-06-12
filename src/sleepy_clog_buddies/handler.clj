(ns sleepy-clog-buddies.handler
  (:use environ.core)
  (:use compojure.core)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]
            [sleepy-clog-buddies.node :as scb-node]))
  (doseq [nodetype '("users" "charities")]
    (println nodetype))

(defroutes app-routes
  (context "/nodes/:id" [id] (defroutes node-routes
    (GET "/" [] (scb-node/get-node id))
    (PUT    "/" {body :body} (scb-node/update-node id body))
    (DELETE "/" [] (scb-node/delete-node id))))
  (context "/users" [] (defroutes users-routes
    (GET "/" [] (scb-node/get-all-nodes-of-type "user"))
    (POST "/" {body :body} (scb-node/create-node "user" body))))
  (GET "/" [] (response {"listen" "shh"}))
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))
