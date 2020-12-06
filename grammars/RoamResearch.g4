/* Roam Research: Text Grammar
    By Adam Tait (a@adamtait.com)
    code: https://github.com/adamtait/roam-research-text-parser
 */

grammar RoamResearchText;


datas : data *? ;

data  :
      alias
      | link
      | ref
      | roamRender
      | latex
      | highlight
      | bold
      | italic
      | codeinline
      | string
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

italic      : ITALICA datas ITALICA;
ITALICA     : '__';

codeinline  : CODEINLINEA datas CODEINLINEA;
CODEINLINEA : '`';

string      : TEXT+;
TEXT        : .+?;


//( '_'~'_' | ']'~'(' | ']'~']' | '('~'(' | '{'~'{' | '}'~'}' | '$'~'$' | '^'~'^' | '*'~'*' | ~'[' | ~')' | ~'`' | ~'`' | ~'`' ) +?;
// ( ~( '`' | '[' | ']' | '(' | ')' | '{' | '}' | '$' | '^' | '*' | '_' ) | '{'~'{' | '}'~'}' | '$'~'$' | '^'~'^' | '*'~'*' | '_'~'_' ) +;