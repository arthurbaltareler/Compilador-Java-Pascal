package br.com.comcet.tp6;

import br.com.comcet.tp1.Symbol;
import br.com.comcet.tp1.Type;
import br.com.comcet.tp1.ast.*;
import br.com.comcet.tp5.ScopedSymbolTable;

import java.util.ArrayList;
import java.util.List;

public class TypeChecker {

    private final ScopedSymbolTable table = new ScopedSymbolTable();
    private final List<String> errors = new ArrayList<>();

    // ── Ponto de entrada ────────────────────────────────────────────────────

    public void check(AstNode node) {
        if (node instanceof Program) {
            checkProgram((Program) node);
        }
    }

    public boolean hasErrors() { return !errors.isEmpty(); }
    public List<String> getErrors() { return errors; }

    // ── Programa e Comandos ──────────────────────────────────────────────────

    private void checkProgram(Program program) {
        table.enterScope();
        for (Command cmd : program.commands()) {
            checkCommand(cmd);
        }
        table.exitScope();
    }

    private void checkCommand(Command cmd) {
        if (cmd instanceof VarDeclCommand) {
            checkVarDecl((VarDeclCommand) cmd);
        } else if (cmd instanceof AssignmentCommand) {
            checkAssignment((AssignmentCommand) cmd);
        } else if (cmd instanceof IfCommand) {
            checkIf((IfCommand) cmd);
        } else if (cmd instanceof WhileCommand) {
            checkWhile((WhileCommand) cmd);
        } else if (cmd instanceof RepeatCommand) {
            checkRepeat((RepeatCommand) cmd);
        } else if (cmd instanceof BlockCommand) {
            checkBlock((BlockCommand) cmd);
        } else if (cmd instanceof WritelnCommand) {
            checkExpression(((WritelnCommand) cmd).expr());
        }
        // ReadlnCommand não exige checagem de tipo de expressão
    }

    private void checkVarDecl(VarDeclCommand cmd) {
        for (String name : cmd.names()) {
            if (!table.existsInCurrentScope(name)) {
                table.add(name, new Symbol(name, cmd.typeName(), null));
            }
            // Duplicidade já é validada na Etapa 5
        }
    }

    private void checkAssignment(AssignmentCommand cmd) {
        String varName = cmd.id().name();
        Symbol symbol = table.lookup(varName);
        Type exprType = checkExpression(cmd.expr());

        if (symbol == null) {
            errors.add("Erro Semântico: variável '" + varName + "' não declarada.");
            return;
        }

        Type varType = Type.fromText(symbol.type());

        if (varType == exprType) {
            return; // tipos idênticos — ok
        }

        // Promoção permitida: integer -> real (preparado para quando houver literais reais)
        if (varType == Type.REAL && exprType == Type.INTEGER) {
            return;
        }

        errors.add("Erro Semântico: não é possível atribuir " + exprType +
                " à variável '" + varName + "' do tipo " + varType + ".");
    }

    private void checkIf(IfCommand cmd) {
        Type condType = checkExpression(cmd.condition());
        if (condType != Type.BOOLEAN) {
            errors.add("Erro Semântico: condição do 'if' deve ser booleana.");
        }
        checkCommand(cmd.thenBranch());
        if (cmd.elseBranch() != null) {
            checkCommand(cmd.elseBranch());
        }
    }

    private void checkWhile(WhileCommand cmd) {
        Type condType = checkExpression(cmd.condition());
        if (condType != Type.BOOLEAN) {
            errors.add("Erro Semântico: condição do 'while' deve ser booleana.");
        }
        checkCommand(cmd.body());
    }

    private void checkRepeat(RepeatCommand cmd) {
        checkCommand(cmd.body());
        Type condType = checkExpression(cmd.condition());
        if (condType != Type.BOOLEAN) {
            errors.add("Erro Semântico: condição do 'until' deve ser booleana.");
        }
    }

    private void checkBlock(BlockCommand cmd) {
        table.enterScope();
        for (Command c : cmd.commands()) {
            checkCommand(c);
        }
        table.exitScope();
    }

    // ── Expressões (bottom-up: folhas primeiro, depois os pais) ─────────────

    private Type checkExpression(Expression expr) {
        Type result;

        if (expr instanceof Literal) {
            result = inferLiteralType((Literal) expr);
        } else if (expr instanceof Identifier) {
            result = checkIdentifier((Identifier) expr);
        } else if (expr instanceof BinaryExpression) {
            result = checkBinaryExpression((BinaryExpression) expr);
        } else if (expr instanceof UnaryExpression) {
            result = checkUnaryExpression((UnaryExpression) expr);
        } else if (expr instanceof CastExpr) {
            result = ((CastExpr) expr).toType();
        } else {
            result = Type.ERROR;
        }

        expr.setResolvedType(result);
        return result;
    }

    private Type inferLiteralType(Literal lit) {
        String v = lit.value();
        if (v.equals("true") || v.equals("false")) {
            return Type.BOOLEAN;
        }
        if (v.matches("[0-9]+")) {
            return Type.INTEGER;
        }
        return Type.STRING; // sobra: strings entre aspas simples já sem as aspas
    }

    private Type checkIdentifier(Identifier id) {
        Symbol symbol = table.lookup(id.name());
        if (symbol == null) {
            errors.add("Erro Semântico: variável '" + id.name() + "' não declarada.");
            return Type.ERROR;
        }
        return Type.fromText(symbol.type());
    }

    private Type checkBinaryExpression(BinaryExpression bin) {
        Type left  = checkExpression(bin.left());
        Type right = checkExpression(bin.right());
        String op  = bin.operator();

        if (isArithmetic(op)) {
            if (left == Type.INTEGER && right == Type.INTEGER) {
                return Type.INTEGER;
            }
            boolean numeric = (left == Type.INTEGER || left == Type.REAL)
                    && (right == Type.INTEGER || right == Type.REAL);
            if (numeric && (left == Type.REAL || right == Type.REAL)) {
                return Type.REAL; // promoção
            }
            errors.add("Erro Semântico: Operação '" + op +
                    "' não suportada para tipos " + left + " e " + right + ".");
            return Type.ERROR;
        }

        if (isRelational(op)) {
            boolean numericPair = (left == Type.INTEGER || left == Type.REAL)
                    && (right == Type.INTEGER || right == Type.REAL);
            boolean boolPair = left == Type.BOOLEAN && right == Type.BOOLEAN
                    && (op.equals("=") || op.equals("<>"));
            if (numericPair || boolPair) {
                return Type.BOOLEAN;
            }
            errors.add("Erro Semântico: Operação '" + op +
                    "' não suportada para tipos " + left + " e " + right + ".");
            return Type.ERROR;
        }

        if (isLogical(op)) {
            if (left == Type.BOOLEAN && right == Type.BOOLEAN) {
                return Type.BOOLEAN;
            }
            errors.add("Erro Semântico: operador lógico '" + op +
                    "' exige operandos booleanos.");
            return Type.ERROR;
        }

        return Type.ERROR;
    }

    private Type checkUnaryExpression(UnaryExpression un) {
        Type operandType = checkExpression(un.operand());

        if (un.operator().equals("-")) {
            if (operandType == Type.INTEGER || operandType == Type.REAL) {
                return operandType;
            }
            errors.add("Erro Semântico: operador unário '-' exige operando numérico.");
            return Type.ERROR;
        }

        if (un.operator().equalsIgnoreCase("not")) {
            if (operandType == Type.BOOLEAN) {
                return Type.BOOLEAN;
            }
            errors.add("Erro Semântico: operador 'not' exige operando booleano.");
            return Type.ERROR;
        }

        return Type.ERROR;
    }

    private boolean isArithmetic(String op) {
        return op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/");
    }

    private boolean isRelational(String op) {
        return op.equals("=") || op.equals("<>") || op.equals("<")
                || op.equals(">") || op.equals("<=") || op.equals(">=");
    }

    private boolean isLogical(String op) {
        return op.equalsIgnoreCase("and") || op.equalsIgnoreCase("or");
    }
}