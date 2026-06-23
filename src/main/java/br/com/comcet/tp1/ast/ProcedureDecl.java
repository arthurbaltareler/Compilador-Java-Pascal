package br.com.comcet.tp1.ast;

import java.util.List;

public final class ProcedureDecl extends AstNode {
    private final String name;
    private final List<Param> params;
    private final List<VarDeclCommand> localVars;
    private final BlockCommand body;

    public ProcedureDecl(String name, List<Param> params,
                         List<VarDeclCommand> localVars, BlockCommand body) {
        this.name      = name;
        this.params    = params;
        this.localVars = localVars;
        this.body      = body;
    }

    public String name()                    { return name; }
    public List<Param> params()             { return params; }
    public List<VarDeclCommand> localVars() { return localVars; }
    public BlockCommand body()              { return body; }

    @Override
    protected void printTree(StringBuilder sb, int level) {
        sb.append(indent(level)).append("ProcedureDecl ").append(name).append("\n");
        sb.append(indent(level + 1)).append("params: ").append(params).append("\n");
        for (VarDeclCommand v : localVars) v.printTree(sb, level + 1);
        body.printTree(sb, level + 1);
    }
}