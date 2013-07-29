(ns catenate.file-struct
  (:require [clojure.java.io :as io])
  (:import java.io.FileNotFoundException))

(defn- file-struct [url type get-contents]
  {:url url
   :original-url url
   :type type
   :get-contents get-contents})

(defn existing-resource [path]
  (or (io/resource path)
      (throw (FileNotFoundException. path))))

(defn binary-file [public-dir url]
  (let [resource (existing-resource (str public-dir url))]
   (file-struct url :binary #(io/as-file resource))))

(defn css-file [public-dir url]
  (let [resource (existing-resource (str public-dir url))]
   (file-struct url :css #(slurp resource))))

(defn js-file [public-dir url]
  (let [resource (existing-resource (str public-dir url))]
   (file-struct url :js #(slurp resource))))

(defn file [public-dir url]
  (cond
   (.endsWith url ".css") (css-file public-dir url)
   (.endsWith url ".js") (js-file public-dir url)
   :else (binary-file public-dir url)))
