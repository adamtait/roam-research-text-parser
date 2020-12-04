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
      | LINKA | LINKB
      | REFA | REFB
      | RRA | RRB
      | LATEXA
      | ALIASA | ALIASB | ALIASC
      | HLA
      | BOLDA
      | ITALICA
      | CODEINLINEA
      | TEXT
      ;

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

alias       : ALIASA datas ALIASB datas ALIASC;
ALIASA      : '[';
ALIASB      : '](';
ALIASC      : ')';

highlight   : HLA datas HLA;
HLA         : '^^';

bold        : BOLDA datas BOLDA;
BOLDA       : '**';

italic      : ITALICA datas ITALICA;
ITALICA     : '__';

codeinline  : CODEINLINEA TEXT CODEINLINEA;
CODEINLINEA : '`';

TEXT        : ( ~( '`' | '[' | ']' | '(' | ')' | '{' | '}' | '$' | '^' | '*' | '_' ) | '{'~'{' | '}'~'}' | '$'~'$' | '^'~'^' | '*'~'*' | '_'~'_' ) +;