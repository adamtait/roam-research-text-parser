/* Roam Research: Text Grammar
    By Adam Tait (a@adamtait.com)
    code: https://github.com/adamtait/roam-research-text-parser
 */

grammar RoamResearchText;


file     : block + contents | contents ;

block    : content *?  CR? '\n' ;

contents : content *? ;

content  :
         alias
         | link
         | ref
         | roamRender
         | latex
         | highlight
         | bold
         | italic
         | codeinline
         | ALIASA | ALIASB | ALIASC
         | string
         ;

alias       : ALIASA contents ALIASB contents ALIASC ;
ALIASA      : '[' ;
ALIASB      : '](' ;
ALIASC      : ')' ;

link        : LINKA contents LINKB ;
LINKA       : '[[' ;
LINKB       : ']]' ;

ref         : REFA contents REFB ;
REFA        : '((' ;
REFB        : '))' ;

roamRender  : RRA contents RRB ;
RRA         : '{{' ;
RRB         : '}}' ;

latex       : LATEXA contents LATEXA ;
LATEXA      : '$$' ;

highlight   : HLA contents HLA ;
HLA         : '^^' ;

bold        : BOLDA contents BOLDA ;
BOLDA       : '**' ;

italic      : ITALICA contents ITALICA ;
ITALICA     : '__' ;

codeinline  : CODEINLINEA contents CODEINLINEA ;
CODEINLINEA : '`' ; 

string      : TEXT + ;
TEXT        : ~[\n\r] +? ;

CR          : '\r' -> skip;


//( '_'~'_' | ']'~'(' | ']'~']' | '('~'(' | '{'~'{' | '}'~'}' | '$'~'$' | '^'~'^' | '*'~'*' | ~'[' | ~')' | ~'`' | ~'`' | ~'`' ) +?;
// ( ~( '`' | '[' | ']' | '(' | ')' | '{' | '}' | '$' | '^' | '*' | '_' ) | '{'~'{' | '}'~'}' | '$'~'$' | '^'~'^' | '*'~'*' | '_'~'_' ) +;