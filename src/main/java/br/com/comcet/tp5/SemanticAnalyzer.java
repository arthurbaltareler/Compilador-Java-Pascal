package br.com.comcet.tp5;

import br.com.comcet.tp1.Symbol;
import br.com.comcet.tp1.ast.*;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer {

    private final ScopedSymbolTable table = new ScopedSymbolTable();
    private final List<String> errors = new ArrayList<>();

    // ── Ponto de entrada ────────────────────────────────────────────────────

    public void analyze(AstNode node) {
        if (node instanceof Program) {
            analyzeProgram((Program) node);
        }
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    // ── Análise do Programa ──────────────────────────────────────────────────

    private void analyzeProgram(Program program) {
        table.enterScope(); // abre escopo global
        for (Command cmd : program.commands()) {
            analyzeCommand(cmd);
        }
        table.exitScope(); // fecha escopo global
    }

    // ── Análise de Comandos ──────────────────────────────────────────────────

    private void analyzeCommand(Command cmd) {
        if (cmd instanceof AssignmentCommand) {
            analyzeAssignment((AssignmentCommand) cmd);
        } else if (cmd instanceof IfCommand) {
            analyzeIf((IfCommand) cmd);
        } else if (cmd instanceof WhileCommand) {
            analyzeWhile((WhileCommand) cmd);
        } else if (cmd instanceof RepeatCommand) {
            analyzeRepeat((RepeatCommand) cmd);
        } else if (cmd instanceof BlockCommand) {
            analyzeBlock((BlockCommand) cmd);
        } else if (cmd instanceof WritelnCommand) {
            analyzeWriteln((WritelnCommand) cmd);
        } else if (cmd instanceof ReadlnCommand) {
            analyzeReadln((ReadlnCommand) cmd);
        } else if (cmd instanceof VarDeclCommand) {
            analyzeVarDecl((VarDeclCommand) cmd);
        }
    }

    private void analyzeAssignment(AssignmentCommand cmd) {
        // Verifica se a variável foi declarada
        String name = cmd.id().name();
        if (table.lookup(name) == null) {
            errors.add("Variavel nao declarada: " + name);
        }
        analyzeExpression(cmd.expr());
    }

    private void analyzeIf(IfCommand cmd) {
        analyzeExpression(cmd.condition());
        analyzeCommand(cmd.thenBranch());
        if (cmd.elseBranch() != null) {
            analyzeCommand(cmd.elseBranch());
        }
    }

    private void analyzeWhile(WhileCommand cmd) {
        analyzeExpression(cmd.condition());
        analyzeCommand(cmd.body());
    }

    private void analyzeRepeat(RepeatCommand cmd) {
        analyzeCommand(cmd.body());
        analyzeExpression(cmd.condition());
    }

    private void analyzeBlock(BlockCommand cmd) {
        table.enterScope();
        for (Command c : cmd.commands()) {
            analyzeCommand(c);
        }
        table.exitScope();
    }

    private void analyzeWriteln(WritelnCommand cmd) {
        analyzeExpression(cmd.expr());
    }

    private void analyzeReadln(ReadlnCommand cmd) {
        String name = cmd.id().name();
        if (table.lookup(name) == null) {
            errors.add("Variavel nao declarada: " + name);
        }
    }

    private void analyzeVarDecl(VarDeclCommand cmd) {
        for (String name : cmd.names()) {
            if (table.existsInCurrentScope(name)) {
                errors.add("Variavel ja declarada no escopo atual: " + name);
            } else {
                table.add(name, new Symbol(name, cmd.typeName(), null));
            }
        }
    }

    // ── Análise de Expressões ────────────────────────────────────────────────

    private void analyzeExpression(Expression expr) {
        if (expr instanceof Identifier) {
            String name = ((Identifier) expr).name();
            if (table.lookup(name) == null) {
                errors.add("Variavel nao declarada: " + name);
            }
        } else if (expr instanceof BinaryExpression) {
            BinaryExpression bin = (BinaryExpression) expr;
            analyzeExpression(bin.left());
            analyzeExpression(bin.right());
        } else if (expr instanceof UnaryExpression) {
            analyzeExpression(((UnaryExpression) expr).operand());
        }
        // Literal não precisa verificar — é um valor constante
    }
}