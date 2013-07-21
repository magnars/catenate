(ns catenate.core
  (:require [clojure.java.io :as io]))

(defn- prefixed-path [context-path asset]
  (str context-path (first asset)))

(defn- respond-with
  [contents]
  {:status 200 :body contents})

;; :env :development

(defn- bundles->asset-map
  [bundles context-path]
  (let [all-assets (apply concat (vals bundles))]
    (zipmap (map (partial prefixed-path context-path) all-assets)
            (map second all-assets))))

(defn- bundles->bundle-urls [bundles context-path]
  (zipmap (keys bundles)
          (map #(map (partial prefixed-path context-path) %)
               (vals bundles))))

(defn- wrap-development
  [app bundles context-path]
  (let [asset-map (bundles->asset-map bundles context-path)
        bundle-urls (bundles->bundle-urls bundles context-path)]
    (fn [request]
      (if (contains? asset-map (:uri request))
        (respond-with ((asset-map (:uri request))))
        (app (assoc-in request [:catenate :urls] bundle-urls))))))

;; public api

(defn wrap
  [app & {:keys [env bundles context-path]}]
  (let [context-path (or context-path "/catenate/")]
    (case env
      :development (wrap-development app bundles context-path))))

(defn resource
  [path]
  [path #(slurp (io/resource path))])

(def resources (partial map resource))

(defn file
  [path]
  [path #(slurp path)])

(def files (partial map file))

(defn urls
  [request urls]
  (mapcat #(get-in request [:catenate :urls %]) urls))
