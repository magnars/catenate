(ns catenate.file-struct
  (:require [clojure.java.io :as io]))

(defn- file-struct [url type get-contents]
  {:url url
   :original-url url
   :type type
   :get-contents get-contents})

(defn binary-file [public-dir url]
  (file-struct url :binary #(io/as-file (io/resource (str public-dir url)))))

(defn css-file [public-dir url]
  (file-struct url :css #(slurp (io/resource (str public-dir url)))))

(defn js-file [public-dir url]
  (file-struct url :js #(slurp (io/resource (str public-dir url)))))

(defn file [public-dir url]
  (cond
   (.endsWith url ".css") (css-file public-dir url)
   (.endsWith url ".js") (js-file public-dir url)
   :else (binary-file public-dir url)))
