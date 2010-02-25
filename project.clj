(defproject clj-relevance "0.0.1"
  :description "Clojure examples for the Relevance blog."
  :dependencies [
                 [org.clojure/clojure
                  "1.2.0-master-SNAPSHOT"]
                 [org.clojure/clojure-contrib
                  "1.2.0-master-SNAPSHOT"][compojure
                  "0.3.2"]
                 [org.clojure/swank-clojure
                  "1.0"]
                 [org.incanter/incanter-full
                  "1.2.0-SNAPSHOT"]
                 [clojure-http-client
                  "1.0.0-SNAPSHOT"]
                 [jline
                  "0.9.94"]
                 [circumspec
                  "0.0.8"]]
  :dev-dependencies [[autodoc "0.7.0"]]
  :repositories {"clojure-releases" "http://build.clojure.org/releases"
                 "incanter" "http://repo.incanter.org"})
