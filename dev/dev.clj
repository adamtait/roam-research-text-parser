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

(def string->wrap #(-> % list list))

(defn string-token->join
  "join individual chars of string/token"
  [t feature-rest]
  (cond-> feature-rest
    (#{:string :token} t)
    (->> (st/join "") string->wrap)))

(defn feature->args
  "ensure optional tokens get included. only need on :alias (unless
  grammars/RoamResearch changes)"
  [t feature-rest]
  (if-not
      (= :alias t) feature-rest
      (cond-> feature-rest

        (= "[" (second feature-rest))    ;; optional '[' at beginning
        (->> (drop 2) (concat [(list [:string (string->wrap "[")])]))
        
        (->> feature-rest (take-last 2) (every? #(= ")" %)))   ;; optional ')' at end
        (->> (drop-last 2) (#(concat % [(list [:string (string->wrap ")")])]))))))

(def feature-type->data-k
  {:alias       :alias
   :bold        :bold
   :codeinline  :codeinline
   :highlight   :highlight
   :italic      :italic
   :latex       :latex
   :link        :link
   :ref         :ref
   :roamRender  :roam-render
   :string      :string
   :token       :string})

(defn valid-feature-type? [t]
  (let [s (->> feature-type->data-k keys set)]
    (contains? s t)))

(defn feature->data
  [t feature-rest]
  {:pre [(s/valid? valid-feature-type? t)]}
  [(get feature-type->data-k t)
   feature-rest])

(defn parsed->walk
  [feature->data-fn parsed]
  (if-not (seq? parsed)
    (if-not (newline? parsed) parsed
            (list [:string (string->wrap parsed)]))

    (condp = (first parsed) ;; seq
      :file     (->> parsed rest (apply concat))
      :block    (->> parsed rest (apply concat))
      :contents (rest parsed)
      :feature  (-> parsed second feature->data-fn)
      parsed)))   ;; default

(defn parsed->data-tree
  [parsed]
  (letfn [(seq->feature-data [rd]
            (if (string? rd) [:string (string->wrap rd)]

                (let [t (first rd)]
                  (->> rd rest
                       (string-token->join t)
                       (feature->args t)
                       (filter seq?)       ;; remove extra tokens from feature
                       parsed->data-tree   ;; recur
                       (feature->data t)))))]

    (if-not (seq? parsed) parsed

            (w/postwalk
             #(parsed->walk seq->feature-data %)
             parsed))))


;;
;; render

(defn string->render
  [string-coll]
  (let [s (ffirst string-coll)]
    (if (newline? s) "<br/>"
        (str "<span class='string'>" s "</span>"))))

(defn arg->optional-char?
  "required to determine args given to alias->render. arg is already
  rendered."
  [arg]
  (or
   (= arg (string->render '(("["))))
   (= arg (string->render '((")"))))))

(defn alias->render
  ([als target]
   (str "<a class='alias' href='" target "'>" als "</a>"))

  ([a b c]
   ;; either a or c is an optional character
   (let [args (if (arg->optional-char? a) [a b c ""]
                  ["" a b c])]
     (apply alias->render args)))
  
  ([pre als target post]
   ;; pre/post always = ([:string (("["))])
   (str
    pre
    (alias->render als target)
    post)))

(def type-data->render
  {
   :html
   {
    :link        #(str "<a class='link' href='" % "'>" % "</a>")
    :ref         #(str "<span class='ref'>" % "</span>")
    :roam-render #(str "<span class='roam-render'>" % "</span>")
    :latex       #(str "<span class='latex'>" % "</span>")
    :alias       alias->render
    :highlight   #(str "<span class='highlight'>" % "</span>")
    :bold        #(str "<b>" % "</b>")
    :italic      #(str "<i>" % "</i>")
    :codeinline  #(str "<span class='codeinline'>" % "</span>")
    :string      string->render}})

(defn valid-renderer-type? [rrt]
  (-> type-data->render keys set
      (contains? rrt)))

(defn data-tree->string
  [renderer-type data-tree]
  {:pre [(s/valid? valid-renderer-type? renderer-type)]}
  
  (letfn [(type-data->string [ld]
            (try
              (let [t (first ld)
                    rf (get-in type-data->render [renderer-type t])
                    args (second ld)]
                (if (= :string t) (rf args)
                    (->> args
                         (map #(data-tree->string renderer-type %))
                         (apply rf))))
              
              (catch Throwable t
                (println (clojure.main/err->msg t))
                (prn ld)
                (prn t)
                (throw t))))]
    (try
      (if-not (seq? data-tree) data-tree
              (->> data-tree
                   (map #(if (seq? %) (data-tree->string renderer-type %)
                             (type-data->string %)))  ;; vector => [:string "hi"]
                   (apply str)))
      
      (catch Throwable t
        (println (clojure.main/err->msg t))
        (prn data-tree)
        (throw t)))))


;;
;; dev helpers

(defn string->parse->render
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

  (def all-blocks
    (->> datoms
         (map :block/string)
         (st/join "\n")))
  (def results
    (->> all-blocks
         parse
         parsed->data-tree
         (data-tree->string :html)))
  (time results)

  (->> some-blocks
       parse
       parsed->data-tree
       (data-tree->string :html))
  )
