package br.com.comcet.tp1.ast;

import br.com.comcet.tp1.Type;

public abstract class Expression extends AstNode {
    private Type type; // tipo resolvido pelo TypeChecker (Etapa 6)

    public Type getResolvedType() {
        return type;
    }

    public void setResolvedType(Type type) {
        this.type = type;
    }
}