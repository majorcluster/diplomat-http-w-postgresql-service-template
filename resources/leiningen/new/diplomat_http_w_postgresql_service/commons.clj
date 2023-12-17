(ns {{namespace}}.commons
    (:require [clojure.string :as cstr]))

(defn remove-namespace [data]
      (if (map? data) (into {} (for [[k v] data]
                                    [(-> k name cstr/lower-case keyword) v]))
                      (reduce #(conj % (remove-namespace %2)) [] data)))

(defn extract-generated-id [m]
      (-> m remove-namespace :id))
