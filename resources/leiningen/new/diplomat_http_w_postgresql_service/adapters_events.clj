(ns {{namespace}}.adapters.events
    (:require [clj-data-adapter.core :as data-adapter]
     [{{namespace}}.commons :as commons]
     [{{namespace}}.adapters.commons :as a.commons]))

(defn internal->sql-wire
      [wire]
      (->> (a.commons/update-when-not-nil wire :scheduled_at a.commons/str->sql-timestamp)
           (data-adapter/transform-keys #(-> % data-adapter/kebab-key->snake-str keyword))))

(defn sql-wire->internal
      [wire]
      (-> wire commons/remove-namespace (a.commons/update-when-not-nil :scheduled_at a.commons/sql-timestamp->str)))
