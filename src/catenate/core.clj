(ns catenate.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [pandect.core]))

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

;; :env :production

(defn- bundle-contents
  [assets]
  (str/join "\n" (map #((second %)) assets)))

(defn- bundle-unique-url
  [context-path contents name]
  (str context-path (pandect.core/sha1 contents) "/" name))

(defn- wrap-production
  [app bundles context-path]
  (let [contents (map #(bundle-contents %) (vals bundles))
        names (keys bundles)
        unique-urls (map (partial bundle-unique-url context-path) contents names)
        url->contents (zipmap unique-urls contents)
        bundle-urls (zipmap names (map vector unique-urls))]
    (fn [request]
      (if-let [contents (url->contents (:uri request))]
        (respond-with contents)
        (app (assoc-in request [:catenate :urls] bundle-urls))))))

;; public api

(defn wrap
  [app & {:keys [env bundles context-path]}]
  (let [context-path (or context-path "/catenate/")]
    (case env
      :development (wrap-development app bundles context-path)
      :production (wrap-production app bundles context-path))))

(defn resource
  [path]
  [path #(slurp (io/resource path))])

(def resources (partial map resource))

(defn file
  [path]
  [path #(slurp path)])

(def files (partial map file))

(defn urls
  [request bundles]
  (mapcat #(get-in request [:catenate :urls %]) bundles))
