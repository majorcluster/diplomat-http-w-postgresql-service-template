(ns {{namespace}}.adapters.commons
    (:require [clojure.instant :as instant])
    (:import [java.time Instant LocalDate LocalDateTime ZoneId ZoneOffset ZoneOffset]
     [java.time.temporal TemporalAdjusters]
     [java.time.format DateTimeFormatter]))

(defn now-zoned
      []
      (-> (Instant/now)
          (.atZone (ZoneId/systemDefault))))

(defn initialize-date
      [year month day]
      (let [now (now-zoned)]
           (LocalDate/of (if (empty? year) (-> now .getYear) (parse-long year))
                         (if (empty? month) (-> now .getMonth) (parse-long month))
                         (if (empty? day) (-> now .getDayOfMonth) (parse-long day)))))

(defn add-leading-zero
      [x]
      (if (< x 10) (str "0" x)
                   (str x)))

(defn splited-date->inst
      [year month day]
      (instant/read-instant-timestamp (format "%s-%s-%sT00:00:00.000Z"
                                              (if (empty? year) (-> (now-zoned) .getYear str) year)
                                              (if (empty? month) (-> (now-zoned) .getMonth .getValue add-leading-zero) month)
                                              (if (empty? day) (-> (now-zoned) .getDayOfMonth add-leading-zero) day))))

(defn- first-day-of-year
       [current-date]
       (.with current-date (TemporalAdjusters/firstDayOfYear)))

(defn- first-day-of-next-year
       [current-date]
       (.with current-date (TemporalAdjusters/firstDayOfNextYear)))

(defn- first-day-of-next-month
       [current-date]
       (.with current-date (TemporalAdjusters/firstDayOfNextMonth)))

(defn- first-day-of-month
       [current-date]
       (.with current-date (TemporalAdjusters/firstDayOfMonth)))

(defn- next-day
       [current-date]
       (.plusDays current-date 1))

(defn get-interval
      [year month day]
      (let [whole-year? (and (empty? day) (empty? month))
            from-fn     (cond whole-year? first-day-of-year
                              (empty? day) first-day-of-month
                              :else identity)
            to-fn       (cond whole-year? first-day-of-next-year
                              (empty? day) first-day-of-next-month
                              :else next-day)]
           {:from (-> (initialize-date year month day) from-fn)
            :to   (-> (initialize-date year month day) to-fn)}))

(defn update-when-not-nil
      [m k f]
      (cond (map? m)
            (if (contains? m k) (update m k f)
                                m)
            (nil? m) m
            :else (map #(update-when-not-nil % k f) m)))

(defn str->sql-timestamp
      [date]
      (-> date
          LocalDateTime/parse
          (.atZone (ZoneId/systemDefault))
          (.withZoneSameInstant (ZoneId/of "UTC"))
          (.toLocalDateTime)
          (.toInstant (ZoneOffset/UTC))))

(defn sql-timestamp->str
      [date]
      (-> date
          (.toInstant)
          (.atZone (ZoneId/systemDefault))
          (.format (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss"))))

(defn now-str
      []
      (-> (LocalDateTime/now)
          (.format (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss"))))
