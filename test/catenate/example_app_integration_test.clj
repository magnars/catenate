(ns catenate.example-app-integration-test
  (:require [catenate.example.app :as example]
            [ring.mock.request :refer [request]]
            [clojure.test :refer [deftest is]]))

;; development mode

(def expected-index-response-in-dev
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (str "<head>"
              "<link href=\"/catenate/test/files/styles/reset.css\" rel=\"stylesheet\" />"
              "<link href=\"/catenate/test/files/styles/base.css\" rel=\"stylesheet\" />"
              "<link href=\"/catenate/test/files/styles/login.css\" rel=\"stylesheet\" />"
              "</head>"
              "<body>"
              "<h1>Example app</h1>"
              "<script src=\"/catenate/public/some.js\"></script>"
              "<script src=\"/catenate/public/cool.js\"></script>"
              "<script src=\"/catenate/public/code.js\"></script>"
              "</body>")})

(deftest development-mode-index-test
  (is (= (example/app-dev (request :get "/"))
         expected-index-response-in-dev)))

(deftest development-mode-index-w-hiccup-test
  (is (= (example/app-dev (request :get "/hiccup"))
         expected-index-response-in-dev)))

(deftest development-mode-single-resource-test
  (is (= (example/app-dev (request :get "/catenate/public/code.js"))
         {:status 200
          :headers {"Content-Type" "text/javascript"}
          :body "prompt('code:');"})))

(deftest development-mode-single-file-test
  (is (= (example/app-dev (request :get "/catenate/test/files/styles/reset.css"))
         {:status 200
          :headers {"Content-Type" "text/css"}
          :body "html, body { margin: 0; padding: 0; }"})))

;; production mode

(deftest production-mode-index-test
  (is (= (example/app-prod (request :get "/"))
         {:status 200
          :headers {"Content-Type" "text/html; charset=utf-8"}
          :body (str "<head>"
                     "<link href=\"/catenate/356e001706e1806abbe5111e85302410dc77cd2a/styles.css\" rel=\"stylesheet\" />"
                     "</head>"
                     "<body>"
                     "<h1>Example app</h1>"
                     "<script src=\"/catenate/6c49e36f075925a46c6a9156d65c8c6c9ac9abe8/lib.js\"></script>"
                     "<script src=\"/catenate/67ed01377a858d64581ff4e28712f4e4e47b8b2b/app.js\"></script>"
                     "</body>")})))

(deftest production-mode-bundle-test
  (is (= (example/app-prod (request :get "/catenate/67ed01377a858d64581ff4e28712f4e4e47b8b2b/app.js"))
         {:status 200
          :headers {"Content-Type" "text/javascript"}
          :body "confirm(\"cool?\");\nprompt('code:');"})))

(deftest production-mode-latest-test
  (is (= (example/app-prod (request :get "/catenate/latest/app.js"))
         {:status 200
          :headers {"Content-Type" "text/javascript"}
          :body "confirm(\"cool?\");\nprompt('code:');"})))
