(ns {{namespace}}.ports.sql.repositories.entities
    (:require [{{namespace}}.ports.sql.core :as sql.c]
     [next.jdbc.sql :as jdbc]))

(defn find-all
      [entity]
      (jdbc/query sql.c/datasource [(format "select * from %s" entity)]))

(defn find-by-id
      [entity id]
      (-> sql.c/datasource
          (jdbc/query [(format "select * from %s where id = ?" entity) id])
          first))

(defn insert!
      [entity m]
      (jdbc/insert! sql.c/datasource entity m))

(defn update!
      [entity set-map where-clause]
      (jdbc/update! sql.c/datasource entity set-map where-clause))

(defn delete-by-id!
      [entity id]
      (jdbc/delete! sql.c/datasource entity ["id = ?" id]))
