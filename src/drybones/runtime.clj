(ns drybones.runtime
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :as test]
   [taoensso.timbre :as timbre]))

(s/def ::hook-key keyword?)
(s/def ::hook-function test/function?)

(def ^:private shutdown-hooks
  (atom {}))

(defn ^:private run-shutdown-hooks
  []
  (doseq [f (vals @shutdown-hooks)] (f)))

(defonce ^:private init-shutdown-hook
  (->> #'run-shutdown-hooks
       (Thread.)
       (.addShutdownHook (Runtime/getRuntime))
       (delay)))

(s/fdef add-shutdown-hook
        :args (s/cat :key ::hook-key
                     :fn  ::hook-function))
(defn add-shutdown-hook
  "Add a hook to run when the JVM is shutting down."
  [k f]
  (timbre/info {:message "Adding shutdown hook" :hook k})
  (force init-shutdown-hook)
  (swap! shutdown-hooks assoc k f))

(s/fdef remove-shutdown-hook
        :args (s/cat :key ::hook-key))
(defn remove-shutdown-hook
  "Remove a shutdown hook registered in the JVM."
  [k]
  (timbre/info {:message "Removing shutdown hook" :hook k})
  (swap! shutdown-hooks dissoc k))
