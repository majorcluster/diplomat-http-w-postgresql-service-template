(ns core-test
    (:require [clojure.test :refer :all]
     [{{namespace}}.ports.http.core :as service]
     [{{namespace}}.ports.sql.core :as sql.c]
     [io.pedestal.http :as bootstrap]))

(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

(defn setup
      []
      (sql.c/migrate-test))

(defn teardown
      []
      (sql.c/teardown))

(defn test-fixture [f]
      (setup)
      (f)
      (teardown))
