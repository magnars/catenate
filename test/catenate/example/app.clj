(ns catenate.example.app
  (:require [catenate.core :as catenate]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]
            [hiccup.core :refer [html]]))

(defn render-index
  [request]
  (html [:h1 "Example app"]))

(defroutes app-routes
  (GET "/" [:as request] (render-index request))

   (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> app-routes
      (catenate/wrap
       :env :production
       :bundles {})))
