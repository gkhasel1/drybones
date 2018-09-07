(ns drybones.web-server
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :as test]
   [mount.core :refer [defstate]]
   [org.httpkit.server :as http]
   [taoensso.timbre :as timbre]
   [drybones.handlers :as handlers]
   [drybones.config :as config]))

(s/def ::ip string?)
(s/def ::port (s/int-in 1 65536))

(s/def ::config (s/keys :opt-un [::ip
                                 ::port]))

(s/fdef start
        :args (s/nilable (s/cat :opts ::config)))
(defn start
  "Start the HTTP Kit web server with the environment config."
  ([]
   (-> (config/config :web-server) (start)))
  ([opts]
   (if-let [server (http/run-server #'handlers/app-handler opts)]
     (do
       (timbre/info {:message "Web server started"
                     :opts    opts})
       server)
     (timbre/error {:message "Web server failed to start"
                    :opts    opts}))))

(s/fdef stop
        :args (s/cat :fn test/function?))
(defn stop
  "Stop the HTTP Kit web server.

  HTTP Kit's `(run-server)` function starts the web server and
  returns a function that can be used to stop the server. This
  function takes that function and calls it."
  [server]
  (try
    (server)
    (timbre/info {:message "Web server stopped"})
    (catch Throwable t
      (do
        (timbre/error {:message "Failed to stop web server"
                       :error   t})
        (throw t)))))

(defstate web-server
  :start (start)
  :stop  (stop web-server))
