(ns catenate.example-app-integration-test
  (:require [catenate.example.app :as example]
            [ring.mock.request :refer [request]]
            [clojure.test :refer [deftest is]]))

(deftest your-handler-test
  (is (= (example/app (request :get "/"))
         {:status 200
          :headers {"Content-Type" "text/html; charset=utf-8"}
          :body "<h1>Example app</h1>"})))
