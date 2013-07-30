# <img align="right" src="https://raw.github.com/magnars/catenate/master/concatenated.jpg"> catenate [![Build Status](https://secure.travis-ci.org/magnars/catenate.png)](http://travis-ci.org/magnars/catenate)

A Ring middleware that:

 - in production: serves static files concatenated with cache buster URLs and [far future Expires headers](http://developer.yahoo.com/performance/rules.html#expires)
 - in development: serves static files individually

In other words: Develop with ease. Cache aggressively in production.

## Install

Add `[catenate "0.5.0"]` to `:dependencies` in your `project.clj`.

## Usage

Let's start with the most basic setup, the top level abstraction. The
porcelain, if you will. This should suit most projects:

```cl
(def bundles ;; 1
  {"styles.css" ["/style/reset.css" ;; 2
                 "/style/main.css"]
   "lib.js" ["/script/external/jquery.js"
             "/script/external/angular.js"
             "/script/global.js"]
   "app.js" ["/script/some.js"
             "/script/code.js"]})

(-> app
      (catenate/wrap ;; 3
       :bundles bundles ;; 4
       :debug true ;; 5
       :public-dir "public") ;; 6
      (ring.middleware.content-type/wrap-content-type)) ;; 7
```

1. Declare how your files are bundled in a map from bundle name to
   list of files.

2. The contents are concatenated together in the order specified in the
   bundle.

3. Add `catenate/wrap` as a Ring middleware.

4. Pass in the bundle map.

5. In production mode you set `:debug false`. Files are concatenated
   and URLs have cache busters. In development you set `:debug true`,
   and the files passes through unscathed. Set up properly with
   environment variables of some kind.

6. The files are served from the `:public-dir` on the classpath
   (normally in the `resources` directory).

7. Since Ring comes with content type middleware, catenate doesn't
   worry about it. Just make sure to put it after catenate.

#### Using the new URLs

Since we're rewriting URLs to include cache busters, we need to access
them through catenate.

See example in hiccup below. Notice that we use `map`, since there is
likely more than one URL in development mode.

```cl
(defn my-page
  [request]
  (hiccup.core/html
   [:html
    [:head
     (map (fn [url] [:link {:rel "stylesheet" :href url}])
          (catenate/bundle-urls request ["styles.css"]))]
    [:body
     (map (fn [url] [:script {:src url}])
          (catenate/bundle-urls request ["lib.js" "app.js"]))]]))
```

There's also some hiccup-specific sugar:

```cl
(defn my-page
  [request]
  (hiccup.core/html
   [:html
    [:head
     (catenate.hiccup/link-to-css-bundles request ["styles.css"])]
    [:body
     (catenate.hiccup/link-to-js-bundles request ["lib.js" "app.js"])]]))
```

## So how does this work in development mode?

The given paths are used unchanged. So given this example:

```cl
(-> app
    (catenate/wrap
     :bundles {"app.js" ["/app/some.js"
                         "/app/cool.js"
                         "/app/code.js"]}
     :debug true
     :public-dir "public"))
```

When you call

```cl
(catenate/bundle-urls request ["app.js"])
```

it returns

```cl
["/app/some.js"
 "/app/cool.js"
 "/app/code.js"]
```

And those are served from `resources/public/`, or more specifically on
eg. `public/app/some.js` on the classpath.

## What about production mode?

All the contents for each bundle is read at startup. URLs are
generated from the hash of the contents and the identifier of the
bundle.

So when you call `(catenate/bundle-urls request ["app.js"])`, it now
returns:

```cl
["/bundles/d131dd02c5e6eec4-app.js"]
```

and the middleware handles this URL by returning the concatenated
file contents in the order given by the bundle.

#### How do I handle cache busters on images?

CSS files that reference images are rewritten so that they point to
cache busting URLs.

If you're using static images in your HTML, then you'll add a list of
these files to the `catenate/wrap` declarations like so:

```cl
(catenate/wrap
     :debug false
     :public-dir "public"
     :extra-files (catenate/paths->files "public" ["/images/logo.png"])
     :bundles {...})
```

And then grab the cache buster URL like so:

```cl
(catenate/file-url request "/images/logo.png")
```

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
your rolling restarts, but it's a source of bugs that are hard to
track down.

#### What if I need to share static files with someone else?

Well, they have no way of knowing the cache buster hash, of course.
Luckily the files are still available on their original URLs.

When `:debug` is set to `false`, the bundles are also available. For
instance: `/bundles/d131dd02c5e6eec4-app.js` can also be accessed on
`/bundles/app.js`.

*Please note:* **You have to make sure these URLs are not served with
far future expires headers**, or you'll be in trouble when updating.

## But how about ...

 - **Minification?**

   Not yet. It is certainly something I'd like to tackle soon.

 - **Compiling?**

   You mean like LESS or CoffeeScript? I guess you're looking for a
   full-fledged asset pipeline. This isn't that. Check out
   [Dieter](https://github.com/edgecase/dieter).

## License

Copyright © 2013 Magnar Sveen

Distributed under the Eclipse Public License, the same as Clojure.
