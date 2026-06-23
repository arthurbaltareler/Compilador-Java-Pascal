package br.com.comcet.tp1.ast;

import java.util.List;

public final class ProcedureCallCommand extends Command {
    private final String name;
    private final List<Expression> args;

    public ProcedureCallCommand(String name, List<Expression> args) {
        this.name = name;
        this.args = args;
    }

    public String name()           { return name; }
    public List<Expression> args() { return args; }

    @Override
    protected void printTree(StringBuilder sb, int level) {
        sb.append(indent(level)).append("ProcedureCallCommand ").append(name).append("\n");
        for (Expression arg : args) arg.printTree(sb, level + 1);
    }
}