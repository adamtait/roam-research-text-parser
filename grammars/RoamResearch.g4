/* Roam Research: Text Grammar
    By Adam Tait (a@adamtait.com)
    code: https://github.com/adamtait/roam-research-text-parser
 */

grammar RoamResearch;


file     : block+ contents | contents ;
block    : contents CR? NL ;
contents : feature*? ;
feature  :
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
         | token
         ;

link        : LSQ LSQ contents RSQ RSQ ;
alias       : LSQ? LSQ contents RSQ LP contents RP RP? ;
ref         : LP LP contents RP RP ;
roamRender  : LB LB contents RB RB ;
latex       : DOLLAR DOLLAR contents DOLLAR DOLLAR ;
highlight   : CARET CARET contents CARET CARET ;
bold        : STAR STAR contents STAR STAR ;
italic      : USCORE USCORE contents USCORE USCORE ;
codeinline  : BTICK contents BTICK ;


string      : CHAR + ;
CHAR        : ~( '\n' | '\r' | '(' | ')' | '[' | ']' | '{' | '}' | '$' | '^' | '*' | '_' | '`' ) ;
token       : LP | RP | LSQ | RSQ | LB | RB | DOLLAR | CARET | STAR | USCORE | BTICK ;

LP          : '(' ;
RP          : ')' ;
LSQ         : '[' ;
RSQ         : ']' ;
LB          : '{' ;
RB          : '}' ;
DOLLAR      : '$' ;
CARET       : '^' ;
STAR        : '*' ;
USCORE      : '_' ;
BTICK       : '`' ;

CR          : '\r' -> skip;
NL          : '\n' ;


//( '_'~'_' | ']'~'(' | ']'~']' | '('~'(' | '{'~'{' | '}'~'}' | '$'~'$' | '^'~'^' | '*'~'*' | ~'[' | ~')' | ~'`' | ~'`' | ~'`' ) +?;
// ( ~( '`' | '[' | ']' | '(' | ')' | '{' | '}' | '$' | '^' | '*' | '_' ) | '{'~'{' | '}'~'}' | '$'~'$' | '^'~'^' | '*'~'*' | '_'~'_' ) +;
