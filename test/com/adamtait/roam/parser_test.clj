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

(deftest alias-inside-alias
  (is
   (string->parse
    "[![img](image-as-alias.com)](www.roamreasearch.com)")

   '(:file
     (:contents
      (:feature
       (:alias
        "["
        (:contents
         (:feature (:string "!"))
         (:feature
          (:alias
           "["
           (:contents (:feature (:string "i" "m" "g")))
           "]"
           "("
           (:contents
            (:feature
             (:string "i" "m" "a" "g" "e" "-" "a" "s" "-" "a" "l" "i" "a" "s" "." "c" "o" "m")))
           ")")))
        "]"
        "("
        (:contents
         (:feature
          (:string "w" "w" "w" "." "r" "o" "a" "m" "r" "e" "a" "s" "e" "a" "r" "c" "h" "." "c" "o" "m")))
        ")"))))))
