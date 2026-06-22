package br.com.comcet.tp1.ast;

public final class Param {
    private final String name;
    private final String typeName;

    public Param(String name, String typeName) {
        this.name     = name;
        this.typeName = typeName;
    }

    public String name()     { return name; }
    public String typeName() { return typeName; }

    @Override
    public String toString() {
        return name + ": " + typeName;
    }
}