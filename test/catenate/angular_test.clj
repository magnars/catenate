(ns catenate.angular-test
  (:use catenate.angular
        clojure.test)
  (:require [clojure.java.io :as io]))

(deftest create-template-cache-test
  (testing "one file"
    (let [file (create-template-cache
                :path "/my-templates.js"
                :module "myapp"
                :public-dir "public"
                :templates ["/templates/simple.html"])]
      (is (= (:url file) "/my-templates.js"))
      (is (= (:original-url file) "/my-templates.js"))
      (is (= (:type file) :js))
      (is (= ((:get-contents file))
             (slurp (io/resource "public/templates/simple-expected.js"))))))

  (testing "multiple files"
    (let [file (create-template-cache
                :path "/multiple-templates.js"
                :module "multiple"
                :public-dir "public"
                :templates ["/templates/multiple/one.html"
                            "/templates/multiple/two/two.html"])]
      (is (= (:url file) "/multiple-templates.js"))
      (is (= (:original-url file) "/multiple-templates.js"))
      (is (= (:type file) :js))
      (is (= ((:get-contents file))
             (slurp (io/resource "public/templates/multiple-expected.js")))))))
