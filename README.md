# catenate

A Ring middleware to concatenate files and add cache busters to their URLs.

## Usage

```cl
(require '[clojure.java.io :as io])
(require '[no.magnars.catenate :as catenate])

;; As middleware to your route:

(-> app
    (catenate/wrap :development
                   {"lib.js" [(io/as-url "http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js")
                              (io/as-url "http://ajax.googleapis.com/ajax/libs/angularjs/1.0.4/angular.js")
                              (io/resource "public/lib/underscore.js")]
                    "app.js" (map io/resource
                                  ["public/app/some.js"
                                   "public/app/cool.js"
                                   "public/app/code.js"])
                    "styles.css" (map io/file
                                      ["theme/css/reset.css"
                                       "theme/css/main.css"])}))

;; In your hiccup template (if you are so inclined):

(defn my-page
  [request]
  (hiccup.core/html
   [:html
    [:head
     (map (fn [url] [:link {:rel "stylesheet" :href url}])
          (get-in request [:catenate :urls "styles.css"]))]
    [:body
     (map (fn [url] [:script {:src url}])
          (concat
           (get-in request [:catenate :urls "lib.js"])
           (get-in request [:catenate :urls "app.js"])))]]))
```

## But how about ...

 - **Caching?**

   In development, you don't want caching. In production, you serve
   cached files from [Nginx](http://nginx.org/) or
   [Varnish](https://www.varnish-cache.org/).

 - **Minification? Compiling?**

   Maybe you're looking for a full-fledged asset pipeline? This isn't
   that. Check out [Dieter](https://github.com/edgecase/dieter).

## License

Copyright © 2013 Magnar Sveen

Distributed under the Eclipse Public License, the same as Clojure.
