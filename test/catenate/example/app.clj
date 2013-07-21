(ns catenate.example.app
  (:require [catenate.core :as catenate]
            [catenate.hiccup]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]
            [ring.middleware.content-type]
            [hiccup.core :refer [html]]))

(defn render-index
  [request]
  (str "<head>"
       (apply str (map #(str "<link href=\"" % "\" rel=\"stylesheet\" />")
                       (get-in request [:catenate :urls "styles.css"])))
       "</head>"
       "<body>"
       "<h1>Example app</h1>"
       (apply str (map #(str "<script src=\"" % "\"></script>")
                       (catenate/urls request ["lib.js" "app.js"])))
       "</body>"))

(defn render-index-w-hiccup
  [request]
  (html
   [:head
    (catenate.hiccup/link-to-css request ["styles.css"])]
   [:body
    [:h1 "Example app"]
    (catenate.hiccup/link-to-js request ["lib.js" "app.js"])]))

(defroutes app-routes
  (GET "/" [:as request] (render-index request))
  (GET "/hiccup" [:as request] (render-index-w-hiccup request))
  (route/not-found "<h1>Page not found</h1>"))

(defn create-app
  [env]
  (-> app-routes
      (catenate/wrap
       :env env
       :bundles {"lib.js" [(catenate/resource "public/some.js")]
                 "app.js" (catenate/resources ["public/cool.js"
                                               "public/code.js"])
                 "styles.css" (catenate/files ["test/files/styles/reset.css"
                                               "test/files/styles/base.css"])})
      (ring.middleware.content-type/wrap-content-type)))

(def app-dev (create-app :development))
(def app-prod (create-app :production))
