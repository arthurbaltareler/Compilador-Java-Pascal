package br.com.comcet.tp4;

import br.com.comcet.tp1.ast.*;
import br.com.comcet.tp4.parser.MiniPascalLexer;
import br.com.comcet.tp4.parser.MiniPascalParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AntlrVisitorTest {

    private AstNode parse(String codigo) {
        CharStream input = CharStreams.fromString(codigo);
        MiniPascalLexer lexer = new MiniPascalLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniPascalParser parser = new MiniPascalParser(tokens);
        ParseTree tree = parser.program();
        MyVisitor visitor = new MyVisitor();
        return visitor.visit(tree);
    }

    // Retorna o primeiro comando que não é VarDeclCommand
    private Command firstRealCommand(Program p) {
        return p.commands().stream()
                .filter(c -> !(c instanceof VarDeclCommand))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Nenhum comando encontrado"));
    }

    @Test
    void visitorConstroiAstParaAtribuicaoComPrecedencia() {
        String codigo = "program p; var x: integer; begin x := 10 + 5 * 2; end.";
        AstNode ast = parse(codigo);

        assertNotNull(ast);
        assertTrue(ast instanceof Program);

        Program program = (Program) ast;
        assertEquals("p", program.name());

        AssignmentCommand assign = (AssignmentCommand) firstRealCommand(program);
        assertEquals("x", assign.id().name());

        BinaryExpression plus = (BinaryExpression) assign.expr();
        assertEquals("+", plus.operator());
        assertTrue(plus.left() instanceof Literal);

        BinaryExpression mult = (BinaryExpression) plus.right();
        assertEquals("*", mult.operator());
    }

    @Test
    void visitorConstroiIfSemElse() {
        String codigo = "program p; var x: integer; begin if x then x := 1; end.";
        AstNode ast = parse(codigo);

        Program program = (Program) ast;
        IfCommand ic = (IfCommand) firstRealCommand(program);
        assertNull(ic.elseBranch());
        assertTrue(ic.thenBranch() instanceof AssignmentCommand);
    }

    @Test
    void visitorConstroiIfComElse() {
        String codigo = "program p; var x: integer; begin if x then x := 1; else x := 2; end.";
        AstNode ast = parse(codigo);

        Program program = (Program) ast;
        IfCommand ic = (IfCommand) firstRealCommand(program);
        assertNotNull(ic.elseBranch());
    }

    @Test
    void visitorConstroiWhile() {
        String codigo = "program p; var x: integer; begin while x do x := 1; end.";
        AstNode ast = parse(codigo);

        Program program = (Program) ast;
        assertTrue(firstRealCommand(program) instanceof WhileCommand);
    }

    @Test
    void visitorConstroiWriteln() {
        String codigo = "program p; var x: integer; begin writeln(x); end.";
        AstNode ast = parse(codigo);

        Program program = (Program) ast;
        assertTrue(firstRealCommand(program) instanceof WritelnCommand);
    }

    @Test
    void parentesesAlteramPrecedencia() {
        String codigo = "program p; var x: integer; begin x := (10 + 5) * 2; end.";
        AstNode ast = parse(codigo);

        Program program = (Program) ast;
        AssignmentCommand assign = (AssignmentCommand) firstRealCommand(program);

        BinaryExpression mult = (BinaryExpression) assign.expr();
        assertEquals("*", mult.operator());

        BinaryExpression plus = (BinaryExpression) mult.left();
        assertEquals("+", plus.operator());
    }
}