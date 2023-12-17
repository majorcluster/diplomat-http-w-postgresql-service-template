# org.clojars.majorcluster/diplomat-http-w-postgresql-service

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.majorcluster/lein-template.diplomat-http-w-postgresql-service.svg)](https://clojars.org/org.clojars.majorcluster/lein-template.diplomat-http-w-postgresql-service) 

A Leiningen template for generating a diplomat-architecture styled pedestal service
The ports available at this template are:   
- http inbound, having foo route specified as a sample
- postgresql db outbound
  - change connections at ports/postgresql/core or use a customized proper way to load configs
  - modify initial scripts at resource/migrations, create db and initial tables and run it on database created

## Usage
Using the template from clojars:   
lein new org.clojars.majorcluster/diplomat-http-w-postgresql-service <your project name>

## Development
Testing locally:   
lein new diplomat-http-w-postgresql-service <your project name>

Deploying:   
export GPG_TTY=$(tty) && lein deploy clojars
