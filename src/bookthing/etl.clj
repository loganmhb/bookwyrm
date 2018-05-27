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

(def upsert-sql "INSERT INTO authors (name, birth_date, death_date, ol_key)
                 VALUES (?, ?, ?, ?)
                 ON CONFLICT (ol_key) DO
                  UPDATE
                    SET (name, birth_date, death_date) = (EXCLUDED.name, EXCLUDED.birth_date, EXCLUDED.death_date)
                    WHERE authors.ol_key = EXCLUDED.ol_key")

(defn upsert-sql [table conflict-key update-cols]
  (let [insert-cols (conj update-cols conflict-key)
        cols-expr (format "%s (%s)" table (string/join ", " insert-cols))
        values-expr (format "(%s)" (string/join ", " (repeat (count insert-cols) "?")))
        update-lhs (format "(%s)" (string/join ", " update-cols))
        update-rhs (format "(%s)" (string/join ", " (map #(format "EXCLUDED.%s" %) update-cols)))
        update-clause (format "SET %s = %s" update-lhs update-rhs)
        where-clause (format "%s.%s = EXCLUDED.%s" table conflict-key conflict-key)]
    (format "INSERT INTO %s VALUES %s ON CONFLICT (%s) DO UPDATE %s WHERE %s"
            cols-expr
            values-expr
            conflict-key
            update-clause
            where-clause)))

(upsert-sql "authors" "ol_key" ["name" "birth_date" "death_date"])
(upsert-sql "books" "ol_key" ["title" "subtitle" "first_published"])

(defn load-author-dump []
  (with-open [rdr (io/reader (io/resource "data/ol_dump_authors.txt"))]
    (let [idx (atom 1)]
      (doseq [batch (partition-all 1000 (reader->authors rdr))]
        (println (format "Inserting batch %d" @idx))
        (jdbc/execute! db/db-spec
                       (into [(upsert-sql "authors" "ol_key" ["name" "birth_date" "death_date"])]
                             (mapv (juxt :name :birth_date :death_date :ol_key)
                                   batch))
                       {:multi? true})
        (swap! idx inc)))))

(defn reader->books [rdr]
  (map (fn [doc]
         {:first_published (doc "first_publish_date")
          :title (doc "title")
          :subtitle (doc "subtitle")
          :authors (doc "authors")
          :ol_key (doc "key")})
       (reader->json rdr)))

(defn load-book-dump []
  (with-open [rdr (io/reader (io/resource "data/ol_dump_works.txt"))]
    (dorun
     (map-indexed
      (fn [i batch]
        (println "Inserting batch" i)
        (jdbc/execute! db/db-spec
                       (into [(upsert-sql "books" "ol_key" ["title" "subtitle" "first_published"])]
                             (mapv (juxt :title :subtitle :first_published :ol_key)
                                   batch))
                       {:multi? true}))
      (partition-all 1000 (reader->books rdr))))))

#_(with-open [rdr (io/reader (io/resource "data/ol_dump_works.txt"))]
    (reduce (fn [ks doc] (into ks (keys doc)))
            #{}
            (take 100000 (reader->json rdr))))
#{"first_sentence" "latest_revision" "created" "subject_times" "number_of_editions" "excerpts" "last_modified" "authors" "key" "revision" "subject_people" "covers" "subject_places" "subtitle" "links" "title" "type" "subjects" "lc_classifications" "cover_edition" "first_publish_date" "dewey_number" "works" "description"}

#_(with-open [rdr (io/reader (io/resource "data/ol_dump_works.txt"))]

    (->> (reader->json rdr)
         (filter (fn [doc]
                   (seq (remove #(get % "author") (get doc "authors")))))

         first
         ))

#_(defn -main []
    (let [author-fut (future (load-author-dump))
          work-fut (future (load-work-dump))]
      @author-fut
      @work-fut))

#_(def author-future (future (load-author-dump)))

#_(def book-future (future (load-book-dump)))
