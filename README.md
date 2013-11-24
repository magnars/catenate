# catenate

Catenate is deprecated in favor of [optimus](https://github.com/magnars/optimus).

### Why?

I had planned on catenate being a small library in a series of
libraries for frontend optimization. Catenate was the one that
concatenated your static assets into bundles.

Turns out that a lot of those optimizations you want to do on the
frontend are tangled together. I was not able to do those improvements
I wanted with this model.

So, when the API turned out quite different, I figured it would be
better with a name that isn't so focused on concatenation - and more
on optimization: [optimus](https://github.com/magnars/optimus).

### I'm still using catenate, where are the docs?

You can find them in [instructions.md](instructions.md).

### How do I migrate to optimus?

This is catenate:

```cl
(-> app
    (catenate/wrap
     :bundles my-bundles
     :extra-files my-files
     :debug (:debug env)
     :public-dir "public"))
```

This is optmius:

```cl
(defn get-assets []
  (concat
   (assets/load-bundles "public" my-bundles)
   (assets/load-assets "public" my-files)))

(-> app
    (optimus/wrap
     get-assets
     (if (:debug env)
       strategies/serve-unchanged-assets
       strategies/serve-frozen-optimized-assets)))
```

There's more code here, and less configuration. In other words, more
power to you.

### What do I get if I upgrade?

At the time of writing, there's this:

- You can change your `get-assets` function in development-mode, and
  see those changes mirrored without a restart.

- You can list files to include in bundles using regexp.

- You get minification of scripts and styles.

- You can serve optimized assets while developing, so that you are in
  a production-like environment, but with live reloads.

