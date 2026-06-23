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

    public boolean hasErrors() { return !errors.isEmpty(); }
    public List<String> getErrors() { return errors; }

    // ── Análise do Programa ──────────────────────────────────────────────────

    private void analyzeProgram(Program program) {
        table.enterScope(); // escopo global

        // Primeiro: registra todas as funções/procedimentos no escopo global
        // (permite chamadas recursivas e chamadas "para frente")
        for (AstNode sub : program.subDecls()) {
            registerSubDecl(sub);
        }

        // Depois: analisa o corpo de cada função/procedimento
        for (AstNode sub : program.subDecls()) {
            analyzeSubDecl(sub);
        }

        // Por fim: analisa o bloco principal
        for (Command cmd : program.commands()) {
            analyzeCommand(cmd);
        }

        table.exitScope();
    }

    // ── Funções e Procedimentos ──────────────────────────────────────────────

    private void registerSubDecl(AstNode sub) {
        String name = (sub instanceof FunctionDecl)
                ? ((FunctionDecl) sub).name()
                : ((ProcedureDecl) sub).name();

        if (table.existsInCurrentScope(name)) {
            errors.add("Funcao ou procedimento ja declarado: " + name);
            return;
        }
        table.add(name, new Symbol(name, "function", null));
    }

    private void analyzeSubDecl(AstNode sub) {
        List<Param> params;
        List<VarDeclCommand> localVars;
        BlockCommand body;

        if (sub instanceof FunctionDecl) {
            FunctionDecl fd = (FunctionDecl) sub;
            params = fd.params();
            localVars = fd.localVars();
            body = fd.body();
        } else {
            ProcedureDecl pd = (ProcedureDecl) sub;
            params = pd.params();
            localVars = pd.localVars();
            body = pd.body();
        }

        table.enterScope(); // escopo local da sub-rotina

        // Parâmetros agem como variáveis locais
        for (Param p : params) {
            if (table.existsInCurrentScope(p.name())) {
                errors.add("Parametro ja declarado: " + p.name());
            } else {
                table.add(p.name(), new Symbol(p.name(), p.typeName(), null));
            }
        }

        // Variáveis locais (podem sombrear globais — permitido)
        for (VarDeclCommand vd : localVars) {
            analyzeVarDecl(vd);
        }

        // Corpo da sub-rotina
        for (Command cmd : body.commands()) {
            analyzeCommand(cmd);
        }

        table.exitScope();
    }

    // ── Comandos ──────────────────────────────────────────────────────────────

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
        } else if (cmd instanceof ProcedureCallCommand) {
            analyzeProcedureCall((ProcedureCallCommand) cmd);
        }
    }

    private void analyzeProcedureCall(ProcedureCallCommand cmd) {
        if (table.lookup(cmd.name()) == null) {
            errors.add("Procedimento nao declarado: " + cmd.name());
        }
        for (Expression arg : cmd.args()) {
            analyzeExpression(arg);
        }
    }

    private void analyzeAssignment(AssignmentCommand cmd) {
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

    // ── Expressões ────────────────────────────────────────────────────────────

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
        } else if (expr instanceof FunctionCallExpression) {
            FunctionCallExpression call = (FunctionCallExpression) expr;
            if (table.lookup(call.name()) == null) {
                errors.add("Funcao nao declarada: " + call.name());
            }
            for (Expression arg : call.args()) {
                analyzeExpression(arg);
            }
        }
        // Literal não precisa verificar
    }
}