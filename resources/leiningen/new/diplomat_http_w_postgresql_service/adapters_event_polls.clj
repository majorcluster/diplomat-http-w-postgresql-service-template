(ns {{namespace}}.adapters.event-polls
    (:require [clj-data-adapter.core :as data-adapter]
     [{{namespace}}.commons :as commons]
     [{{namespace}}.adapters.commons :as a.commons]))

(defn attach-default-values
      [wire]
      (cond-> wire
              (not (contains? wire :active))     (assoc :active true)
              (not (contains? wire :created_at)) (assoc :created_at (-> (a.commons/now-str)
                                                                        a.commons/str->sql-timestamp))))

(defn internal->sql-wire
      [wire]
      (->> wire
           attach-default-values
           (data-adapter/transform-keys #(-> % data-adapter/kebab-key->snake-str keyword))))

(defn sql-wire->internal
      [wire]
      (-> wire commons/remove-namespace (a.commons/update-when-not-nil :created_at a.commons/sql-timestamp->str)))
