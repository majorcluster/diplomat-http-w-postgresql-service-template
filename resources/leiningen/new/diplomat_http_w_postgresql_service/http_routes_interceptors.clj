(ns {{namespace}}.ports.http.routes.interceptors
    (:require [clojure.data.json :as cjson]
     [{{namespace}}.configs :as configs]
     [clj-data-adapter.core :as data-adapters])
    (:import [clojure.lang ExceptionInfo]))

(defn convert-to-json
      [m]
      (cond (map? m) (cjson/write-str m :key-fn data-adapters/kebab-key->snake-str
                                      :escape-unicode false)
            :else m))

(defn json-out
      []
      {:name ::json-out
       :leave (fn [context]
                  (->> (:response context)
                       :body
                       convert-to-json
                       (assoc-in context [:response :body])))})

(defn authorization-error
      "Throws an authorization error"
      ([]
       (authorization-error "Unauthorized" {}))
      ([message]
       (authorization-error message {}))
      ([message data]
       (throw (ex-info "Unauthorized"
                       {:type :unauthorized
                        :message message
                        :reason data}))))

(def authz-user
  {:name ::authz-user
   :enter (fn [context]
              (try
                (let [token (get-in context [:request :headers "x-token"] "")]
                     (cond (or (= token configs/user-passphrase)
                               (= token configs/admin-passphrase)) context
                           :else (authorization-error)))
                (catch ExceptionInfo e
                  (println "error authorizing" (ex-cause e) (ex-message e))
                  (authorization-error))))})

(def authz-admin
  {:name ::authz-admin
   :enter (fn [context]
              (try
                (let [token (get-in context [:request :headers "x-token"] "")]
                     (cond (= token configs/admin-passphrase) context
                           :else (authorization-error)))
                (catch ExceptionInfo e
                  (println "error authorizing" (ex-cause e) (ex-message e))
                  (authorization-error))))})
