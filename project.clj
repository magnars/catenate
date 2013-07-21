(defproject catenate "0.1.0-SNAPSHOT"
  :description "A Ring middleware to serve concatenated static files with cache buster URLs in production."
  :url "http://github.com/magnars/catenate"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :profiles {:dev {:dependencies [[ring/ring-core "1.1.1"]
                                  [hiccup "1.0.2"]]}})
