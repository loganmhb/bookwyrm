(ns bookthing.db)

(def db-spec {:classname "org.postgresql.Driver"
              :subprotocol "postgresql"
              :subname "//localhost:5432/bookthing"
              :password "password"
              :user "bookthing"})
