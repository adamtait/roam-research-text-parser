(ns dev
  (:require
   [clj-antlr.core :as antlr]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
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


(defn datom->tx-data [datom] (concat [:db/add] datom))

(defn data-seq->type-data
  [ds]
  ;; (first ds) => :data
  (if (string? (second ds))
    [:text (second ds)]
    (let [t (-> ds second first)
          cs (-> ds second (nth 2))]
      (condp = t
        :link        [:link cs]
        :ref         [:ref cs]
        :roamRender  [:roam-render cs]
        :latex       [:latex cs]
        :alias       [:alias cs (-> ds second (nth 4))]
        :highlight   [:highlight cs]
        :bold        [:bold cs]
        :italic      [:italic cs]
        :codeinline  [:codeinline cs]
        nil))))

(defn parsed->data-tree
  [parsed]
  (w/postwalk
   #(if (seq? %)
      (condp = (first %)
        :datas (rest %)
        :data  (data-seq->type-data %)
        %)
      %)
   parsed))


(def type-data->render
  {
   :html
   {
    :link        #(str "<a href='" % "'>" % "</a>")
    :ref         #(str "<div>" % "</div>")
    :roam-render #(str "<div>" % "</div>")
    :latex       #(str "<div>" % "</div>")
    :alias       #(str "<a href='" %2 "'>" %1 "</a>")
    :highlight   #(str "<div style='color:yellow'>" % "</div>")
    :bold        #(str "<b>" % "</b>")
    :italic      #(str "<i>" % "</i>")
    :codeinline  #(str "<div style='font-family:monospace;background-color:#313131;color:#fff'>" % "</div>")}})

(def valid-renderer-type?
  #(contains?
    (-> type-data->render keys set)
    %))

(defn data-tree->string
  [renderer-type data-tree]
  {:pre [(s/valid? valid-renderer-type? renderer-type)]}
  
  (let [rr (get type-data->render renderer-type)]
    (letfn [(type-data->string [renderer ld]
              (let [t (first ld)]
                (if (= :text t) (second ld)
                    (let [rf (get renderer t)]
                      (->> ld rest
                           (map #(data-tree->string renderer-type %))
                           (apply rf))))))]
      (->> data-tree
           (map #(type-data->string rr %))
           (apply str)))))


(comment
  
  (def data (-> "db.edn"
                slurp
                (clojure.string/replace #"#datascript/DB " "")
                clojure.edn/read-string))
  (def conn (d/create-conn (:schema data)))
  (->> data :datoms (mapv datom->tx-data) (d/transact! conn) ((constantly :ok)))
  (def ss (d/q '[:find (pull ?e [*]) :where [?e :block/string _]] @conn))
  (def parse (-> "grammars/RoamResearch.g4" slurp antlr/parser))
  (->> "How [[Roam]] works"
       parse
       parsed->data-tree
       (data-tree->string :html))
  )
