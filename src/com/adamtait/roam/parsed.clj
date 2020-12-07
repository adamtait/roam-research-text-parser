(ns com.adamtait.roam.parsed
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as st]
   [clojure.walk :as w]
   [com.adamtait.roam.spec :refer [newline?]]))


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
