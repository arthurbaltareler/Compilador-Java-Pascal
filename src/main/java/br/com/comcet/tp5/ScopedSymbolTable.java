package br.com.comcet.tp5;

import br.com.comcet.tp1.Symbol;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class ScopedSymbolTable {

    // Pilha de escopos — cada escopo é um Map de nome -> Symbol
    private final Deque<Map<String, Symbol>> scopes = new ArrayDeque<>();

    // Abre um novo escopo (empilha um Map vazio)
    public void enterScope() {
        scopes.push(new HashMap<>());
    }

    // Fecha o escopo atual (desempilha)
    public void exitScope() {
        if (!scopes.isEmpty()) {
            scopes.pop();
        }
    }

    // Adiciona no escopo atual — erro se já existir no mesmo escopo
    public void add(String name, Symbol symbol) {
        Map<String, Symbol> current = scopes.peek();
        if (current == null) {
            throw new SemanticException("Nenhum escopo ativo.");
        }
        if (current.containsKey(name)) {
            throw new SemanticException("Variavel ja declarada no escopo atual: " + name);
        }
        current.put(name, symbol);
    }

    // Busca do escopo atual até o global
    public Symbol lookup(String name) {
        for (Map<String, Symbol> scope : scopes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null; // não encontrado em nenhum escopo
    }

    // Verifica se existe no escopo atual (apenas)
    public boolean existsInCurrentScope(String name) {
        Map<String, Symbol> current = scopes.peek();
        return current != null && current.containsKey(name);
    }
}