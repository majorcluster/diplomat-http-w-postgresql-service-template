(ns {{namespace}}.ports.sql.repositories.events
    (:require [next.jdbc.sql :as jdbc]
     [{{namespace}}.adapters.events :as a.events]
     [{{namespace}}.ports.sql.core :as sql.c]
     [{{namespace}}.ports.sql.repositories.entities :as r.entities]))

(defn find-all
      []
      (-> (r.entities/find-all "events")
          a.events/sql-wire->internal))

(defn find-by-interval
      [from to]
      (-> sql.c/datasource
          (jdbc/query ["select * from events where scheduled_at >= ? AND scheduled_at < ?" from to])
          a.events/sql-wire->internal))

(defn find-by-id
      [id]
      (->> id
           (r.entities/find-by-id "events")
           a.events/sql-wire->internal
           (#(if (empty? %) nil %))))

(defn insert!
      [m]
      (r.entities/insert! :events (a.events/internal->sql-wire m)))

(defn update!
      [m id]
      (r.entities/update! :events
                        (a.events/internal->sql-wire m)
                        ["id = ?" id]))

(defn delete-by-id!
      [id]
      (r.entities/delete-by-id! :events (parse-long id)))
