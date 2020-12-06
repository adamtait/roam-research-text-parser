# Roam Research Text Parsing

By: Adam Tait

[Notes page in Roam Research](https://roamresearch.com/#/app/at/page/doz-ureGh)


## Usage

1. Download [Roam Research - Help](https://roamresearch.com/#/app/help/page/NYgRwJaQM). Choose `...` in top right, `Export All`, `EDN`.
2. Unzip downloaded zip file and name it `db.edn`
3. Place `db.edn` in the root directory of this git repository. (_Same directory as this README.md_)
4. `clj -A:repl` (_assuming you're using nREPL for your Clojure development environment_)
5. `user=> (go)`
6. run the code in the comments section at the bottom of `dev/dev.clj`


## Transforming sections of the parse tree

I recommend using `clojure.walk/postwalk-replace`. You can choose any
pattern in the parse tree to replace.


## Performance


```
dev> (->> datoms
          (map :block/string)
          (map parse)
          (map parsed->data-tree)
          (map #(data-tree->string :html %))
          doall
          time)
"Elapsed time: 117855.919702 msecs"
```
     
