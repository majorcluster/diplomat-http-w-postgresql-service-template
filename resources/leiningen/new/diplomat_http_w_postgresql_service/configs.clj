(ns {{namespace}}.configs
    (:require [outpace.config :refer [defconfig]]))

(defconfig env)
(defconfig user-passphrase)
(defconfig admin-passphrase)

(defn env-test?
      []
      (= "test" env))
