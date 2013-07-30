(ns catenate.transformers-test
  (:use catenate.transformers
        clojure.test)
  (:require [clojure.java.io :as io]
            [clj-time.core :as time]))

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
            "body {background: url(../images/bg.png);}\n#logo {background: url(../images/logo.png);}\n.button {background: url(button.png);}\n\n"]))))

(deftest add-cache-busters-test
  (is (= (map (juxt :original-url :url) (add-cache-busters files))
         [["/scripts/external/jquery.js" "/scripts/external/0eff519d941e9e18503eb4095f4f53ecab5ce4bf-jquery.js"]
          ["/scripts/external/angular.js" "/scripts/external/3125a187825eb52821b2e3ca6a7f6e89e3271662-angular.js"]
          ["/styles/main.css" "/styles/b471ac9b1280a89ad1e4643f3265cb48be3ea903-main.css"]
          ["/images/logo.png" "/images/79e3f867ba8e6445a031df097f3a7cc19ed9e642-logo.png"]])))

(deftest add-files-referenced-in-css-test
  (is (= (map (juxt :url :original-url :type)
              (drop 3 (add-files-referenced-in-css files)))
         [["/images/logo.png" "/images/logo.png" :binary]
          ["/images/bg.png" "/images/bg.png" :binary]
          ["/styles/button.png" "/styles/button.png" :binary]])))

(deftest rewrite-file-paths-in-css-to-absolute-urls-test
  (is (= (get-contents (nth (rewrite-file-paths-in-css-to-absolute-urls files) 2))
         (str "body {background: url(\"/images/bg.png\");}\n"
              "#logo {background: url(\"/images/logo.png\");}\n"
              ".button {background: url(\"/styles/button.png\");}\n\n"))))

(deftest update-file-paths-in-css-test
  (is (= (get-contents (nth (update-file-paths-in-css files) 2))
         (str "body {background: url(\"../images/bg.png\");}\n"
              "#logo {background: url(\"../images/logo.png\");}\n"
              ".button {background: url(\"button.png\");}\n\n"))))

(deftest add-far-future-expires-headers-test
  (let [now (time/date-time 2013 07 30)]
    (with-redefs [time/now (fn [] now)]
      (is (= ((:get-headers (first (add-far-future-expires-headers files))))
             {"Cache-Control" "max-age=315360000"
              "Expires" "Fri, 28 Jul 2023 00:00:00 GMT"})))))

(deftest basic-integration-test
  (let [optimized (-> files
                      rewrite-file-paths-in-css-to-absolute-urls
                      add-files-referenced-in-css
                      concatenate-bundles
                      add-cache-busters
                      update-file-paths-in-css)]
    (is (= (map (juxt :url :type :bundle) optimized)
           [["/scripts/external/0eff519d941e9e18503eb4095f4f53ecab5ce4bf-jquery.js" :js nil]
            ["/scripts/external/3125a187825eb52821b2e3ca6a7f6e89e3271662-angular.js" :js nil]
            ["/styles/46f6cfab48f77299a4665a975f990bd7abafb6fc-main.css" :css nil]
            ["/images/79e3f867ba8e6445a031df097f3a7cc19ed9e642-logo.png" :binary nil]
            ["/images/f314fe7cd7a380babfd4fc11cb5f60f8e788b6a3-bg.png" :binary nil]
            ["/styles/c5e335c1fa312963d0485568ef45e50876ce3b24-button.png" :binary nil]
            ["/bundles/3b09c9d319f24d41e4376068707b9b4e538338d5-lib.js" :js "lib.js"]
            ["/bundles/46f6cfab48f77299a4665a975f990bd7abafb6fc-styles.css" :css "styles.css"]]))
    (is (= (map get-contents (drop 6 optimized))
           [(str "alert('jquery');\n"
                 "alert('angular');")
            (str "body {background: url(\"/images/f314fe7cd7a380babfd4fc11cb5f60f8e788b6a3-bg.png\");}\n"
                 "#logo {background: url(\"/images/79e3f867ba8e6445a031df097f3a7cc19ed9e642-logo.png\");}\n"
                 ".button {background: url(\"/styles/c5e335c1fa312963d0485568ef45e50876ce3b24-button.png\");}\n\n")]))))
