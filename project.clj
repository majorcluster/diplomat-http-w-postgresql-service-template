(defproject org.clojars.majorcluster/lein-template.diplomat-http-w-postgresql-service "0.0.1"
  :description "Diplomat architecture-pedestal styled template with postgresql db for leiningen generation"
  :url "https://github.com/majorcluster/diplomat-http-w-postgresql-service-template"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :deploy-repositories [["clojars" {:url "https://repo.clojars.org"
                                    :username :env/clojars_username
                                    :password :env/clojars_password}]]
  :eval-in-leiningen true)
