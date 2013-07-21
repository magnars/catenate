(ns catenate.example.app
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]
            [hiccup.core :refer [html]]))

(defn render-index [request]
  (html [:h1 "Example app"]))

(defroutes route-handler
  (GET "/" [:as request] (render-index request))

  (route/not-found "<h1>Page not found</h1>"))

(defn app
  [request]
  (route-handler request))
