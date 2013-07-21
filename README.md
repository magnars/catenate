# catenate

A Ring middleware that:

 - in production: serves concatenated files with cache buster URLs
 - in development: serves files individually

In other words: Develop with ease. Cache aggressively in production,
using
[far future Expires headers](http://developer.yahoo.com/performance/rules.html#expires)
for your static assets.

## Usage

### Set up as middleware

```cl
(require '[catenate.core :as catenate])

(-> app
 (catenate/wrap
  :env :production ;; 1
  :bundles {"lib.js" [(catenate/file "external/jquery.js") ;; 2
                      (catenate/file "external/angular.js") ;; 3
                      (catenate/resource "public/libs/moment.js")] ;; 4

            "app.js" (concat (catenate/resources ;; 5
                              ["public/app/some.js"
                               "public/app/cool.js"
                               "public/app/code.js"])
                             (catenate/files ;; 6
                              ["scripts/even.js"
                               "scripts/more.js"]))

            "styles.css" (catenate/distinct-files ;; 7
                          ["theme/css/reset.css"
                           "theme/css/base.css"
                           "theme/css/*.css"])})
 (ring.middleware.content-type/wrap-content-type)) ;; 8
```

1. `:env :production` concatenates and adds cache busters, while
   `:env :development` just passes the files through unscathed. Set up
   properly with environment variables of some kind.

2. `:bundles` is a map from bundle name to a list of files.
   `catenate/file` returns a data structure, and you may inspect it,
   of course. But this data structure isn't part of the public API
   before version 1.0.

3. The contents are concatenated together in the order specified in the
   bundle.

4. Bundles can also grab resources on the classpath instead of
   accessing the file system directly.

5. Using the `catenate/resources` sugar to include many resources in
   the bundle. Yeah, it's just a `map` of `catenate/resource` over the
   list. Too sweet?

6. There's sugar for files too.

7. More sugar. It includes `reset.css` and `base.css` first, and then
   skips over them when we get the rest of the CSS files in that
   folder.

   I'm sorry, but there's no `catenate/distinct-resources`, because
   globbing the classpath doesn't fill me with happy thoughts. If you
   want to tackle that problem, pull requests are welcome.

8. Since Ring comes with content type middleware, catenate doesn't
   worry about it. Just make sure to put it after catenate.

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
     (catenate.hiccup/link-to-css "styles.css")]
    [:body
     (map catenate.hiccup/link-to-js ["lib.js" "app.js"])]]))
```

## So how does this work in development mode?

The identifier in each tuple is used for the URL, prefixed with
`/catenate/`, so that in this example bundle:

```cl
{"app.js" (catenate/resources ["public/app/some.js"
                               "public/app/cool.js"
                               "public/app/code.js"])}
```

calling `(get-in request [:catenate :urls "app.js"])` returns

```cl
["/catenate/public/app/some.js"
 "/catenate/public/app/cool.js"
 "/catenate/public/app/code.js"]
```

and the middleware handles these URLs by returning the given contents.

## What about production mode?

All the contents for each bundle is read at startup. URLs are
generated from the hash of the contents and the identifier of the
bundle.

So when you call `(get-in request [:catenate :urls "app.js"])`, it now
returns:

```cl
["/catenate/d131dd02c5e6eec4/app.js"]
```

and the middleware returns the concatenated contents on this URL.

#### What if the contents have changed?

All the contents are read at startup, and then never checked again. To
read in new contents, the app has to be restarted.

#### No, I mean, what if someone requests an old version of app.js?

With a different hash? Yeah, then they get a 404. In production, you
should serve the files through [Nginx](http://nginx.org/) or
[Varnish](https://www.varnish-cache.org/) to avoid this problem while
doing rolling restarts of app servers.

#### Why not just ignore the hash and return the current contents?

Because then the user might be visiting an old app server with a new
URL, and suddenly he is caching stale contents. Or worse, your Nginx
or Varnish cache picks up on it and is now serving out old shit in a
new wrapping. Not cool.

This of course depends on how your machines are set up, and how you do
your rolling restarts, but trust me - it's been a major problem with
some other (to be unnamed) concatenation packages on the JVM.

#### Do I have to have "/catenate/" in front of the URLs in production?

No. Just pass in another `:context-path` to `catenate/wrap`:

```cl
(catenate/wrap
     :env :production
     :context-path "/bundles/")
```

#### What if I need to share static files with someone else?

Well, they have no way of knowing the cache buster hash, of course. In
that case you can give them a URL with `latest` in place of the hash:

```cl
["/catenate/latest/app.js"]
```

But **you have to make sure these URLs are not served with far future
expires headers**, or you'll be in trouble when updating.

## But how about ...

 - **Minification?**

   This middleware doesn't concern itself with minification, but there
   are options:

   - since the configuration takes tuples of filenames and content-producing
   functions, adding minification can be done there.

   - minification can be added as another middleware.

 - **Compiling?**

   You mean like LESS or CoffeeScript? I guess you're looking for a
   full-fledged asset pipeline. This isn't that. Check out
   [Dieter](https://github.com/edgecase/dieter).

 - **Relative URLs in CSS?**

   At the moment there is no support for rewriting the CSS to update
   inline paths, so your CSS `url(...)` declarations need to be
   absolute. This is certainly a problem I would like to tackle at
   some point. In fact, this is the reason that the data structure for
   files is not part of the public API before 1.0.

## License

Copyright © 2013 Magnar Sveen

Distributed under the Eclipse Public License, the same as Clojure.
