(ns catenate.example-app-integration-test
  (:require [catenate.example.app :as example]
            [ring.mock.request :refer [request]]
            [clojure.test :refer [deftest is]]))

(deftest development-mode-index-test
  (is (= (example/app (request :get "/"))
         {:status 200
          :headers {"Content-Type" "text/html; charset=utf-8"}
          :body (str "<head>"
                     "<link href=\"/catenate/test/files/styles/reset.css\" rel=\"stylesheet\" />"
                     "<link href=\"/catenate/test/files/styles/base.css\" rel=\"stylesheet\" />"
                     "</head>"
                     "<body>"
                     "<h1>Example app</h1>"
                     "<script src=\"/catenate/public/some.js\"></script>"
                     "<script src=\"/catenate/public/cool.js\"></script>"
                     "<script src=\"/catenate/public/code.js\"></script>"
                     "</body>")})))

(deftest development-mode-single-resource-test
  (is (= (example/app (request :get "/catenate/public/code.js"))
         {:status 200
          :headers {"Content-Type" "text/javascript"}
          :body "prompt('code:');"})))

(deftest development-mode-single-file-test
  (is (= (example/app (request :get "/catenate/test/files/styles/reset.css"))
         {:status 200
          :headers {"Content-Type" "text/css"}
          :body "html, body { margin: 0; padding: 0; }"})))
