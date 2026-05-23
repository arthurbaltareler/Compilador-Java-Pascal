package br.com.comcet.tp1.ast;

import java.util.List;

public final class VarDeclCommand extends Command {
    private final List<String> names;
    private final String typeName;

    public VarDeclCommand(List<String> names, String typeName) {
        this.names    = names;
        this.typeName = typeName;
    }

    public List<String> names()  { return names; }
    public String typeName()     { return typeName; }

    @Override
    protected void printTree(StringBuilder sb, int level) {
        sb.append(indent(level)).append("VarDeclCommand (").append(typeName).append(")\n");
        for (String name : names) {
            sb.append(indent(level + 1)).append(name).append("\n");
        }
    }
}