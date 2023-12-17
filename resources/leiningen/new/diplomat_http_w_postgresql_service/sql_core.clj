(ns {{namespace}}.ports.sql.core
    (:require [clojure.java.io :as io]
     [next.jdbc :as jdbc]
     [{{namespace}}.configs :as configs]
     [outpace.config :refer [defconfig]])
    (:import (clojure.lang Var$Unbound)))

(defconfig db-type)
(defconfig subname)
(defconfig subprotocol)
(defconfig host)
(defconfig port)
(defconfig db-name)
(defconfig username)
(defconfig password)

(def raw-connection
  {:dbtype db-type
   :subname subname
   :subprotocol subprotocol
   :host host
   :port port
   :dbname db-name
   :user username
   :password password
   :ssl false})

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
                       reverse)]
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

(defn start
      [migrate?]
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
