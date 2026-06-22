package br.com.comcet.tp1.ast;

import br.com.comcet.tp1.Type;

public final class CastExpr extends Expression {
    private final Expression inner;
    private final Type fromType;
    private final Type toType;

    public CastExpr(Expression inner, Type fromType, Type toType) {
        this.inner    = inner;
        this.fromType = fromType;
        this.toType   = toType;
        setResolvedType(toType);
    }

    public Expression inner() { return inner; }
    public Type fromType()    { return fromType; }
    public Type toType()      { return toType; }

    @Override
    protected void printTree(StringBuilder sb, int level) {
        sb.append(indent(level))
                .append("CastExpr (").append(fromType).append(" -> ").append(toType).append(")\n");
        inner.printTree(sb, level + 1);
    }
}