(ns {{namespace}}.ports.http.routes.events
    (:require [{{namespace}}.adapters.commons :as adapters.commons]
     [{{namespace}}.controllers.events :as c.events]
     [{{namespace}}.ports.http.routes.commons :refer [json-header]]
     [pedestal-api-helper.params-helper :as ph])
    (:import (clojure.lang ExceptionInfo)))

(defn get-events
      [request]
      (let [{:keys [year month day]} (-> request :query-params)
            {:keys [from to]} (adapters.commons/get-interval year month day)
            result {:events (c.events/get-by-interval from to)}]
           {:status 200 :headers json-header :body result}))

(defn get-event
      [request]
      (cond (re-matches #"^[0-9]+$" (-> (:path-params request)
                                        :id)) (let [result (-> request :path-params :id
                                                               Integer/parseInt
                                                               c.events/get-by-id)
                                                    not-found? (nil? result)]
                                                   (cond not-found? {:status 404 :headers json-header :body {}}
                                                         :else {:status 200 :headers json-header :body result}))
            :else {:status 404 :headers json-header :body {}}))

(defn post-event
      [request]
      (try
        (let [result-id (-> (get request :json-params {})
                            (ph/validate-and-mop!!
                             {"title"        [{:validate/type :validate/mandatory}
                                              {:validate/type :validate/min, :validate/value 2}
                                              {:validate/type :validate/max, :validate/value 100}]
                              "city"         [{:validate/type :validate/mandatory}
                                              {:validate/type :validate/regex, :validate/value #"^(Diadema)|(Mauá)|(Santo André)|(São Bernardo)|(São Caetano)|(Ribeirão Pires)|(Rio Grande da Serra)$"}]
                              "location"     [{:validate/type :validate/mandatory}
                                              {:validate/type :validate/min, :validate/value 2}
                                              {:validate/type :validate/max, :validate/value 100}]
                              "scheduled_at" [{:validate/type :validate/mandatory}
                                              {:validate/type :validate/regex, :validate/value #"^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})$"}]}
                             ["title","city","location","scheduled_at"])
                            c.events/post)]
             {:status 201
              :headers (merge json-header {"Location" (str "http://localhost:8080/events/" result-id)})
              :body {:id result-id}})
        (catch ExceptionInfo e
          (->> e
               ex-data
               :validation-messages
               (assoc-in {:status 400 :headers json-header :body {:validation-messages []}}
                         [:body :validation-messages])))))

(defn convert-id
      [id]
      (cond (string? id) (Integer/parseInt id)
            :else id))

(defn patch-event
      [request]
      (try
        (let [event (get request :json-params {})
              event-id (->  event
                            (ph/validate-and-mop!!
                             {"id"           [{:validate/type :validate/mandatory}]
                              "title"        [{:validate/type :validate/min, :validate/value 2, :validate/ignore-if-absent true}
                                              {:validate/type :validate/max, :validate/value 100, :validate/ignore-if-absent true}]
                              "city"         [{:validate/type :validate/regex, :validate/value #"^(Diadema)|(Mauá)|(Santo André)|(São Bernardo)|(São Caetano)|(Ribeirão Pires)|(Rio Grande da Serra)$",
                                               :validate/ignore-if-absent true}]
                              "location"     [{:validate/type :validate/min, :validate/value 2, :validate/ignore-if-absent true}
                                              {:validate/type :validate/max, :validate/value 100, :validate/ignore-if-absent true}]
                              "scheduled_at" [{:validate/type :validate/regex, :validate/value #"^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})$",
                                               :validate/ignore-if-absent true}]}
                             ["id","title","city",
                              "location","scheduled_at"
                              "confirmed","absent"])
                            (c.events/patch (-> event :id convert-id)))]
             {:status 200
              :headers json-header
              :body {:id event-id}})
        (catch ExceptionInfo e
          (->> (.getData e)
               :validation-messages
               (assoc-in {:status 400 :headers json-header :body {:validation-messages []}}
                         [:body :validation-messages])))))

(defn delete-event
      [request]
      (try
        (c.events/delete-by-id (get-in request [:path-params :id]))
        {:status 200}
        (catch Exception _
          {:status 404})))
