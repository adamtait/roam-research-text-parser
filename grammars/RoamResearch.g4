/* Roam Research
    Text Grammar

    By Adam Tait (a@adamtait.com)
    url: https://github.com/adamtait/roam-research-text-parser
 */

grammar RoamResearchText;


datas: data * ;

data: String
    | link
    | ref
    | roamRender
    | latex
    | alias
    | highlight
    | bold
    | italic
    ;

link: '[[' datas ']]';
ref: '((' datas '))';
roamRender: '{{' datas '}}';
latex: '$$' datas '$$';
alias: '[' datas '](' datas ')';
highlight: '^^' datas '^^';
bold: '**' datas '**';
italic: '__' datas '__';

String: [0-9a-zA-Z ] +;