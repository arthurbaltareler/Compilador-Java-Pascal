package br.com.comcet.tp1.ast;

import java.util.ArrayList;
import java.util.List;

public final class Program extends AstNode {
    private final String name;
    private final List<Command> commands;
    private List<AstNode> subDecls = new ArrayList<>(); // FunctionDecl / ProcedureDecl

    public Program(String name, List<Command> commands) {
        this.name = name;
        this.commands = commands;
    }

    public String name() { return name; }
    public List<Command> commands() { return commands; }

    public List<AstNode> subDecls() { return subDecls; }
    public void setSubDecls(List<AstNode> subDecls) { this.subDecls = subDecls; }

    @Override
    protected void printTree(StringBuilder sb, int level) {
        sb.append(indent(level)).append("Program(\"").append(name).append("\")\n");
        for (AstNode sub : subDecls) {
            sub.printTree(sb, level + 1);
        }
        for (Command cmd : commands) {
            cmd.printTree(sb, level + 1);
        }
    }
}