(ns {{namespace}}.ports.sql.core
    (:require [clojure.java.io :as io]
     [{{namespace}}.configs :as configs]
     [next.jdbc :as jdbc]
     [next.jdbc.prepare :as jdbc.prepare]
     [next.jdbc.result-set :as jdbc.result-set]
     [outpace.config :refer [defconfig]])
    (:import (clojure.lang LazySeq PersistentVector Var$Unbound)
     [java.sql
      Array
      Date
      PreparedStatement
      Time
      Timestamp]))

(defconfig url "")
(defconfig username "")
(defconfig password "")
(def raw-connection
  (if (.contains url "h2")
    {:dbtype "h2:mem"
     :dbname "firma-analysis"
     :user username
     :password password
     :ssl false}
    {:jdbcUrl url
     :user username
     :password password
     :ssl false}))

(def ^:private connection ;removes unbound values
  (into {} (filter (fn [[_ v]]
                       (not
                        (= (class v) Var$Unbound)))
                   raw-connection)))

(def datasource
  (jdbc/get-datasource connection))

(defn exec-migration-file
      [_ file]
      (println "migrating" (.getName file))
      (let [sql-command (slurp file)]
           (jdbc/with-transaction
            [tx datasource]
            (jdbc/execute! tx [sql-command]))))

(defn migrate
      []
      (let [files (->> (io/resource "migrations")
                       io/file
                       file-seq
                       (filter #(re-matches #"^[1-9]{1,}.sql$" (.getName %)))
                       (sort-by #(.getName %)))]
           (jdbc/with-transaction
            [tx datasource]
            (mapv #(exec-migration-file tx %) files))))

(defn- file-seq-if-existing
       [folder]
       (if folder (file-seq folder)
                  []))

(defn- get-files-map
       [res-path]
       (->> (io/resource res-path)
            io/file
            file-seq-if-existing
            (filter #(re-matches #"^[1-9]{1,}.sql$" (.getName %)))
            (sort-by #(.getName %))
            (mapv (fn [f] (let [n (.getName f)] [n f])))
            (into {})))

(defn migrate-test
      []
      (let [files (get-files-map "migrations")
            test-files (->> (get-files-map "migrations_test")
                            (merge files))]
           (jdbc/with-transaction
            [tx datasource]
            (mapv #(exec-migration-file tx %) (vals test-files)))))

(defn teardown
      []
      (let [t-file  (io/file "resources/migrations/teardown.sql")]
           (jdbc/with-transaction
            [tx datasource]
            (exec-migration-file tx t-file))))

(defn convert-sql-array [^Array v]
      (let [x (first (.getArray v))
            f (cond (instance? Date x) #(.toLocalDate %)
                    (instance? Time x) #(.toLocalTime %)
                    (instance? Timestamp x) #(.toLocalDateTime %)
                    :else identity)]
           (vec (map f (.getArray v)))))

(defn override-protocols
      []
      (extend-protocol jdbc.prepare/SettableParameter
                       PersistentVector
                       (set-parameter [^PersistentVector v ^PreparedStatement ps ^long i]
                                      (.setObject ps i (into-array v)))
                       LazySeq
                       (set-parameter [^LazySeq v ^PreparedStatement ps ^long i]
                                      (.setObject ps i (into-array v))))
      (extend-protocol jdbc.result-set/ReadableColumn
                       Array
                       (read-column-by-label [^Array v _]
                                             (convert-sql-array v))
                       (read-column-by-index [^Array v _2 _3]
                                             (convert-sql-array v))))

(defn start
      [migrate?]
      (override-protocols)
      (cond migrate? (do
                       (when (configs/env-test?)
                             (teardown))
                       (migrate)
                       (when (configs/env-test?)
                             (teardown)))
            :else nil))

(defn reset
      []
      (teardown)
      (migrate))
