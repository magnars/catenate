(ns catenate.hiccup
  (:require [catenate.core :as catenate]))

(defn link-to-css
  [request bundles]
  (map (fn [url] [:link {:rel "stylesheet" :href url}])
       (catenate/urls request bundles)))

(defn link-to-js
  [request bundles]
  (map (fn [url] [:script {:src url}])
       (catenate/urls request bundles)))
