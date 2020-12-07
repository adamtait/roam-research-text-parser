(ns com.adamtait.roam.database
  (:require
   [clojure.edn :as edn]
   [clojure.spec.alpha :as s]
   [clojure.string :as st]
   [datascript.core :as d]))


(defn filename->db-data
  [filename]
  (-> filename
      slurp
      (st/replace #"#datascript/DB " "")
      edn/read-string))

(defn datom->tx-data [datom] (concat [:db/add] datom))

(defn data->db-conn
  [data]
  {:pre [(s/valid? map? (:schema data))]}
  (let [conn (d/create-conn (:schema data))]
    (->> data :datoms
         (mapv datom->tx-data)
         (d/transact! conn))
    conn))

(defn db->all-block-string-datoms
  [conn]
  (flatten
   (d/q
    '[:find (pull ?e [*])
      :where [?e :block/string _]]
    @conn)))
