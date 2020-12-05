/* Roam Research
    Text Grammar

    By Adam Tait (a@adamtait.com)
    url: https://github.com/adamtait/roam-research-text-parser
 */

grammar RoamResearchText;


datas : data * ;

data  : alias
      | link
      | ref
      | roamRender
      | latex
      | highlight
      | bold
      | italic
      | codeinline
      | ALIASA | ALIASB | ALIASC
      | LINKA | LINKB
      | REFA | REFB
      | RRA | RRB
      | LATEXA
      | HLA
      | BOLDA
      | ITALICA
      | CODEINLINEA
      | TEXT
      ;

alias       : ALIASA datas ALIASB datas ALIASC;
ALIASA      : '[';
ALIASB      : '](';
ALIASC      : ')';

link        : LINKA datas LINKB;
LINKA       : '[[';
LINKB       : ']]';

ref         : REFA datas REFB;
REFA        : '((';
REFB        : '))';

roamRender  : RRA datas RRB;
RRA         : '{{';
RRB         : '}}';

latex       : LATEXA datas LATEXA;
LATEXA      : '$$';

highlight   : HLA datas HLA;
HLA         : '^^';

bold        : BOLDA datas BOLDA;
BOLDA       : '**';

italic      : '__' datas '__';
ITALICA     : '__';

codeinline  : CODEINLINEA TEXT CODEINLINEA;
CODEINLINEA : '`' | '`' | '`';

TEXT        : ( '_'~'_' | ']'~'(' | ']'~']' | '('~'(' | '{'~'{' | '}'~'}' | '$'~'$' | '^'~'^' | '*'~'*' | ~'[' | ~')' | ~'`' | ~'`' | ~'`' ) +?;

// ( . ) +?;

// ( ~( '`' | '[' | ']' | '(' | ')' | '{' | '}' | '$' | '^' | '*' | '_' ) | '{'~'{' | '}'~'}' | '$'~'$' | '^'~'^' | '*'~'*' | '_'~'_' ) +;