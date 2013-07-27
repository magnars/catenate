(ns catenate.file-struct-test
  (:require [catenate.file-struct :as c]
            [clojure.java.io :as io])
  (:use clojure.test))

(deftest binary-file-test
  (doseq [file [(c/binary-file "public" "/images/logo.png")
                (c/file "public" "/images/logo.png")]]
    (is (= (:url file) "/images/logo.png"))
    (is (= (:original-url file) "/images/logo.png"))
    (is (= (:type file) :binary))
    (is (= ((:get-contents file)) (io/as-file (io/resource "public/images/logo.png"))))))

(deftest css-file-test
  (doseq [file [(c/css-file "public" "/styles/main.css")
                (c/file "public" "/styles/main.css")]]
    (is (= (:url file) "/styles/main.css"))
    (is (= (:original-url file) "/styles/main.css"))
    (is (= (:type file) :css))
    (is (= ((:get-contents file)) "body {background: url(../images/bg.png);}\n#logo {background: url(../images/logo.png);}\n\n"))))

(deftest js-file-test
  (doseq [file [(c/js-file "public" "/scripts/code.js")
                (c/file "public" "/scripts/code.js")]]
    (is (= (:url file) "/scripts/code.js"))
    (is (= (:original-url file) "/scripts/code.js"))
    (is (= (:type file) :js))
    (is (= ((:get-contents file)) "prompt('code:');"))))
