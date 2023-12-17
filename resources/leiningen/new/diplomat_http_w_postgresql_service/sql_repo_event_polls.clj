(ns {{namespace}}.ports.sql.repositories.event-polls
    (:require [next.jdbc.sql :as jdbc]
     [{{namespace}}.adapters.event-polls :as a.event-polls]
     [{{namespace}}.adapters.events :as a.events]
     [{{namespace}}.ports.sql.core :as sql.c]
     [{{namespace}}.ports.sql.repositories.entities :as repo.ent]))

(defn find-all
      []
      (-> (repo.ent/find-all "event_polls")
          a.event-polls/sql-wire->internal))

(defn- gen-in-ps
       [col]
       (->> col
            (map-indexed (fn [index _] (if (= (count col) (inc index)) "?" "?,")))
            (reduce str)))

(defn- query-from-params
       [m]
       (apply conj [(apply str "select * from event_polls where " (interpose " AND " (map #(str (name %) " = ?") (keys m))))]
              (vals m)))

(defn find-by-params
      [params]
      (let [event-polls (-> sql.c/datasource
                            (jdbc/query (query-from-params params))
                            a.event-polls/sql-wire->internal)
            events (if (not-empty event-polls) (-> sql.c/datasource
                                                   (jdbc/query (concat [(str "select * from events where id in (" (gen-in-ps event-polls) ")")]
                                                                       (map :event_id event-polls)))
                                                   a.events/sql-wire->internal)
                                               [])
            mapped (update-vals (group-by :id events) first)]
           (map (fn [event-poll]
                    (-> event-poll
                        (assoc  :event (get mapped (:event_id event-poll)))
                        (dissoc :event_id))) event-polls)))

(defn find-by-id
      [id]
      (->> id
           (repo.ent/find-by-id "event_polls")
           a.event-polls/sql-wire->internal))

(defn insert!
      [m]
      (repo.ent/insert! :event_polls (a.event-polls/internal->sql-wire m)))

(defn update!
      [m id]
      (repo.ent/update! :event_polls
                        (a.event-polls/internal->sql-wire m)
                        ["id = ?" id]))

(defn delete-by-id!
      [id]
      (repo.ent/delete-by-id! :event_polls (parse-long id)))
