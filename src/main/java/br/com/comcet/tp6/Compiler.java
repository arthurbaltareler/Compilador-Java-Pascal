package br.com.comcet.tp6;

import br.com.comcet.tp1.ast.AstNode;
import br.com.comcet.tp4.MyVisitor;
import br.com.comcet.tp4.parser.MiniPascalLexer;
import br.com.comcet.tp4.parser.MiniPascalParser;
import br.com.comcet.tp5.SemanticAnalyzer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

/**
 * Ponto de entrada único do front-end do compilador.
 * Executa, em sequência: Léxico -> Sintático (ANTLR) -> Semântico (Escopo) -> Tipos.
 */
public class Compiler {

    private final List<String> errors = new ArrayList<>();

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    /**
     * Executa apenas o front-end (sem geração de código).
     * Lança RuntimeException se houver erro léxico ou sintático.
     * Erros semânticos/de tipo são acumulados em getErrors().
     */
    public AstNode compileFrontendOnly(String source) {
        errors.clear();

        // 1. Léxico + 2. Sintático (via ANTLR)
        CharStream input = CharStreams.fromString(source);
        MiniPascalLexer lexer = new MiniPascalLexer(input);

        lexer.removeErrorListeners();
        lexer.addErrorListener(new ThrowingErrorListener());

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniPascalParser parser = new MiniPascalParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(new ThrowingErrorListener());

        ParseTree tree = parser.program(); // lança exceção se houver erro sintático

        // 3. Construção da AST
        MyVisitor visitor = new MyVisitor();
        AstNode ast = visitor.visit(tree);

        // 4. Semântico (Escopo)
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        semanticAnalyzer.analyze(ast);
        errors.addAll(semanticAnalyzer.getErrors());

        // 5. Checagem de Tipos
        TypeChecker typeChecker = new TypeChecker();
        typeChecker.check(ast);
        errors.addAll(typeChecker.getErrors());

        if (hasErrors()) {
            throw new RuntimeException("Erros semanticos encontrados: " + errors);
        }

        return ast;
    }

    /**
     * Listener que transforma qualquer erro léxico/sintático em exceção,
     * em vez de apenas imprimir no console (comportamento padrão do ANTLR).
     */
    private static class ThrowingErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg,
                                RecognitionException e) {
            throw new RuntimeException("Erro sintatico/lexico na linha " + line +
                    ":" + charPositionInLine + " - " + msg);
        }
    }
}