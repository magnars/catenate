# catenate

A Ring middleware that:

 - in production: serves concatenated files with cache buster URLs
 - in development: serves files individually

In other words, develop with ease, and cache aggressively in production.

## Usage

### Set up as middleware

```cl
(require '[clojure.java.io :as io])
(require '[catenate.core :as catenate])

(-> app
    (catenate/wrap
     :env :production ;; 1
     :bundles {"lib.js" [["external/jquery.js" #(slurp "http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js")] ;; 2
                         ["external/angular.js" (slurp "http://ajax.googleapis.com/ajax/libs/angularjs/1.0.4/angular.js")] ;; 3
                         ["resources/public/lib/lodash.js" slurp] ;; 4
                         ["public/lib/moment.js" #(slurp (io/resource %))]] ;; 5
               "app.js" (catenate/resources ["public/app/some.js" ;; 6
                                             "public/app/cool.js"
                                             "public/app/code.js"])
               "styles.css" (catenate/files ;; 7
                             (distinct ;; 8
                              (concat
                               ["theme/css/reset.css"
                                "theme/css/base.css"]
                               (catenate/css-files-in "theme/css"))))})) ;; 9
```

1. `:env :production` means concatenate and add cache busters, while
   `:env :development` just passes the files through unscathed. Use your
   environment variables of choice here.

2. `:bundles` is a map from package name to a list of tuples. The tuples
   contain identifiers and a function that returns the contents.

3. Instead of a function, the contents can be returned directly. One
   use case is fetching external resources only once, even in
   development mode.

4. If the function has an arity > 1, the identifier is passed to it.

5. Using `io/resource` we don't have to access the file system directly.

6. catenate offers some sugar to create a list of tuples for resources.

7. catenate offers sugar for files too.

8. Let's make sure we get `reset.css` and `base.css` first, but
   only include them once.

9. Some sugar to list out css-files in a directory, it is equivalent to:

        (->> (io/file "theme/css")
             .listFiles
             (map #(.getPath %))
             (filter #(.endsWith % ".css")))

### Using the new URLs

After setting up the middleware, the URLs are added to the request map
under `:catenate :urls`.

See example in hiccup below. Notice that we use `map`, since there is
likely more than one URL in development mode.

```cl
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

There's some sugar for that last `concat`:

```cl
(catenate/urls request ["lib.js" "app.js"])
```

Heck, there's even some hiccup-specific sugar:

```cl
(defn my-page
  [request]
  (hiccup.core/html
   [:html
    [:head
     (catenate.hiccup/link-to "styles.css")]
    [:body
     (map catenate.hiccup/link-to ["lib.js" "app.js"])]]))
```

## But how about ...

 - **Caching?**

   In development, you don't want caching. In production, you serve
   static files from [Nginx](http://nginx.org/) or
   [Varnish](https://www.varnish-cache.org/).

 - **Minification?**

   This middleware doesn't concern itself with minification, but there
   are options:

   - since the configuration takes tuples of filenames and content-producing
   functions, adding minification can be done there.

   - minification can be added as another middleware.

 - **Compiling?**

   Maybe you're looking for a full-fledged asset pipeline? This isn't
   that. Check out [Dieter](https://github.com/edgecase/dieter).

## License

Copyright © 2013 Magnar Sveen

Distributed under the Eclipse Public License, the same as Clojure.
