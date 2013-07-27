(ns catenate.core-test
  (:require [catenate.core :as catenate]
            [clojure.java.io :as io])
  (:use clojure.test))

(def bundled (catenate/bundles->files "public"
                                     {"lib.js" ["/scripts/external/jquery.js"
                                                "/scripts/external/angular.js"]
                                      "app.js" ["/scripts/some.js"
                                                "/scripts/cool.js"
                                                "/scripts/code.js"]}))

(deftest bundles->files-test
  (is (= (map (juxt :url :bundle) bundled)
         [["/scripts/external/jquery.js" "lib.js"]
          ["/scripts/external/angular.js" "lib.js"]
          ["/scripts/some.js" "app.js"]
          ["/scripts/cool.js" "app.js"]
          ["/scripts/code.js" "app.js"]])))

(deftest bundle-urls-test
  (is (= (catenate/bundle-urls {:catenate-files bundled} ["app.js"])
         ["/scripts/some.js"
          "/scripts/cool.js"
          "/scripts/code.js"])))
