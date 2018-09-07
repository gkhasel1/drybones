(ns drybones.handlers
  (:require
   [bidi.ring :as bidi]
   [ring.util.response :as response]
   [ring.middleware.format :refer [wrap-restful-format]]
   [taoensso.timbre :as timbre]
   [clojure.walk :as walk]))

(defn index-handler
  [req]
  (response/response "[ OK ] Drybones."))

(defn not-found-handler
  [req]
  (-> (response/response {:error :not-found})
      (response/status 404)))

(def route-map
  ["/" [[""       :index]
        [true     :not-found]]])

(def handler-map
  {:index     index-handler
   :not-found not-found-handler})

(def app-handler
  (-> handler-map
      (walk/postwalk-replace route-map)
      (bidi/make-handler)
      (wrap-restful-format)))
