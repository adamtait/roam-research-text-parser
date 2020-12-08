# Roam Research Text Parsing

By: Adam Tait

[Notes page in Roam Research](https://roamresearch.com/#/app/at/page/doz-ureGh)


## Usage

1. Download [Roam Research - Help](https://roamresearch.com/#/app/help/page/NYgRwJaQM). Choose `...` in top right, `Export All`, `EDN`.
2. Unzip downloaded zip file and name it `db.edn`
3. Place `db.edn` in the root directory of this git repository. (_Same directory as this README.md_)
4. `clj -A:nrepl` (_assuming you're using nREPL for your Clojure development environment_)
5. `user=> (go)`
6. run the code in the comments section at the bottom of `dev/dev.clj`


## Transforming sections of the parse tree

Retrieve the (_cleaned up_) parse tree with:

```
dev> (->> "[[string]] to ((render))"
          ((->parser))
          rp/parsed->data-tree)
```

I recommend using `clojure.walk/postwalk-replace`. You can choose any
pattern in the parse tree to replace.


## Performance

Using `datoms` from [Roam Research - Help](https://roamresearch.com/#/app/help/page/NYgRwJaQM) database.

Run on an Apple MacBook Pro 2015 (16Gb RAM).

```
dev> (def abs
      (->> datoms
           (map :block/string)
           (st/join "\n")))
#'dev/abs
dev> (time (->> abs
                parse
                rp/parsed->data-tree
                (rr/data-tree->string :html)))
"Elapsed time: 48518.681028 msecs"
```


## Implementation

### hand coded parser vs grammar

In my experience writing parsers, you start off writing a simple
parser by hand. Before long, your parser becomes messy and difficult
to update or extremely slow. 

I think it's possible to hand code a parser and have that parser be
highly performant. Achieving high performance goals usually means
having good abstractions and focusing on performance.

Achieving a good abstraction for the input to parse is harder. A
grammar is a better abstraction for parsing arbitrary text.

I actually do not have a lot of experience writing grammars though the
Roam Research text certainly seems to have become complex enough that
a better abstraction is needed. Also, I felt that a better abstraction
was implied in the request from the Roam team.


### ANTLR

I do have some experience with [ANTLR](https://www.antlr.org/)
(specifically, `clj-antlr`). I did a quick internet search and found
that among other lexer/parser tools based on grammars, it's still
considered the most performant and one of the easiest to use.

ANTLR's grammar definition and implementation are deep (Terrence Parr,
the author, claims he put 25 years of research into it). If you're
curious about ANTLR4, I would recommend Terrence Parr's [The
Definitive ANTLR 4
Reference](https://pragprog.com/titles/tpantlr2/the-definitive-antlr-4-reference/)
which I would recommend.


### clj-antlr

Kyle Kingsbury added a Clojure wrapper for ANTLR4,
[clj-antlr](https://github.com/aphyr/clj-antlr). He compares the
performance of `clj-antlr` to
[instaparse](https://github.com/engelberg/instaparse) (another popular
Clojure parsing library) and finds `clj-antlr` and ANTLR4 to be
significantly faster. I have not performed the same test but I have
much trust in Kyle to be honest and thorough in performance testing.

A disadvantage of `clj-antlr` is that (as of 2020/12/7) it is not
compatible with ClojureScript. I believe that the current parser &
renderer that Roam Research's export database tool uses is written in
ClojureScript and runs in the browser. You would not be able to run
`clj-antlr` in the same way, as it uses ANTLR4's Java implementation
which requires a JVM.

ANTLR4 does have a [JavaScript
implementation](https://github.com/antlr/antlr4/blob/master/doc/javascript-target.md)
so it may be possible to make a `cljs-antlr` but I haven't
investigated what buliding that capability would require.


### Known issues

[See issues.](https://github.com/adamtait/roam-research-text-parser/issues)
