(ns {{namespace}}.server
    (:gen-class) ; for -main method in uberjar
    (:require [{{namespace}}.ports.core :as ports.c]))

(defn run-dev
      "The entry-point for 'lein run-dev'"
      [& _]
      (println "\nCreating your [DEV] server...")
      (ports.c/start-ports-dev false))

(defn run-dev-w-migration
      "The entry-point for 'lein run-dev'"
      [& _]
      (println "\nCreating your [DEV] server...")
      (ports.c/start-ports-dev true))

(defn -main
      "The entry-point for 'lein run'"
      [& args]
      (println "\nCreating your server...")
      (let [migrate? (->> args
                          (filter #(= "--migrate" %))
                          first
                          boolean)]
           (ports.c/start-ports migrate?)))
