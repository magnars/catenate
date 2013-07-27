(ns catenate.hiccup
  (:require [catenate.core :as catenate]))

(defn link-to-css-bundles
  [request bundles]
  (map (fn [url] [:link {:rel "stylesheet" :href url}])
       (catenate/bundle-urls request bundles)))

(defn link-to-css-file
  [request original-url]
  [:link {:rel "stylesheet"
          :href (catenate/file-url request original-url)}])

(defn link-to-js-bundles
  [request bundles]
  (map (fn [url] [:script {:src url}])
       (catenate/bundle-urls request bundles)))
