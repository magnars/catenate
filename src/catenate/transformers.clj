(ns catenate.transformers
  (:require [clojure.java.io :as io]
            [catenate.digest :as digest]
            [catenate.file-struct :as f]
            [clojure.string :as str]
            [clojure.set :as set]
            [pathetic.core :as pathetic]))

(defn- read-in-contents [file]
  (let [contents ((:get-contents file))]
    (if (= :binary (:type file))
      (slurp contents)
      contents)))

;; concatenate-bundles

(defn- concatenate-bundle [[name files]]
  (when name
    {:url (str "/bundles/" name)
     :original-url (str "/bundles/" name)
     :type (:type (first files)) ;; todo: throw if different
     :get-contents #(str/join "\n" (map read-in-contents files))
     :bundle name}))

(defn concatenate-bundles [files]
  (concat (map #(dissoc % :bundle) files)
          (keep concatenate-bundle (group-by :bundle files))))

;; add-cache-busters

(defn- insert-cache-buster [url hash]
  (str/replace url #"/([^/]+)$" (str "/" hash "-$1")))

(defn- add-cache-buster [file]
  (let [hash (digest/sha-1 (read-in-contents file))]
    (update-in file [:url] #(insert-cache-buster % hash))))

(defn add-cache-busters [files]
  (map add-cache-buster files))

;; add-files-referenced-in-css

(def css-url-re #"url\(['\"]?([^\)]+?)['\"]?\)")

(defn- just-the-path [url]
  (-> url
      pathetic/parse-path
      pathetic/up-dir
      pathetic/render-path
      pathetic/ensure-trailing-separator))

(defn- combine-paths [original-url relative-url]
  (pathetic/normalize (pathetic/resolve (just-the-path original-url)
                                        relative-url)))

(defn- paths-in-css [file]
  (->> file
       read-in-contents
       (re-seq css-url-re)
       (map (comp (partial combine-paths (:original-url file)) second))))

(defn- just-the [type files]
  (filter (comp #{type} :type) files))

(defn add-files-referenced-in-css [files]
  (concat
   files
   (map (partial f/binary-file "public")
        (set/difference
         (set (mapcat paths-in-css (just-the :css files)))
         (set (map :original-url files))))))

;; css rewriting

(defn- map-just-the [type files fn]
  (map #(if (= type (:type %)) (fn %) %)
       files))

(defn- css-url-str [url]
  (str "url(\"" url "\")"))

(defn- replace-css-urls [file replacement-fn]
  (str/replace (read-in-contents file)
               css-url-re
               (fn [[_ url]] (css-url-str (replacement-fn file url)))))

(defn- replace-css-urls-in-files [files replacement-fn]
  (map-just-the :css files
                (fn [file]
                  (assoc file :get-contents (partial replace-css-urls file replacement-fn)))))

;; rewrite-file-paths-in-css-to-absolute-urls

(defn rewrite-file-paths-in-css-to-absolute-urls [files]
  (replace-css-urls-in-files files (fn [file url] (combine-paths (:original-url file) url))))

;; update-file-paths-in-css

(defn update-file-paths-in-css [files]
  (let [orig->curr (into {} (map (juxt :original-url :url) files))]
    (replace-css-urls-in-files files (fn [_ url] (get orig->curr url url)))))
