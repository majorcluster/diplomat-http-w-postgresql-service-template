(ns {{namespace}}.controllers.event-polls
    (:require [{{namespace}}.ports.sql.repositories.event-polls :as repo.event-polls]
     [{{namespace}}.commons :as commons]))

(defn get-by-params
      [active]
      (repo.event-polls/find-by-params {:active active}))

(defn get-by-id
      [id]
      (first (repo.event-polls/find-by-params {:id id})))

(defn post
      [m]
      (-> m
          repo.event-polls/insert!
          commons/extract-generated-id))
