(ns {{namespace}}.controllers.events
    (:require [{{namespace}}.ports.sql.repositories.events :as repo.events]
     [{{namespace}}.commons :as commons]))

(defn get-by-interval
      [from to]
      (repo.events/find-by-interval from to))

(defn get-by-id
      [id]
      (repo.events/find-by-id id))

(defn post
      [m]
      (-> m
          repo.events/insert!
          commons/extract-generated-id))

(defn patch
      [m id]
      (->  m
           (dissoc m :id)
           (repo.events/update! id))
      id)

(defn delete-by-id
      [id]
      (repo.events/delete-by-id! id))
