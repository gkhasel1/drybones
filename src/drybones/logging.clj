(ns drybones.logging
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [camel-snake-kebab.core :as cskfmt]
   [cheshire.core :as json]
   [clj-time.coerce :as time-coerce]
   [clj-time.format :as time-format]
   [mount.core :refer [defstate]]
   [taoensso.timbre :as timbre]
   [drybones.config :as config]))

(s/def ::level #{:debug :info :warn :error :fatal})
(s/def ::format #{:json :string})

(defn context-snippet
  [{:keys [client-id request-id request-uri request-method] :as context}]
  (if (some some? [client-id request-id request-uri request-method])
    (str "[" request-method " " request-uri ":" client-id ":" request-id ":] - ")
    ""))

(defn get-stack-trace
  "Get the Timbre stack trace value using the given error and options."
  [{:keys [no-stacktrace?] :as opts} ?err_]
  (when-not no-stacktrace?
    (when-let [err (force ?err_)]
      (timbre/stacktrace err opts))))

(def iso8601-formatter
  (time-format/formatters :date-time))

(defn get-log-map
  "Force Timber log data into an evaluated log map."
  [{:keys [level vargs msg_ ?ns-str ?line instant] :as data}]
  (let [time-obj          (time-coerce/from-date instant)
        timestamp         (time-format/unparse iso8601-formatter time-obj)]
    {:timestamp timestamp
     :level     (str/upper-case (name level))
     :ns-str    (or ?ns-str "?")
     :line      (or ?line "?")
     :message   (force msg_)}))

(defn output-json
  "Output logging data in a parseable JSON format.
  Takes an optional options map and a data map and produces a log line from it."
  ([data]
   (output-json nil data))
  ([opts {:keys [?err_] :as data}]
   (let [log-map     (get-log-map data)
         stack-trace (get-stack-trace opts ?err_)]
     (json/generate-string
      (if stack-trace
        (assoc log-map :stacktrace stack-trace)
        log-map)
      {:key-fn cskfmt/->snake_case_string}))))

(defn output-string
  "Output logging data as a human readable string.
  Takes an optional options map and a data map and produces a log line from it."
  ([data]
   (output-string nil data))
  ([opts {:keys [?err_] :as data}]
   (let [{:keys [timestamp hostname level ns-str context line message]} (get-log-map data)]
     (str timestamp " "
          level  " "
          "[" ns-str ":" line "] "
          (context-snippet context)
          message
          (when-let [stack-trace (get-stack-trace opts ?err_)]
            (str "\n" stack-trace))))))

(def shared-config
  {:middleware []})

(def json-config
  (->> (partial output-json {:stacktrace-fonts {}})
       (assoc shared-config :output-fn)))

(def string-config
  (->> output-string
       (assoc shared-config :output-fn)))

(defn active-config
  "Load the correct logger configuration based on the logging format
  environment variable."
  []
  (condp = (config/config :logging :format)
    :string string-config
    :json   json-config
    string-config))

(defstate logging-config
  :start (do
           (timbre/set-level! (config/config :logging :level))
           (timbre/merge-config! (active-config))))
