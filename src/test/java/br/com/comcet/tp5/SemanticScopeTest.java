package br.com.comcet.tp5;

import br.com.comcet.tp1.ast.AstNode;
import br.com.comcet.tp4.MyVisitor;
import br.com.comcet.tp4.parser.MiniPascalLexer;
import br.com.comcet.tp4.parser.MiniPascalParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SemanticScopeTest {

    private AstNode parse(String codigo) {
        CharStream input = CharStreams.fromString(codigo);
        MiniPascalLexer lexer = new MiniPascalLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniPascalParser parser = new MiniPascalParser(tokens);
        ParseTree tree = parser.program();
        return new MyVisitor().visit(tree);
    }

    @Test
    void falhaVariavelNaoDeclarada() {
        String codigo = "program p; begin x := 1; end.";
        AstNode ast = parse(codigo);
        SemanticAnalyzer sem = new SemanticAnalyzer();
        sem.analyze(ast);

        assertTrue(sem.hasErrors());
        assertTrue(sem.getErrors().stream()
                .anyMatch(e -> e.contains("nao declarada")));
    }

    @Test
    void passaVariavelDeclaradaCorretamente() {
        String codigo = "program p; var x: integer; begin x := 1; end.";
        AstNode ast = parse(codigo);
        SemanticAnalyzer sem = new SemanticAnalyzer();
        sem.analyze(ast);

        assertFalse(sem.hasErrors());
    }

    @Test
    void falhaDuplaDeclaracaoMesmoEscopo() {
        // Na gramática, dupla declaração é: var x, x: integer;
        String codigo = "program p; var x, x: integer; begin x := 1; end.";
        AstNode ast = parse(codigo);
        SemanticAnalyzer sem = new SemanticAnalyzer();
        sem.analyze(ast);

        assertTrue(sem.hasErrors());
        assertTrue(sem.getErrors().stream()
                .anyMatch(e -> e.contains("ja declarada")));
    }

    @Test
    void passaCodigoCorreto() {
        String codigo = "program p; var x, y: integer; begin x := 10; y := x + 1; end.";
        AstNode ast = parse(codigo);
        SemanticAnalyzer sem = new SemanticAnalyzer();
        sem.analyze(ast);

        assertFalse(sem.hasErrors());
    }

    @Test
    void falhaVariavelUsadaEmExpressaoNaoDeclarada() {
        String codigo = "program p; var x: integer; begin x := y + 1; end.";
        AstNode ast = parse(codigo);
        SemanticAnalyzer sem = new SemanticAnalyzer();
        sem.analyze(ast);

        assertTrue(sem.hasErrors());
        assertTrue(sem.getErrors().stream()
                .anyMatch(e -> e.contains("nao declarada")));
    }
}