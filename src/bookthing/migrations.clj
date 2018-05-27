(ns bookthing.migrations
  (:require [clojure.java.jdbc :as jdbc]
            [migratus.core :as migratus]
            [bookthing.db :as db]))


(def config {:store :database
             :migration-dir "migrations/"
             :init-script "init.sql"
             :migration-table-name "migratus_migrations"
             :db db/db-spec})

#_(migratus/init config)


#_(migratus/create config "create-authors")

#_(migratus/create config "create-gin-index-on-book-titles")

#_(migratus/migrate config)

#_(migratus/rollback config)

#_(migratus/pending-list config)
