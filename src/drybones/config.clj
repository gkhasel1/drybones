(ns drybones.config
  (:require
   [clojure.core.memoize :as memoize]
   [clojure.spec.alpha :as s]
   [cprop.core :as cprop]
   [cprop.source :as source]
   [taoensso.timbre :as timbre]))

(s/def ::logging (s/keys :req-un [:drybones.logging/format
                                  :drybones.logging/level]))

(s/def ::web-server (s/keys :req-un [:drybones.web-server/port]))

(s/def ::config (s/keys :req-un [::logging
                                 ::web-server]))

(defn read-env-config
  "Read the config from the environment."
  ([]
   (-> (cprop/load-config :merge [(source/from-env)])
       (read-env-config)))
  ([config]
   (if (s/valid? ::config config)
     config
     (let [explain-str  (s/explain-str ::config config)
           explain-data (s/explain-data ::config config)]
       (timbre/error {:message "Could not load config"
                      :error   explain-str
                      :data    explain-data})
       (throw
        (ex-info explain-str
                 explain-data))))))

(def config-cache
  (memoize read-env-config))

(defn config
  "Read the  configuration from a memoized cache.

  If arguments are provided, access elements of the
  config as in clojure.core's function `get-in'."
  ([]
   (config-cache))
  ([& args]
   (-> (config-cache) (get-in args))))
