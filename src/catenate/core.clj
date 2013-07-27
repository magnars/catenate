(ns catenate.core
  (:require [catenate.transformers :as transform]
            [catenate.file-struct :as f]))

(defn- bundle-urls-1
  [request bundle-name]
  (->> (:catenate-files request)
       (filter #(= bundle-name (:bundle %)))
       (map :url)))

(defn bundle-urls
  [request bundles]
  (mapcat (partial bundle-urls-1 request) bundles))

(defn file-url
  [request original-url]
  (->> (:catenate-files request)
       (filter #(= original-url (:original-url %)))
       (first)
       :url))

(defn with-prefix
  [prefix & paths]
  (map (partial str prefix)
       (apply concat paths)))

(defn- bundle->files
  [public-dir [name files]]
  (map #(assoc (f/file public-dir %) :bundle name) files))

(defn bundles->files [public-dir bundles]
  (mapcat (partial bundle->files public-dir) bundles))

(defn- respond-with
  [contents]
  {:status 200 :body contents})

(defn serve-files
  [app files]
  (let [original-url->get-contents (into {} (map (juxt :original-url :get-contents) files))
        url->get-contents (into original-url->get-contents (map (juxt :url :get-contents) files))]
    (fn [request]
      (if-let [get-contents (url->get-contents (:uri request))]
        (respond-with (get-contents))
        (app (assoc-in request [:catenate-files] files))))))

(defn- transform-debug [files]
  (-> files
      transform/include-files-in-css))

(defn- transform-production [files]
  (-> files
      transform/concatenate-bundles
      transform/include-files-in-css
      transform/add-cache-busters
      transform/update-file-paths-in-css))

(defn wrap
  [app & {:keys [bundles extra-files debug public-dir]}]
  (-> app
      (serve-files
       (let [bundle-files (bundles->files public-dir bundles)
             extra-files (map (partial f/file public-dir) extra-files)
             all-files (concat bundle-files extra-files)]
         (if debug
           (transform-debug all-files)
           (transform-production all-files))))))
