(ns catenate.angular
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [catenate.file-struct :refer [existing-resource]]))

(defn- escaped-js-string
  [s]
  (-> s
      (str/replace "\\" "\\\\")
      (str/replace "\"" "\\\"")
      (str/replace "\n" "\\n")))

(defn- template-cache-put
  [public-dir template]
  (let [contents (slurp (existing-resource (str public-dir template)))
        escaped-contents (escaped-js-string contents)]
    (str "  $templateCache.put(\"" template "\", \"" escaped-contents "\");\n")))

(defn- create-template-cache-js
  [module public-dir templates]
  (str "angular.module(\"" module  "\").run([\"$templateCache\", function ($templateCache) {\n"
       (apply str (map (partial template-cache-put public-dir) templates))
       "}]);" ))

(defn create-template-cache
  [& {:keys [path module public-dir templates bundle]}]
  {:url path
   :original-url path
   :type :js
   :bundle bundle
   :get-contents #(create-template-cache-js module public-dir templates)})
