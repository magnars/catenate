(ns catenate.example.app
  (:require [catenate.core :as catenate]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]
            [ring.middleware.content-type]
            [hiccup.core :refer [html]]))

(defn render-index
  [request]
  (html
   [:body
    [:h1 "Example app"]
    (map (fn [url] [:script {:src url}])
         (get-in request [:catenate :urls "app.js"]))]))

(defroutes app-routes
  (GET "/" [:as request] (render-index request))

   (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> app-routes
      (catenate/wrap
       :env :development
       :bundles {"app.js" [(catenate/resource "public/cool.js")
                           (catenate/resource "public/code.js")]})
      (ring.middleware.content-type/wrap-content-type)))
