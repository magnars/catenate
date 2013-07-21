(ns catenate.example.app
  (:require [catenate.core :as catenate]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]
            [ring.middleware.content-type]
            [hiccup.core :refer [html]]))

(defn render-index
  [request]
  (html
   [:head
    (map (fn [url] [:link {:rel "stylesheet" :href url}])
         (get-in request [:catenate :urls "styles.css"]))]
   [:body
    [:h1 "Example app"]
    (map (fn [url] [:script {:src url}])
         (catenate/urls request ["lib.js" "app.js"]))]))

(defroutes app-routes
  (GET "/" [:as request] (render-index request))
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> app-routes
      (catenate/wrap
       :env :development
       :bundles {"lib.js" [(catenate/resource "public/some.js")]
                 "app.js" (catenate/resources ["public/cool.js"
                                               "public/code.js"])
                 "styles.css" (catenate/files ["test/files/styles/reset.css"
                                               "test/files/styles/base.css"])})
      (ring.middleware.content-type/wrap-content-type)))
