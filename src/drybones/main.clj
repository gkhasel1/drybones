(ns drybones.main
  (:gen-class)
  (:require
   [clojure.string :as str]
   [clojure.tools.cli :as cli]
   [mount.core :as mount]
   [taoensso.timbre :as timbre]
   [drybones.logging]
   [drybones.web-server]
   [drybones.runtime :as runtime]))

(defn usage
  [_]
  (->> ["Usage:"
        "  start    Start a new server"
        ""]
       (str/join \newline)))

(defn error-msg
  [errors]
  (str "The following errors occured while parsing your command:\n\n"
       (str/join \newline errors)))

(defn exit
  "Stop the service, issuing an error message and the
  specified exit code."
  [status msg]
  (timbre/info msg)
  (System/exit status))

(defn start
  "Start all internal stateful components."
  []
  (mount/start)
  (runtime/add-shutdown-hook ::stop-system #(mount/stop))
  (timbre/info {:message "service started"}))

(defn -main
  "Entry point of the production runtime."
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args [])]
    ;; Handle help and error conditions
    (cond
      (:help options)         (exit 0 (usage summary))
      (= (count arguments) 0) (exit 1 (usage summary))
      errors                  (exit 1 (error-msg errors)))

    ;; Execute program with options
    (case (first arguments)
      "start" (start)
      (exit 1 (usage summary)))))
