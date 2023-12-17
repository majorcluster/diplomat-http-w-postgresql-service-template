(ns {{namespace}}.ports.core
    (:require [{{namespace}}.ports.http.core :as http.c]
     [{{namespace}}.ports.sql.core :as sql.c]))

(defn start-ports-dev
      [migrate?]
      (sql.c/start migrate?)
      (http.c/start-dev))

(defn start-ports
      [migrate?]
      (sql.c/start migrate?)
      (http.c/start))
