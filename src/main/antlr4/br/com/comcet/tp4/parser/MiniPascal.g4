grammar MiniPascal;

// ── Regras do Parser ────────────────────────────────────────────────────────

program
    : 'program' ID ';' varDecl? subDecl* block '.' EOF
    ;

varDecl
    : 'var' (idList ':' type ';')+
    ;

subDecl
    : functionDecl
    | procedureDecl
    ;

functionDecl
    : 'function' ID '(' paramList? ')' ':' type ';' varDecl? block ';'
    ;

procedureDecl
    : 'procedure' ID '(' paramList? ')' ';' varDecl? block ';'
    ;

paramList
    : param (';' param)*
    ;

param
    : idList ':' type
    ;

idList
    : ID (',' ID)*
    ;

type
    : 'integer'
    | 'boolean'
    | 'string'
    ;

block
    : 'begin' commandList 'end'
    ;

commandList
    : (command)*
    ;

command
    : assignment
    | ifCommand
    | whileCommand
    | repeatCommand
    | writeCommand
    | readCommand
    | procedureCall
    | block
    ;

assignment
    : ID ':=' expression ';'
    ;

ifCommand
    : 'if' expression 'then' command ('else' command)?
    ;

whileCommand
    : 'while' expression 'do' command
    ;

repeatCommand
    : 'repeat' commandList 'until' expression ';'
    ;

writeCommand
    : 'writeln' '(' expression ')' ';'
    ;

readCommand
    : 'readln' '(' ID ')' ';'
    ;

procedureCall
    : ID '(' argList? ')' ';'
    ;

argList
    : expression (',' expression)*
    ;

// ── Expressões com precedência ───────────────────────────────────────────────

expression
    : 'not' expression                                                    # notExpr
    | '-' expression                                                      # unaryExpr
    | expression op=('*' | '/') expression                               # mulExpr
    | expression op=('+' | '-') expression                               # addExpr
    | expression op=('=' | '<>' | '<' | '>' | '<=' | '>=') expression   # relationalExpr
    | expression op=('and' | 'or') expression                            # logicalExpr
    | '(' expression ')'                                                  # parenExpr
    | ID '(' argList? ')'                                                 # functionCallExpr
    | NUMBER                                                              # numberExpr
    | STRING_LITERAL                                                      # stringExpr
    | 'true'                                                              # trueExpr
    | 'false'                                                             # falseExpr
    | ID                                                                  # idExpr
    ;

// ── Regras do Lexer ──────────────────────────────────────────────────────────

ID          : [a-zA-Z][a-zA-Z0-9_]* ;
NUMBER      : [0-9]+ ;
STRING_LITERAL : '\'' (~'\'')* '\'' ;

COMMENT1    : '{' .*? '}' -> skip ;
COMMENT2    : '(*' .*? '*)' -> skip ;
WS          : [ \t\r\n]+ -> skip ;