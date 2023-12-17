(ns {{namespace}}.ports.http.routes.event-polls
    (:require [{{namespace}}.controllers.event-polls :as c.event-polls]
     [{{namespace}}.ports.http.routes.commons :refer [json-header]]
     [pedestal-api-helper.params-helper :as ph])
    (:import (clojure.lang ExceptionInfo)))

(defn get-event-polls
      [request]
      (let [active (get-in request [:query-params :active] true)
            result {:event-polls (c.event-polls/get-by-params active)}]
           {:status 200 :headers json-header :body result}))

(defn get-event-poll
      [request]
      (cond (re-matches #"^[0-9]+$" (-> (:path-params request)
                                        :id)) (let [result (-> request :path-params :id
                                                               Integer/parseInt
                                                               c.event-polls/get-by-id)
                                                    not-found? (nil? result)]
                                                   (cond not-found? {:status 404 :headers json-header :body {}}
                                                         :else      {:status 200 :headers json-header :body result}))
            :else {:status 404 :headers json-header :body {}}))

(defn post-event-poll
      [request]
      (try
        (let [result-id (-> (get request :json-params {})
                            (ph/validate-and-mop!!
                             {"event_id"     [{:validate/type :validate/mandatory}
                                              {:validate/type :validate/custom, :validate/value number?}]
                              "active"       [{:validate/type :validate/custom, :validate/value boolean?, :validate/ignore-if-absent true}]
                              "title"        [{:validate/type :validate/mandatory}
                                              {:validate/type :validate/min, :validate/value 2}
                                              {:validate/type :validate/max, :validate/value 100}]}
                             ["event_id","title","active"])
                            c.event-polls/post)]
             {:status 201
              :headers (merge json-header {"Location" (str "http://localhost:8080/event-polls/" result-id)})
              :body {:id result-id}})
        (catch ExceptionInfo e
          (->> e
               ex-data
               :validation-messages
               (assoc-in {:status 400 :headers json-header :body {:validation-messages []}}
                         [:body :validation-messages])))))
