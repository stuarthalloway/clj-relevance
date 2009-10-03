(ns test
  (:use clojure.test))

(def tests
     ['lau.brians-brain-test])

(doseq [test tests] (require test))

(apply run-tests tests)

(shutdown-agents)