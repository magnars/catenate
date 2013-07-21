(ns catenate.core-test
  (:require [clojure.test :refer :all]
            [catenate.core :as catenate]))

(deftest resource-reads-from-classpath
  (let [r (catenate/resource "public/code.js")]
    (is (= (first r) "public/code.js"))
    (is (= ((second r)) "prompt('code:');"))))

(def js-bundles {"lib.js" [(catenate/resource "public/some.js")]
                 "app.js" [(catenate/resource "public/cool.js")
                           (catenate/resource "public/code.js")]})

(deftest wrap-adds-plain-urls-to-request-in-development-mode
  ((catenate/wrap
    (fn [request]
      (is (= (get-in request [:catenate :urls "app.js"])
             ["/bundles/public/cool.js"
              "/bundles/public/code.js"])))
    :env :development
    :context-path "/bundles/"
    :bundles js-bundles)
   {}))

(deftest bundles->asset-map-test
  (is (= (set (keys (catenate/bundles->asset-map js-bundles "/catenate/")))
         #{"/catenate/public/some.js"
           "/catenate/public/cool.js"
           "/catenate/public/code.js"})))
