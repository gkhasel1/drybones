(ns dev
  (:require
   [clojure.spec.test.alpha :as st]
   [clojure.test :as test]
   [clojure.tools.namespace.repl :as repl]
   [mount.core :as mount]
   [drybones.logging]
   [drybones.runtime]
   [drybones.web-server]))

(defn start
  []
  (st/instrument)
  (mount/start)
  :ready)

(defn stop
  []
  (mount/stop))

(defn reset
  []
  (stop)
  (repl/refresh :after 'dev/start))

(defn run-all-tests
  []
  (repl/refresh)
  (test/run-all-tests #"drybones.*-test"))
