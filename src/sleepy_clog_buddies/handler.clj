(ns sleepy-clog-buddies.handler
  (:use environ.core)
  (:use compojure.core)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]
            [sleepy-clog-buddies.user :as scb-user]))

(defroutes app-routes
  (context "/users" [] (defroutes users-routes
    (GET "/" [] (scb-user/get-all-users))
    (POST "/" {body :body} (scb-user/create-new-user body))
    (context "/:id" [id] (defroutes user-routes
      (GET "/" [] (scb-user/get-user id))
      (PUT    "/" {body :body} (scb-user/update-user id body))
      (DELETE "/" [] (scb-user/delete-user id))))))
  (GET "/" [] (response {"listen" (env :neo4j-url)}))
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))
