(ns dev
  (:require
   [clj-antlr.core :as antlr]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.string :as st]
   [clojure.walk :as w]
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



;;
;; DataScript DB

(defn filename->db-data
  [filename]
  (-> filename
      slurp
      (st/replace #"#datascript/DB " "")
      clojure.edn/read-string))

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


;;
;; parser

(defn ->parser []
  (-> "grammars/RoamResearch.g4" slurp antlr/parser))

(defn newline? [s] (or (= s "\r") (= s "\n")))


;;
;; data tree

(defn data-seq->type-data
  [ds]
  ;; (first ds) => :data
  (if (string? (second ds))
    [:text (second ds)]
    (let [t (-> ds second first)
          cs (-> ds second rest)]
      (condp = t
        :link        [:link (nth cs 1)]
        :ref         [:ref (nth cs 1)]
        :roamRender  [:roam-render (nth cs 1)]
        :latex       [:latex (nth cs 1)]
        :alias       [:alias (nth cs 1) (nth cs 3)]
        :highlight   [:highlight (nth cs 1)]
        :bold        [:bold (nth cs 1)]
        :italic      [:italic (nth cs 1)]
        :codeinline  [:codeinline (nth cs 1)]
        :string      [:string (st/join #"" cs)]
        nil))))

(defn parsed->data-tree
  [parsed]
  (w/postwalk
   #(if-not (seq? %)
      (if-not (newline? %)
        % [:string %])
      (condp = (first %)
        :file     (rest %)
        :block    (rest %)
        :contents (rest %)
        :content  (data-seq->type-data %)
        %))  ;; default
   parsed))


;;
;; render

(def type-data->render
  {
   :html
   {
    :link        #(str "<a class='link' href='" % "'>" % "</a>")
    :ref         #(str "<span class='ref'>" % "</span>")
    :roam-render #(str "<span class='roam-render'>" % "</span>")
    :latex       #(str "<span class='latex'>" % "</span>")
    :alias       #(str "<a class='alias' href='" %2 "'>" %1 "</a>")
    :highlight   #(str "<span class='highlight'>" % "</span>")
    :bold        #(str "<b>" % "</b>")
    :italic      #(str "<i>" % "</i>")
    :codeinline  #(str "<span class='codeinline'>" % "</span>")
    :string      #(if (newline? %) "<br/>"
                      (str "<span class='string'>" % "</span>"))
    :text        #(str "<span class='text'>" % "</span>")}})

(defn valid-renderer-type? [rrt]
  (-> type-data->render keys set
      (contains? rrt)))

(defn data-tree->string
  [renderer-type data-tree]
  {:pre [(s/valid? valid-renderer-type? renderer-type)]}
  
  (let [rr (get type-data->render renderer-type)]
    (letfn [(type-data->string [ld]
              (try
                (let [t (first ld)
                      rf (get rr t)]
                  (->> ld rest
                       (map #(data-tree->string renderer-type %))
                       (apply rf)))
                
                (catch Throwable t
                  (println (clojure.main/err->msg t))
                  (prn ld)
                  (prn t)
                  (throw t))))]
      (try
        (if-not
            (seq? data-tree) data-tree
            (->> data-tree
                 (map #(if (seq? %) (data-tree->string renderer-type %)
                           (type-data->string %)))  ;; vector => [:string "hi"]
                 (apply str)))
        
        (catch Throwable t
          (println (clojure.main/err->msg t))
          (prn data-tree)
          (throw t))))))


;;
;; dev helpers

(defn string->render
  [st]
  (let [parse (->parser)]
    (->> st
         parse
         parsed->data-tree
         (data-tree->string :html))))

(comment

  (def data (filename->db-data "db.edn"))
  (def conn (data->db-conn data))
  (def datoms (db->all-block-string-datoms conn))
  (def parse (->parser))
  
  (->> datoms
       (map :block/string)
       (map parse)
       (map parsed->data-tree)
       (map #(data-tree->string :html %))
       doall
       time)
  )
