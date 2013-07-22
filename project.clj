(defproject catenate "0.2.0-SNAPSHOT"
  :description "A Ring middleware to serve concatenated static files with cache buster URLs in production."
  :url "http://github.com/magnars/catenate"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-glob "1.0.0"]]
  :profiles {:test {:dependencies [[ring/ring-core "1.1.1"]
                                   [hiccup "1.0.2"]
                                   [compojure "1.1.3"]
                                   [ring-mock "0.1.5"]]
                    :resource-paths ["test/resources"]}})
