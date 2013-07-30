(ns catenate.example.app
  (:require [catenate.core :as catenate :refer [with-prefix]]
            [catenate.file-struct]
            [catenate.hiccup]
            [catenate.angular]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]
            [ring.middleware.content-type]
            [hiccup.core :refer [html]]))

(defn render-index
  [request]
  (str "<head>"
       (apply str (map #(str "<link href=\"" % "\" rel=\"stylesheet\" />")
                       (catenate/bundle-urls request ["styles.css"])))
       "<link href=\"" (catenate/file-url request "/styles/login.css") "\" rel=\"stylesheet\" />"
       "</head>"
       "<body>"
       "<h1>Example app</h1>"
       (apply str (map #(str "<script src=\"" % "\"></script>")
                       (catenate/bundle-urls request ["lib.js" "app.js"])))
       "<script src=\"" (catenate/file-url request "/angular-templates.js") "\"></script>"
       "</body>"))

(defn render-index-w-hiccup
  [request]
  (html
   [:head
    (catenate.hiccup/link-to-css-bundles request ["styles.css"])
    (catenate.hiccup/link-to-css-file request "/styles/login.css")]
   [:body
    [:h1 "Example app"]
    (catenate.hiccup/link-to-js-bundles request ["lib.js" "app.js"])
    (catenate.hiccup/link-to-js-file request "/angular-templates.js")]))

(defroutes app-routes
  (GET "/" [:as request] (render-index request))
  (GET "/hiccup" [:as request] (render-index-w-hiccup request))
  (route/not-found "<h1>Page not found</h1>"))

(def bundles
  {"lib.js" ["/scripts/some.js"]
   "app.js" ["/scripts/cool.js"
             "/scripts/code.js"]
   "styles.css" (with-prefix "/styles/"
                  ["reset.css"
                   "base.css"])})

(defn create-app
  [env]
  (-> app-routes
      (catenate/wrap
       :bundles bundles
       :extra-files (conj
                     (catenate/paths->files "public" ["/styles/login.css"
                                                      "/scripts/more.js"])
                     (catenate.angular/create-template-cache
                      :path "/angular-templates.js"
                      :module "myapp"
                      :public-dir "public"
                      :templates ["/templates/simple.html"
                                  "/templates/multiple/one.html"
                                  "/templates/multiple/two/two.html"]))
       :debug (= :development env)
       :public-dir "public")
      (ring.middleware.content-type/wrap-content-type)))

(def app-dev (create-app :development))
(def app-prod (create-app :production))
