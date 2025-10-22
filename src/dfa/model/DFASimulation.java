package dfa.model;

import java.util.ArrayList;
import java.util.List;

public class DFASimulation {
    private final DFA dfa;
    private final String input;
    private final List<String> pathStates; // includes start state and subsequent states after each symbol
    private int index; // next symbol index to consume

    public DFASimulation(DFA dfa, String input) {
        this.dfa = dfa;
        this.input = input != null ? input : "";
        this.pathStates = new ArrayList<>();
        reset();
    }

    public void reset() {
        pathStates.clear();
        pathStates.add(dfa.getStartState());
        index = 0;
    }

    public boolean canStepForward() {
        return index < input.length();
    }

    public boolean canStepBack() {
        return index > 0;
    }

    public boolean stepForward() {
        if (!canStepForward()) return false;
        String current = pathStates.get(pathStates.size() - 1);
        String sym = String.valueOf(input.charAt(index));
        String next = dfa.step(current, sym);
        if (next == null) {
            // dead transition: stay in null/"∅" state representation
            pathStates.add(null);
        } else {
            pathStates.add(next);
        }
        index++;
        return true;
    }

    public boolean stepBack() {
        if (!canStepBack()) return false;
        pathStates.remove(pathStates.size() - 1);
        index--;
        return true;
    }

    public int getIndex() { return index; }
    public String getInput() { return input; }
    public List<String> getPathStates() { return pathStates; }

    public boolean isAccepted() {
        if (index != input.length()) return false; // only when finished
        String last = pathStates.get(pathStates.size() - 1);
        return last != null && dfa.isAccept(last);
    }
}
