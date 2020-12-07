(ns com.adamtait.roam.parser-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [com.adamtait.roam.parser :refer [->parser]]))


(defn string->parse
  [s]
  (let [f (->parser)]
    (f s)))

(deftest nested-rules
  (is
   (string->parse
    "[[[[Andy Matuschak]]]]")

   '(:file
     (:contents
      (:feature
       (:link
        "["
        "["
        (:contents
         (:feature
          (:link
           "["
           "["
           (:contents
            (:feature
             (:string "A" "n" "d" "y" " " "M" "a" "t" "u" "s" "c" "h" "a" "k")))
           "]"
           "]")))
        "]"
        "]"))))))


(deftest overlapping-rules
  (is
   (string->parse
    "[[alias](target)]")

   '(:file
     (:contents
      (:feature
       (:alias
        "["
        "["
        (:contents (:feature (:string "a" "l" "i" "a" "s")))
        "]"
        "("
        (:contents (:feature (:string "t" "a" "r" "g" "e" "t")))
        ")"))
      (:feature (:token "]"))))))
