(ns leiningen.new.diplomat-http-w-postgresql-service
    (:require [leiningen.new.templates :as tmpl]
            [leiningen.core.main :as main]))

(def render (tmpl/renderer "diplomat_http_w_postgresql_service"))

(defn diplomat-http-w-postgresql-service
  [name]
  (let [main-ns (tmpl/sanitize-ns name)
        data {:raw-name name
              :name (tmpl/project-name name)
              :namespace main-ns
              :sanitized (tmpl/name-to-path name)}]
    (main/info "Generating fresh 'lein new' diplomat-http-w-postgresql-service project.")
    (tmpl/->files data
                  ["resources/migrations/1.sql" (render "1.sql" data)]
                  ["resources/migrations/2.sql" (render "2.sql" data)]
                  ["resources/migrations/teardown.sql" (render "teardown.sql" data)]
                  ["resources/dev-config.edn" (render "dev-config.edn" data)]
                  ["resources/test-config.edn" (render "test-config.edn" data)]
                  ["src/{{sanitized}}/adapters/commons.clj" (render "adapters_commons.clj" data)]
                  ["src/{{sanitized}}/adapters/event_polls.clj" (render "adapters_event_polls.clj" data)]
                  ["src/{{sanitized}}/adapters/events.clj" (render "adapters_events.clj" data)]
                  ["src/{{sanitized}}/controllers/event_polls.clj" (render "controllers_event_polls.clj" data)]
                  ["src/{{sanitized}}/controllers/events.clj" (render "controllers_events.clj" data)]
                  ["src/{{sanitized}}/ports/http/routes/commons.clj" (render "http_routes_commons.clj" data)]
                  ["src/{{sanitized}}/ports/http/routes/core.clj" (render "http_routes_core.clj" data)]
                  ["src/{{sanitized}}/ports/http/routes/error_handler.clj" (render "http_routes_error_handler.clj" data)]
                  ["src/{{sanitized}}/ports/http/routes/event_polls.clj" (render "http_routes_event_polls.clj" data)]
                  ["src/{{sanitized}}/ports/http/routes/events.clj" (render "http_routes_events.clj" data)]
                  ["src/{{sanitized}}/ports/http/routes/interceptors.clj" (render "http_routes_interceptors.clj" data)]
                  ["src/{{sanitized}}/ports/http/core.clj" (render "http_core.clj" data)]
                  ["src/{{sanitized}}/ports/sql/repositories/event_polls.clj" (render "sql_repo_event_polls.clj" data)]
                  ["src/{{sanitized}}/ports/sql/repositories/events.clj" (render "sql_repo_events.clj" data)]
                  ["src/{{sanitized}}/ports/sql/repositories/entities.clj" (render "sql_repo_entities.clj" data)]
                  ["src/{{sanitized}}/ports/sql/core.clj" (render "sql_core.clj" data)]
                  ["src/{{sanitized}}/ports/core.clj" (render "ports_core.clj" data)]
                  ["src/{{sanitized}}/commons.clj" (render "commons.clj" data)]
                  ["src/{{sanitized}}/server.clj" (render "server.clj" data)]
                  ["src/{{sanitized}}/configs.clj" (render "configs.clj" data)]
                  ["test/{{sanitized}}/adapters/commons_test.clj" (render "adapters_commons_test.clj" data)]
                  ["test/{{sanitized}}/ports/http/routes/event_polls_test.clj" (render "http_routes_event_polls_test.clj" data)]
                  ["test/{{sanitized}}/ports/http/routes/events_test.clj" (render "http_routes_events_test.clj" data)]
                  ["test/core_test.clj" (render "core_test.clj" data)]
                  ["README.md" (render "README.md" data)]
                  ["project.clj" (render "project.clj" data)]
                  ["Dockerfile" (render "Dockerfile" data)]
                  [".gitignore" (render "gitignore" data)]
                  ["config/logback.xml" (render "logback.xml" data)])))
