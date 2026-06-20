package br.com.comcet.tp1;

public enum Type {
    INTEGER,
    REAL,
    BOOLEAN,
    STRING,
    ERROR;   // usado quando uma expressão tem tipo inválido

    // Converte o nome textual do tipo (ex: "integer") para o enum
    public static Type fromText(String text) {
        if (text == null) return ERROR;
        switch (text.toLowerCase()) {
            case "integer": return INTEGER;
            case "real":    return REAL;
            case "boolean": return BOOLEAN;
            case "string":  return STRING;
            default:        return ERROR;
        }
    }
}