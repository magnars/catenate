(ns catenate.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [catenate.digest]))

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
    (fn [{:keys [uri] :as request}]
      (if (contains? asset-map uri)
        (respond-with ((asset-map uri)))
        (app (assoc-in request [:catenate :urls] bundle-urls))))))

;; :env :production

(defn- bundle-contents
  [assets]
  (str/join "\n" (map #((second %)) assets)))

(defn- bundle-unique-url
  [context-path contents name]
  (str context-path (catenate.digest/sha-1 contents) "/" name))

(defn- bundle-latest-url
  [context-path name]
  (str context-path "latest/" name))

(defn- wrap-production
  [app bundles context-path]
  (let [contents (map bundle-contents (vals bundles))
        names (keys bundles)
        unique-urls (map (partial bundle-unique-url context-path) contents names)
        latest-urls (map (partial bundle-latest-url context-path) names)
        url->contents (merge (zipmap unique-urls contents)
                             (zipmap latest-urls contents))
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
