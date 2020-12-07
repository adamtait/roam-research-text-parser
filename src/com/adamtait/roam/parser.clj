(ns com.adamtait.roam.parser
  (:require
   [clj-antlr.core :as antlr]
   [clojure.spec.alpha :as s]))

(defn ->parser
  ([] (->parser "grammars/RoamResearch.g4"))
  ([grammar-file-path]
   {:pre [(s/valid? string? grammar-file-path)]}
   (-> grammar-file-path slurp antlr/parser)))
