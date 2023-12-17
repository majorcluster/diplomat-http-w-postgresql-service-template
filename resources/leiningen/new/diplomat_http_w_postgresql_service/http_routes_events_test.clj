(ns {{namespace}}.ports.http.routes.events-test
    (:require [clojure.data.json :as cjson]
     [clojure.string :as cstr]
     [clojure.test :refer :all]
     [core-test :refer [service test-fixture]]
     [{{namespace}}.controllers.events :as c.events]
     [io.pedestal.test :refer [response-for]]
     [matcher-combinators.test]))

(use-fixtures :each test-fixture)

(def root-auth-headers {"Content-Type" "application/json"
                        "X-TOKEN" "dobry den"})
(def admin-auth-headers {"Content-Type" "application/json"
                         "X-TOKEN" "dobrou noc"})

(defn get-id-from-resp
      [resp]
      (get (-> (:body resp)
               (cjson/read-str))
           "id"))

(deftest user-unauthorized-test
         (testing "unauthorized"
                  (is (= 401
                         (:status (response-for service :get "/events"))))
                  (is (= 401
                         (:status (response-for service :get "/events"
                                                :headers {"Content-Type" "application/json"
                                                          "X-TOKEN" "dobre rano"}))))))

(deftest get-events
         (let [event {:title "Such event"
                      :city "Mauá"
                      :location "Centro"
                      :scheduled_at "2023-01-01T10:30:00"}]
              (testing "no events are returned"
                       (is (= "{\"events\":[]}"
                              (:body (response-for service :get "/events"
                                                   :headers root-auth-headers)))))
              (testing "for matching interval"
                       (let [id-1       (c.events/post event)
                             resp-year  (response-for service :get "/events?year=2023"
                                                      :headers root-auth-headers)
                             resp-month (response-for service :get "/events?year=2023&month=01"
                                                      :headers root-auth-headers)
                             resp-day   (response-for service :get "/events?year=2023&month=01&day=01"
                                                      :headers root-auth-headers)]
                            (are [resp] (= {:events [(assoc event
                                                            :id id-1
                                                            :confirmed nil
                                                            :absent nil)]}
                                           (-> resp :body (cjson/read-str :key-fn keyword)))
                                 resp-year resp-month resp-day)
                            (are [resp] (= 200
                                           (:status resp))
                                 resp-year resp-month resp-day)
                            (let [event-2 {:title "Crazy event"
                                           :city "Santo André"
                                           :location "Utinga"
                                           :scheduled_at "2023-02-01T10:30:00"}
                                  event-3 {:title "Crazier event"
                                           :city "Santo André"
                                           :location "Utinga"
                                           :scheduled_at "2023-02-02T10:30:00"}
                                  event-4 {:title "Insane event"
                                           :city "Santo André"
                                           :location "Utinga"
                                           :scheduled_at "2024-01-01T10:30:00"}
                                  id-2 (c.events/post event-2)
                                  id-3 (c.events/post event-3)
                                  _ (c.events/post event-4)
                                  resp-year  (response-for service :get "/events?year=2023"
                                                           :headers root-auth-headers)
                                  resp-month (response-for service :get "/events?year=2023&month=01"
                                                           :headers root-auth-headers)
                                  resp-day   (response-for service :get "/events?year=2023&month=02&day=01"
                                                           :headers root-auth-headers)]
                                 (is (= [(assoc event
                                                :id id-1 :confirmed nil :absent nil)
                                         (assoc event-2
                                                :id id-2 :confirmed nil :absent nil)
                                         (assoc event-3
                                                :id id-3 :confirmed nil :absent nil)]
                                        (-> resp-year :body (cjson/read-str :key-fn keyword) :events)))
                                 (is (= 200
                                        (:status resp-year)))
                                 (is (= [(assoc event
                                                :id id-1 :confirmed nil :absent nil)]
                                        (-> resp-month :body (cjson/read-str :key-fn keyword) :events)))
                                 (is (= 200
                                        (:status resp-month)))
                                 (is (= [(assoc event-2
                                                :id id-2 :confirmed nil :absent nil)]
                                        (-> resp-day :body (cjson/read-str :key-fn keyword) :events)))
                                 (is (= 200
                                        (:status resp-day))))))))

(deftest get-event
         (let [event-1 {:title "Such event"
                        :city "Mauá"
                        :location "Centro"
                        :scheduled_at "2023-01-01T10:30:00"}
               event-2  {:title "Crazy event"
                         :city "Santo André"
                         :location "Utinga"
                         :scheduled_at "2023-02-01T10:30:00"}
               id-1 (c.events/post event-1)
               id-2 (c.events/post event-2)]
              (testing "once valid id is sent, event is returned"
                       (let [resp (response-for service :get (str "/events/" id-1)
                                                :headers root-auth-headers)]
                            (is (= (assoc event-1
                                          :id id-1 :confirmed nil :absent nil)
                                   (-> resp :body (cjson/read-str :key-fn keyword))))
                            (is (= 200
                                   (:status resp))))
                       (let [resp (response-for service :get (str "/events/" id-2)
                                                :headers root-auth-headers)]
                            (is (= (assoc event-2
                                          :id id-2 :confirmed nil :absent nil)
                                   (-> resp :body (cjson/read-str :key-fn keyword))))
                            (is (= 200
                                   (:status resp)))))
              (testing "once invalid id is sent, 404 is returned"
                       (let [resp (response-for service :get "/events/473a4e4a-b4ec-44ea-a2be-ac67b9d9e205"
                                                :headers root-auth-headers)]
                            (is (= 404
                                   (:status resp))))
                       (let [resp (response-for service :get "/events/aaay"
                                                :headers root-auth-headers)]
                            (is (= 404
                                   (:status resp)))))))

(deftest post-event
         (testing "non-admin is not authorized"
                  (is (= 401
                         (:status (response-for service  :post "/events"
                                                :headers root-auth-headers
                                                :body "")))))
         (testing "missing fields are validated with 400"
                  (let [resp (response-for service  :post "/events"
                                           :headers admin-auth-headers
                                           :body "")]
                       (is (= (-> resp :body (cjson/read-str :key-fn keyword) :validation_messages count)
                              10))
                       (is (= 400
                              (:status resp)))))
         (testing "when mandatory fields are present, 201 with location is returned and event is placed on db"
                  (let [event-1 {:title "Such event"
                                 :city "Mauá"
                                 :location "Centro"
                                 :scheduled_at "2023-01-01T10:30:00"}
                        event-json (cjson/write-str event-1)
                        resp (response-for service  :post "/events"
                                           :headers admin-auth-headers
                                           :body event-json)
                        id (get-id-from-resp resp)
                        db-found (c.events/get-by-id id)]
                       (is (= (format "{\"id\":%s}" id)
                              (:body resp)))
                       (is (= 201
                              (:status resp)))
                       (is (cstr/ends-with?
                            (get-in resp [:headers "Location"])
                            (str "/events/" id)))
                       (is (= (assoc event-1 :id id :confirmed nil :absent nil)
                              db-found)))))

(deftest patch-event
         (testing "missing fields are validated with 400"
                  (let [resp (response-for service :patch "/events"
                                           :headers admin-auth-headers
                                           :body "")]
                       (is (= (-> resp :body (cjson/read-str :key-fn keyword) :validation_messages)
                              [{:field "id", :message "Field id is not present"}]))
                       (is (= 400
                              (:status resp)))))
         (testing "when mandatory fields are present, 200 is returned and event is updated on db"
                  (let [event-1 {:title "Such event"
                                 :city "Mauá"
                                 :location "Centro"
                                 :scheduled_at "2023-01-01T10:30:00"}
                        id (c.events/post event-1)
                        event-2 {:id id
                                 :title "Crazy event"
                                 :city "Santo André"
                                 :location "Utinga"
                                 :scheduled_at "2023-02-01T10:30:00"}
                        event-2-json (cjson/write-str event-2)
                        resp (response-for service
                                           :patch "/events"
                                           :headers admin-auth-headers
                                           :body event-2-json)
                        db-found (c.events/get-by-id id)]
                       (is (= (format "{\"id\":%s}" id)
                              (:body resp)))
                       (is (= 200
                              (:status resp)))
                       (is (match? event-2 db-found))
                       (let [event-3 {:id id
                                      :city "Diadema"}
                             event-3-json (cjson/write-str event-3)
                             resp (response-for service
                                                :patch "/events"
                                                :headers admin-auth-headers
                                                :body event-3-json)
                             db-found (c.events/get-by-id id)]
                            (is (= (format "{\"id\":%s}" id)
                                   (:body resp)))
                            (is (= 200
                                   (:status resp)))
                            (is (match? (merge event-2 event-3)
                                        db-found))))))

(deftest delete-event
         (testing "not present event delete returns 200"
                  (let [resp (response-for service
                                           :delete "/events/1"
                                           :headers admin-auth-headers)]
                       (is (= 200
                              (:status resp)))))
         (testing "when id is present, event is deleted"
                  (let [event {:title "Such event"
                               :city "Mauá"
                               :location "Centro"
                               :scheduled_at "2023-01-01T10:30:00"}
                        id (c.events/post event)
                        db-found-before (c.events/get-by-id id)
                        resp (response-for service
                                           :delete (str "/events/" id)
                                           :headers admin-auth-headers)
                        db-found-after (c.events/get-by-id id)]
                       (is (= 200
                              (:status resp)))
                       (is (match? (merge {:id id} event) db-found-before))
                       (is (nil? db-found-after)))))
