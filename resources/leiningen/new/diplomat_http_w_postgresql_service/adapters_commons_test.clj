(ns {{namespace}}.adapters.commons-test
    (:require [clojure.test :refer :all]
     [{{namespace}}.adapters.commons :as commons])
    (:import [java.time LocalDate ZoneId]))

(deftest add-leading-zero-test
         (testing "no leading zero"
                  (are [expected month-sent] (= expected (commons/add-leading-zero month-sent))
                       "12" 12
                       "10" 10))
         (testing "with leading zero"
                  (are [expected month-sent] (= expected (commons/add-leading-zero month-sent))
                       "02" 2
                       "01" 1
                       "09" 9)))

(deftest splited-date->inst-test
         (testing "missing params"
                  (with-redefs-fn {#'commons/now-zoned #(-> #inst"2023-06-01T00:00:00.000-00:00" .toInstant (.atZone (ZoneId/of "UTC")))}
                                  #(are [expected year month day] (= expected (commons/splited-date->inst year month day))
                                        #inst"2023-06-20T00:00:00.000-00:00" nil nil "20"
                                        #inst"2023-06-20T00:00:00.000-00:00" "" nil "20"
                                        #inst"2023-06-01T00:00:00.000-00:00" nil nil "01"
                                        #inst"2023-06-01T00:00:00.000-00:00" nil "" "01"
                                        #inst"2022-06-01T00:00:00.000-00:00" "2022" nil "01"
                                        #inst"2023-04-01T00:00:00.000-00:00" nil "04" "01")))
         (testing "with all params"
                  (are [expected year month day] (= expected (commons/splited-date->inst year month day))
                       #inst"2020-12-01T00:00:00.000-00:00" "2020" "12" "01"
                       #inst"2024-01-31T00:00:00.000-00:00" "2024" "01" "31")))

(deftest initialize-date-test
         (testing "All params"
                  (are [expected year month day] (.isEqual expected (commons/initialize-date year month day))
                       (LocalDate/of 2022 01 02) "2022" "01" "02"
                       (LocalDate/of 2019 01 31) "2019" "01" "31"))
         (testing "Missing params"
                  (with-redefs-fn {#'commons/now-zoned #(-> #inst"2023-06-01T00:00:00.000-00:00" .toInstant (.atZone (ZoneId/of "UTC")))}
                                  #(are [expected year month day] (.isEqual expected (commons/initialize-date year month day))
                                        (LocalDate/of 2023 06 01) nil nil nil
                                        (LocalDate/of 2019 01 01) "2019" "01" nil
                                        (LocalDate/of 2019 06 30) "2019" nil "30"))))

(defn- matches-interval
       [expected-from expected-to result]
       (and (.isEqual expected-from (:from result))
            (.isEqual expected-to (:to result))))

(deftest get-interval-test
         (testing "Single day"
                  (are [expected-from expected-to year month day] (matches-interval expected-from expected-to (commons/get-interval year month day))
                       (LocalDate/of 2022 01 31) (LocalDate/of 2022 02 01) "2022" "01" "31"
                       (LocalDate/of 2022 12 31) (LocalDate/of 2023 01 01) "2022" "12" "31")
                  (testing "Whole month"
                           (are [expected-from expected-to year month day] (matches-interval expected-from expected-to (commons/get-interval year month day))
                                (LocalDate/of 2022 03 01) (LocalDate/of 2022 04 01) "2022" "03" nil
                                (LocalDate/of 2022 12 01) (LocalDate/of 2023 01 01) "2022" "12" ""))
                  (testing "Whole year"
                           (is (matches-interval (LocalDate/of 2022 01 01) (LocalDate/of 2023 01 01) (commons/get-interval "2022" nil nil)))
                           (with-redefs-fn {#'commons/now-zoned #(-> #inst"2023-06-01T00:00:00.000-00:00" .toInstant (.atZone (ZoneId/of "UTC")))}
                                           #(is (matches-interval (LocalDate/of 2023 01 01) (LocalDate/of 2024 01 01) (commons/get-interval nil nil "")))))))
