(ns {{namespace}}.ports.http.routes.core
    (:require [{{namespace}}.ports.http.routes.error-handler :refer [service-error-handler]]
     [{{namespace}}.ports.http.routes.events :as r.events]
     [{{namespace}}.ports.http.routes.event-polls :as r.event-polls]
     [{{namespace}}.ports.http.routes.interceptors :as i]
     [io.pedestal.http :as http]
     [io.pedestal.http.body-params :as body-params]))

(def json-public-interceptors [service-error-handler
                               (body-params/body-params)
                               (i/json-out)
                               http/html-body])

(def json-interceptors [service-error-handler
                        (body-params/body-params)
                        i/authz-user
                        (i/json-out)
                        http/html-body])

(def json-root-interceptors [service-error-handler
                             (body-params/body-params)
                             i/authz-admin
                             (i/json-out)
                             http/html-body])

(def specs #{["/events" :get (conj json-interceptors `r.events/get-events)]
             ["/events/:id" :get (conj json-interceptors `r.events/get-event)]
             ["/events" :post (conj json-root-interceptors `r.events/post-event)]
             ["/events" :patch (conj json-root-interceptors `r.events/patch-event)]
             ["/events/:id" :delete (conj json-root-interceptors `r.events/delete-event)]
             ["/event-polls" :get (conj json-interceptors `r.event-polls/get-event-polls)]
             ["/event-polls/:id" :get (conj json-interceptors `r.event-polls/get-event-poll)]
             ["/event-polls" :post (conj json-root-interceptors `r.event-polls/post-event-poll)]})
