(ns com.adamtait.roam.render
  (:require
   [clojure.main :refer [err->msg]]
   [clojure.spec.alpha :as s]
   [com.adamtait.roam.spec :refer [newline?]]))


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
                (println (err->msg t))
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
        (println (err->msg t))
        (prn data-tree)
        (throw t)))))
