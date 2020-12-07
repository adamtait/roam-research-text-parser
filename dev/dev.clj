(ns dev
  (:require
   [clojure.string :as st]

   [com.adamtait.roam.database :as rdb]
   [com.adamtait.roam.parsed :as rp]
   [com.adamtait.roam.parser :refer [->parser]]
   [com.adamtait.roam.render :as rr]

   [com.stuartsierra.component :as component]
   [com.stuartsierra.component.repl
    :refer [reset set-init start stop system]]))

;; Do not try to load source code from 'resources' directory
(clojure.tools.namespace.repl/set-refresh-dirs "dev" "src" "test")

;; system

(defn new-system
  [_]
  (component/system-map))

(set-init new-system)



;;
;; dev helpers

(defn string->parse->render
  [st]
  (let [parse (->parser)]
    (->> st
         parse
         rp/parsed->data-tree
         (rr/data-tree->string :html))))

(comment

  (def data (rdb/filename->db-data "db.edn"))
  (def conn (rdb/data->db-conn data))
  (def datoms (rdb/db->all-block-string-datoms conn))
  (def parse (->parser))
  
  (->> datoms
       (map :block/string)
       (map parse)
       (map rp/parsed->data-tree)
       (map #(rr/data-tree->string :html %))
       doall
       time)

  (def all-blocks
    (->> datoms
         (map :block/string)
         (st/join "\n")))
  (def results
    (->> all-blocks
         parse
         rp/parsed->data-tree
         (rr/data-tree->string :html)))
  (time results)

  (->> some-blocks
       parse
       rp/parsed->data-tree
       (rr/data-tree->string :html))
  )
