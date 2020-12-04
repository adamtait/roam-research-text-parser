/* Roam Research
    Text Grammar

    By Adam Tait (a@adamtait.com)
    url: https://github.com/adamtait/roam-research-text-parser
 */

grammar RoamResearchText;


datas : data * ;

data  : link
      | ref
      | roamRender
      | latex
      | alias
      | highlight
      | bold
      | italic
      | codeinline
      | TEXT
      ;

link        : '[[' datas ']]';
ref         : '((' datas '))';
roamRender  : '{{' datas '}}';
latex       : '$$' datas '$$';
alias       : '[' datas '](' datas ')';
highlight   : '^^' datas '^^';
bold        : '**' datas '**';
italic      : '__' datas '__';
codeinline  : '`' TEXT '`';

TEXT        : ( ~( '`' | '[' | ']' | '(' | ')' | '{' | '}' | '$' | '^' | '*' | '_' ) | '{'~'{' | '}'~'}' | '$'~'$' | '^'~'^' | '*'~'*' | '_'~'_' ) +;