(ns bookthing.etl
  "Load Open Library data into Postgres"
  (:require [bookthing.db :as db]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]))

(defn reader->json [rdr]
  (->> rdr
       line-seq
       (map #(string/split % #"\t"))
       (map last)
       (map json/parse-string)))

(defn reader->authors [rdr]
  (map (fn [doc]
         {:name (doc "name")
          :birth_date (doc "birth_date")
          :death_date (doc "death_date")
          :ol_key (doc "key")})
       (reader->json rdr)))

(defn author->sql-command [author]
  (into ["INSERT INTO authors (name, birth_date, death_date, ol_key) VALUES (?, ?, ?, ?) ON CONFLICT UPDATE"]
        ((juxt :name :birth_date :death_date :ol_key) author)))

(defn load-author-dump []
  (with-open [rdr (io/reader (io/resource "ol_dump_authors.txt"))]
    (let [idx (atom 1)]
      (doseq [batch (partition-all 1000 (reader->authors rdr))]
        (println (format "Inserting batch %d" @idx))
        (jdbc/db-do-commands db/db-spec (map author->sql-command batch))
        (swap! idx inc)))))

(defn reader->work [rdr]
  (map (fn [doc]
         (println doc))))

(with-open [rdr (io/reader (io/resource "data/ol_dump_works.txt"))]
  (reduce (fn [ks doc] (into ks (keys doc)))
          #{}
          (reader->json rdr)))

(defn -main []
  (let [author-fut (future (load-author-dump))
        work-fut (future (load-work-dump))]
    @author-fut
    @work-fut))
