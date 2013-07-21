(ns catenate.example-app-integration-test
  (:require [catenate.example.app :as example]
            [ring.mock.request :refer [request]]
            [clojure.test :refer [deftest is]]))

(deftest development-mode-index-test
  (is (= (example/app (request :get "/"))
         {:status 200
          :headers {"Content-Type" "text/html; charset=utf-8"}
          :body (str "<body>"
                     "<h1>Example app</h1>"
                     "<script src=\"/catenate/public/cool.js\"></script>"
                     "<script src=\"/catenate/public/code.js\"></script>"
                     "</body>")})))

(deftest development-mode-single-file-test
  (is (= (example/app (request :get "/catenate/public/code.js"))
         {:status 200
          :headers {"Content-Type" "text/javascript"}
          :body "prompt('code:');"})))
