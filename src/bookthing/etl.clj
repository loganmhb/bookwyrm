(ns bookthing.etl
  "Load Open Library data into Postgres"
  (:require [bookthing.db :as db]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]))


(defn reader->authors [rdr]
  (->> rdr
       line-seq
       (map #(string/split % #"\t"))
       (map last)
       (map json/parse-string)
       (map (fn [doc]
              {:name (doc "name")
               :birth_date (doc "birth_date")
               :death_date (doc "death_date")
               :ol_key (doc "key")}))))

(defn load-author-dump []
  (with-open [rdr (io/reader (io/resource "ol_dump_authors.txt"))]
    (let [idx (atom 1)]
      (doseq [batch (partition-all 1000 (reader->authors rdr))]
        (println (format "Inserting batch %d" @idx))
        (jdbc/insert-multi! db/db-spec :authors batch)
        (swap! idx inc)))))

(load-author-dump)
