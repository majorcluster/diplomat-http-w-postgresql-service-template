(ns {{namespace}}.ports.http.routes.event-polls-test
    (:require [clojure.data.json :as cjson]
     [clojure.string :as cstr]
     [clojure.test :refer :all]
     [core-test :refer [service test-fixture]]
     [{{namespace}}.controllers.events :as c.events]
     [{{namespace}}.controllers.event-polls :as c.event-polls]
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

(deftest get-event-polls
         (let [event {:title "Such event"
                      :city "Mauá"
                      :location "Centro"
                      :scheduled_at "2023-01-01T10:30:00"}
               event-poll {:title "My poll"
                           :active true
                           :created_at "2023-01-01T10:30:00"}]
              (testing "no event polls are returned"
                       (is (= "{\"event_polls\":[]}"
                              (:body (response-for service :get "/event-polls"
                                                   :headers root-auth-headers)))))
              (testing "polls are returned"
                       (let [event-1-id      (c.events/post event)
                             event-poll-1-id (c.event-polls/post (assoc event-poll :event_id event-1-id))
                             resp-no-param   (response-for service :get "/event-polls"
                                                           :headers root-auth-headers)
                             resp-active     (response-for service :get "/event-polls?active=true"
                                                           :headers root-auth-headers)]
                            (are [resp] (= [(assoc event-poll :event (assoc event
                                                                            :id event-1-id :confirmed nil :absent nil)
                                                   :id event-poll-1-id :voted nil)]
                                           (-> resp :body (cjson/read-str :key-fn keyword) :event_polls))
                                 resp-no-param
                                 resp-active)
                            (is (= []
                                   (-> (response-for service :get "/event-polls?active=false"
                                                     :headers root-auth-headers) :body (cjson/read-str :key-fn keyword) :event_polls)))))))

(deftest get-event-poll
         (let [event           {:title "Such event"
                                :city "Mauá"
                                :location "Centro"
                                :scheduled_at "2023-01-01T10:30:00"}
               event-poll      {:title "My poll"
                                :active true
                                :created_at "2023-01-01T10:30:00"}
               event-1-id      (c.events/post event)
               event-poll-1-id (c.event-polls/post (assoc event-poll :event_id event-1-id))]
              (testing "polls are returned"
                       (let [resp   (response-for service :get (str "/event-polls/" event-poll-1-id)
                                                  :headers root-auth-headers)]
                            (= (assoc event-poll :event (assoc event
                                                               :id event-1-id :confirmed nil :absent nil)
                                      :id event-poll-1-id :voted nil)
                               (-> resp :body (cjson/read-str :key-fn keyword)))))
              (testing "once invalid id is sent, 404 is returned"
                       (let [resp (response-for service :get "/event-polls/473a4e4a-b4ec-44ea-a2be-ac67b9d9e205"
                                                :headers root-auth-headers)]
                            (is (= 404
                                   (:status resp))))
                       (let [resp (response-for service :get "/event-polls/aaay"
                                                :headers root-auth-headers)]
                            (is (= 404
                                   (:status resp)))))))

(deftest post-event-poll
         (testing "missing fields are validated with 400"
                  (let [resp (response-for service  :post "/event-polls"
                                           :headers admin-auth-headers
                                           :body "")]
                       (is (= (-> resp :body (cjson/read-str :key-fn keyword) :validation_messages count)
                              5))
                       (is (= 400
                              (:status resp)))))
         (with-redefs [{{namespace}}.adapters.commons/now-str (fn [] "2023-07-01T10:00:00")]
                      (testing "when mandatory fields are present, 201 with location is returned and event-poll is placed on db"
                               (let [event-1 {:title "Such event"
                                              :city "Mauá"
                                              :location "Centro"
                                              :scheduled_at "2023-01-01T10:30:00"}
                                     event-1-id (c.events/post event-1)
                                     event-poll-1 {:title "Such poll"
                                                   :event_id event-1-id}
                                     event-poll-json (cjson/write-str event-poll-1)
                                     resp (response-for service  :post "/event-polls"
                                                        :headers admin-auth-headers
                                                        :body event-poll-json)
                                     id (get-id-from-resp resp)
                                     db-found (c.event-polls/get-by-id id)]
                                    (is (= (format "{\"id\":%s}" id)
                                           (:body resp)))
                                    (is (= 201
                                           (:status resp)))
                                    (is (cstr/ends-with?
                                         (get-in resp [:headers "Location"])
                                         (str "/event-polls/" id)))
                                    (is (= (assoc {:title "Such poll"}
                                                  :id id
                                                  :active true
                                                  :created_at "2023-07-01T10:00:00"
                                                  :voted nil
                                                  :event (assoc event-1 :id event-1-id
                                                                :confirmed nil
                                                                :absent nil))
                                           db-found))))))
