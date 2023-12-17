(ns {{namespace}}.ports.http.routes.error-handler
    (:require [clojure.data.json :as cjson]
     [io.pedestal.interceptor.error :refer [error-dispatch]])
    (:import (clojure.lang ExceptionInfo)))

(defn- get-exception-data
       [exception]
       (or (-> exception
               ex-data
               :exception
               ex-data)
           (try
             (-> exception
                 ex-data
                 :exception
                 .getCause
                 ex-data)
             (catch Exception _
               nil))))

(defn- get-exception-type
       [exception]
       (-> exception
           get-exception-data
           :type))

(defn- resp-custom-ex
       [exception status]
       (let [body (get-exception-data exception)
             body (cond (map? body) (cjson/write-str body)
                        :else body)]
            {:status status
             :body body
             :headers {"Content-Type" "application/json"}}))

(def service-error-handler
  (error-dispatch
   [context exception]

   [{:exception-type ExceptionInfo}]
   (condp = (get-exception-type exception)
          :invalid-schema (assoc context :response (resp-custom-ex exception 400))
          :unauthorized (assoc context :response (resp-custom-ex exception 401))
          :bad-format (assoc context :response (resp-custom-ex exception 400))
          :duplicated (assoc context :response (resp-custom-ex exception 400))
          (assoc context :io.pedestal.interceptor.chain/error exception))

   :else
   (assoc context :io.pedestal.interceptor.chain/error exception)))
