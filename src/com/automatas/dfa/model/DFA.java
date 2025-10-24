package com.automatas.dfa.model;

import java.util.*;

/**
 * Deterministic Finite Automaton (DFA) model.
 */
public class DFA {
    private final Set<String> symbols;
    private final Set<String> states;
    private final String startState;
    private final Set<String> acceptStates;
    // transition: (state, symbol) -> state
    private final Map<String, Map<String, String>> transitions;

    public DFA(Set<String> symbols,
               Set<String> states,
               String startState,
               Set<String> acceptStates,
               Map<String, Map<String, String>> transitions) {
        this.symbols = Collections.unmodifiableSet(new LinkedHashSet<>(symbols));
        this.states = Collections.unmodifiableSet(new LinkedHashSet<>(states));
        this.startState = startState;
        this.acceptStates = Collections.unmodifiableSet(new LinkedHashSet<>(acceptStates));
        // deep copy
        Map<String, Map<String, String>> t = new LinkedHashMap<>();
        for (String s : transitions.keySet()) {
            t.put(s, new LinkedHashMap<>(transitions.get(s)));
        }
        this.transitions = Collections.unmodifiableMap(t);
        validate();
    }

    private void validate() throws IllegalArgumentException {
        if (!states.contains(startState))
            throw new IllegalArgumentException("Estado inicial no pertenece al conjunto de estados");
        if (!states.containsAll(acceptStates))
            throw new IllegalArgumentException("Al menos un estado de aceptación no pertenece al conjunto de estados");
        for (Map.Entry<String, Map<String, String>> e : transitions.entrySet()) {
            String from = e.getKey();
            if (!states.contains(from))
                throw new IllegalArgumentException("Transición desde estado desconocido: " + from);
            for (Map.Entry<String, String> e2 : e.getValue().entrySet()) {
                String sym = e2.getKey();
                String to = e2.getValue();
                if (!symbols.contains(sym))
                    throw new IllegalArgumentException("Símbolo desconocido en transición: " + sym);
                if (!states.contains(to))
                    throw new IllegalArgumentException("Transición hacia estado desconocido: " + to);
            }
        }
    }

    public Set<String> getSymbols() { return symbols; }
    public Set<String> getStates() { return states; }
    public String getStartState() { return startState; }
    public Set<String> getAcceptStates() { return acceptStates; }
    public Map<String, Map<String, String>> getTransitions() { return transitions; }

    public String step(String currentState, String symbol) {
        Map<String, String> map = transitions.get(currentState);
        if (map == null) return null;
        return map.get(symbol);
    }

    public boolean isAccept(String state) {
        return acceptStates.contains(state);
    }
}
