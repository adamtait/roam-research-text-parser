(ns dev
  (:require
   [clj-antlr.core :as antlr]
   [clojure.java.io :as io]
   [com.stuartsierra.component :as component]
   [com.stuartsierra.component.repl
    :refer [reset set-init start stop system]]
   [datascript.core :as d]))


;; Do not try to load source code from 'resources' directory
(clojure.tools.namespace.repl/set-refresh-dirs "dev" "src" "test")

;; system

(defn new-system
  [_]
  (component/system-map))

(set-init new-system)


(defn get-schema
  [db-conn]
  (d/q
   '[:find ?attr ?type ?card
     :where
     [_ :db.install/attribute ?a]
     [?a :db/valueType ?t]
     [?a :db/cardinality ?c]
     [?a :db/ident ?attr]
     [?t :db/ident ?type]
     [?c :db/ident ?card]]
   db-conn))

(defn datom->tx-data [datom] (concat [:db/add] datom))

(comment
  
  (def data (-> "db.edn" slurp clojure.edn/read-string))
  (def conn (d/create-conn (:schema data)))
  (->> data :datoms (mapv datom->tx-data) (d/transact! conn))
  (def ss (d/q '[:find (pull ?e [*]) :where [?e :block/string _]] @conn))
  (def rr-parser (-> "grammars/RoamResearch.g4" slurp antlr/parser))
  )
