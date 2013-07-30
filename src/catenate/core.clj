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
  [file]
  {:status 200
   :body ((:get-contents file))
   :headers (if-let [get-headers (:get-headers file)]
              (get-headers)
              {})})

(defn serve-files
  [app files]
  (let [original-url->file (into {} (map (juxt :original-url identity) files))
        url->file (into original-url->file (map (juxt :url identity) files))]
    (fn [request]
      (if-let [file (url->file (:uri request))]
        (respond-with file)
        (app (assoc-in request [:catenate-files] files))))))

(defn- transform-debug [files]
  (-> files
      transform/add-files-referenced-in-css))

(defn- transform-production [files]
  (-> files
      transform/rewrite-file-paths-in-css-to-absolute-urls
      transform/add-files-referenced-in-css
      transform/concatenate-bundles
      transform/add-far-future-expires-headers
      transform/add-cache-busters
      transform/update-file-paths-in-css))

(defn paths->files [public-dir file-paths]
  (map (partial f/file public-dir) file-paths))

(defn wrap
  [app & {:keys [bundles extra-files debug public-dir]}]
  (-> app
      (serve-files
       (let [bundle-files (bundles->files public-dir bundles)
             all-files (concat bundle-files extra-files)]
         (if debug
           (transform-debug all-files)
           (transform-production all-files))))))
