(ns catenate.example-app-integration-test
  (:require [catenate.example.app :as example]
            [ring.mock.request :refer [request]]
            [clojure.test :refer [deftest testing is]]))

;; development mode

(def expected-index-response-in-dev
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (str "<head>"
              "<link href=\"/styles/reset.css\" rel=\"stylesheet\" />"
              "<link href=\"/styles/base.css\" rel=\"stylesheet\" />"
              "<link href=\"/styles/login.css\" rel=\"stylesheet\" />"
              "</head>"
              "<body>"
              "<h1>Example app</h1>"
              "<script src=\"/scripts/some.js\"></script>"
              "<script src=\"/scripts/cool.js\"></script>"
              "<script src=\"/scripts/code.js\"></script>"
              "</body>")})

(deftest development-mode-index-test
  (is (= (example/app-dev (request :get "/"))
         expected-index-response-in-dev)))

(deftest development-mode-index-w-hiccup-test
  (is (= (example/app-dev (request :get "/hiccup"))
         expected-index-response-in-dev)))

(deftest development-mode-single-resource-test
  (is (= (example/app-dev (request :get "/scripts/code.js"))
         {:status 200
          :headers {"Content-Type" "text/javascript"}
          :body "prompt('code:');"})))

(deftest development-mode-single-file-test
  (is (= (example/app-dev (request :get "/styles/reset.css"))
         {:status 200
          :headers {"Content-Type" "text/css"}
          :body "html, body { margin: 0; padding: 0; }"})))

;; production mode

(deftest production-mode-test
  (let [expires "Fri, 28 Jul 2023 00:00:00 GMT"
        expires-headers {"Cache-Control" "max-age=315360000" "Expires" expires}]
    (with-redefs [catenate.transformers/http-date-formatter (fn [_] expires)]
      (testing "generation of urls"
        (is (= (example/app-prod (request :get "/"))
               {:status 200
                :headers {"Content-Type" "text/html; charset=utf-8"}
                :body (str "<head>"
                           "<link href=\"/bundles/07d3b468bb7ba285e80bab3912ccd9ec9df4053f-styles.css\" rel=\"stylesheet\" />"
                           "<link href=\"/styles/d8a4e6f45de5689d977757030aa35e4d50b4fef1-login.css\" rel=\"stylesheet\" />"
                           "</head>"
                           "<body>"
                           "<h1>Example app</h1>"
                           "<script src=\"/bundles/6c49e36f075925a46c6a9156d65c8c6c9ac9abe8-lib.js\"></script>"
                           "<script src=\"/bundles/67ed01377a858d64581ff4e28712f4e4e47b8b2b-app.js\"></script>"
                           "</body>")})))

      (testing "getting bundle"
        (is (= (example/app-prod (request :get "/bundles/67ed01377a858d64581ff4e28712f4e4e47b8b2b-app.js"))
               {:status 200
                :headers (merge {"Content-Type" "text/javascript"} expires-headers)
                :body "confirm(\"cool?\");\nprompt('code:');"})))

      (testing "getting bundle without cache buster"
        (is (= (example/app-prod (request :get "/bundles/app.js"))
               {:status 200
                :headers (merge {"Content-Type" "text/javascript"} expires-headers)
                :body "confirm(\"cool?\");\nprompt('code:');"})))

      (testing "bundled files are still available individually"
        (is (= (example/app-prod (request :get "/scripts/code.js"))
               {:status 200
                :headers (merge {"Content-Type" "text/javascript"} expires-headers)
                :body "prompt('code:');"})))

      (testing "getting extra file (defined outside of bundle)"
        (is (= (example/app-prod (request :get "/scripts/more.js"))
               {:status 200
                :headers (merge {"Content-Type" "text/javascript"} expires-headers)
                :body "if (1 < 2) {\n  alert('Math still works!');\n}"}))
        (is (= (example/app-prod (request :get "/styles/d8a4e6f45de5689d977757030aa35e4d50b4fef1-login.css"))
               {:status 200
                :headers (merge {"Content-Type" "text/css"} expires-headers)
                :body ".login { color: red; }\n"}))))))
