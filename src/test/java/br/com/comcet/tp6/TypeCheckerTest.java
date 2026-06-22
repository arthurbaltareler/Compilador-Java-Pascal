package br.com.comcet.tp6;

import br.com.comcet.tp1.ast.AstNode;
import br.com.comcet.tp4.MyVisitor;
import br.com.comcet.tp4.parser.MiniPascalLexer;
import br.com.comcet.tp4.parser.MiniPascalParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TypeCheckerTest {

    private AstNode parse(String codigo) {
        CharStream input = CharStreams.fromString(codigo);
        MiniPascalLexer lexer = new MiniPascalLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniPascalParser parser = new MiniPascalParser(tokens);
        ParseTree tree = parser.program();
        return new MyVisitor().visit(tree);
    }

    @Test
    void falhaAoSomarIntComBoolean() {
        String codigo = "program p; var x: integer; begin x := 10 + true; end.";
        AstNode ast = parse(codigo);

        TypeChecker tc = new TypeChecker();
        tc.check(ast);

        assertTrue(tc.hasErrors());
        assertTrue(tc.getErrors().stream().anyMatch(e -> e.contains("Operação '+'")));
    }

    @Test
    void falhaAtribuicaoIntParaString() {
        // String literal sendo atribuída a uma variável integer
        String codigo = "program p; var x: integer; begin x := 'ola'; end.";
        AstNode ast = parse(codigo);

        TypeChecker tc = new TypeChecker();
        tc.check(ast);

        assertTrue(tc.hasErrors());
    }

    @Test
    void falhaCondicaoIfNaoBooleana() {
        String codigo = "program p; var x: integer; begin if 10 then x := 1; end.";
        AstNode ast = parse(codigo);

        TypeChecker tc = new TypeChecker();
        tc.check(ast);

        assertTrue(tc.hasErrors());
        assertTrue(tc.getErrors().stream()
                .anyMatch(e -> e.contains("'if'") && e.contains("booleana")));
    }

    @Test
    void passaProgramaComTiposCorretos() {
        String codigo = "program p; var x, y: integer; begin x := 10; y := x + 5; "
                + "if y > x then x := y; end.";
        AstNode ast = parse(codigo);

        TypeChecker tc = new TypeChecker();
        tc.check(ast);

        assertFalse(tc.hasErrors());
    }

    @Test
    void passaComparacaoEntreInteiros() {
        String codigo = "program p; var x, y: integer; var flag: boolean; "
                + "begin x := 10; y := 20; flag := x < y; end.";
        AstNode ast = parse(codigo);

        TypeChecker tc = new TypeChecker();
        tc.check(ast);

        assertFalse(tc.hasErrors());
    }

    @Test
    void falhaOperadorLogicoComInteiros() {
        String codigo = "program p; var x: integer; var flag: boolean; "
                + "begin flag := x and 1; end.";
        AstNode ast = parse(codigo);

        TypeChecker tc = new TypeChecker();
        tc.check(ast);

        assertTrue(tc.hasErrors());
    }
}