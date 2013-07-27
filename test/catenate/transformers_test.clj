(ns catenate.transformers-test
  (:use catenate.transformers
        clojure.test)
  (:require [clojure.java.io :as io]))

(def files
  [{:url "/scripts/external/jquery.js"
    :original-url "/scripts/external/jquery.js"
    :type :js
    :get-contents #(slurp (io/resource "public/scripts/external/jquery.js"))
    :bundle "lib.js"}
   {:url "/scripts/external/angular.js"
    :original-url "/scripts/external/angular.js"
    :type :js
    :get-contents #(slurp (io/resource "public/scripts/external/angular.js"))
    :bundle "lib.js"}
   {:url "/styles/main.css"
    :original-url "/styles/main.css"
    :type :css
    :get-contents #(slurp (io/resource "public/styles/main.css"))
    :bundle "styles.css"}
   {:url "/images/logo.png"
    :original-url "/images/logo.png"
    :type :binary
    :get-contents #(io/as-file (io/resource "public/images/logo.png"))}])

(def get-contents #((:get-contents %)))

(deftest concatenate-bundles-test
  (let [bundled (concatenate-bundles files)]
    (is (= (map (juxt :url :type :bundle) bundled)
           [["/scripts/external/jquery.js" :js nil]
            ["/scripts/external/angular.js" :js nil]
            ["/styles/main.css" :css nil]
            ["/images/logo.png" :binary nil]
            ["/bundles/lib.js" :js "lib.js"]
            ["/bundles/styles.css" :css "styles.css"]]))
    (is (= (map get-contents (take 2 (drop 4 bundled)))
           ["alert('jquery');\nalert('angular');"
            "body {background: url(../images/bg.png);}\n#logo {background: url(../images/logo.png);}\n\n"]))))

(deftest add-cache-busters-test
  (is (= (map (juxt :original-url :url) (add-cache-busters files))
         [["/scripts/external/jquery.js" "/scripts/external/0eff519d941e9e18503eb4095f4f53ecab5ce4bf-jquery.js"]
          ["/scripts/external/angular.js" "/scripts/external/3125a187825eb52821b2e3ca6a7f6e89e3271662-angular.js"]
          ["/styles/main.css" "/styles/01a1860de69fdf24784b6f60a8e94c9cd76d0d9a-main.css"]
          ["/images/logo.png" "/images/79e3f867ba8e6445a031df097f3a7cc19ed9e642-logo.png"]])))

(deftest include-files-in-css-test
  (is (= (map (juxt :url :original-url :type)
              (drop 3 (include-files-in-css files)))
         [["/images/logo.png" "/images/logo.png" :binary]
          ["/images/bg.png" "/images/bg.png" :binary]])))

(deftest update-file-paths-in-css-test
  (is (= (map get-contents (take 3 (update-file-paths-in-css files)))
         ["alert('jquery');"
          "alert('angular');"
          (str "body {background: url(\"../images/bg.png\");}\n"
               "#logo {background: url(\"/images/logo.png\");}\n\n")])))

(deftest basic-integration-test
  (let [optimized (-> files
                      concatenate-bundles
                      include-files-in-css
                      add-cache-busters
                      update-file-paths-in-css)]
    (is (= (map (juxt :url :type :bundle) optimized)
           [["/scripts/external/0eff519d941e9e18503eb4095f4f53ecab5ce4bf-jquery.js" :js nil]
            ["/scripts/external/3125a187825eb52821b2e3ca6a7f6e89e3271662-angular.js" :js nil]
            ["/styles/01a1860de69fdf24784b6f60a8e94c9cd76d0d9a-main.css" :css nil]
            ["/images/79e3f867ba8e6445a031df097f3a7cc19ed9e642-logo.png" :binary nil]
            ["/bundles/3b09c9d319f24d41e4376068707b9b4e538338d5-lib.js" :js "lib.js"]
            ["/bundles/01a1860de69fdf24784b6f60a8e94c9cd76d0d9a-styles.css" :css "styles.css"]
            ["/images/f314fe7cd7a380babfd4fc11cb5f60f8e788b6a3-bg.png" :binary nil]]))
    (is (= (map get-contents (take 2 (drop 4 optimized)))
           [(str "alert('jquery');\n"
                 "alert('angular');")
            (str "body {background: url(\"/images/f314fe7cd7a380babfd4fc11cb5f60f8e788b6a3-bg.png\");}\n"
                 "#logo {background: url(\"/images/79e3f867ba8e6445a031df097f3a7cc19ed9e642-logo.png\");}\n\n")]))))
