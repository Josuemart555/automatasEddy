package dfa.ui;

import dfa.model.DFA;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TransitionTableModel extends AbstractTableModel {
    private DFA dfa;
    private List<String> states = new ArrayList<>();
    private List<String> symbols = new ArrayList<>();
    // Estado actual resaltado (para mostrar "->" en progreso). Si es null, se mostrará "->" en el estado inicial.
    private String currentState;

    public void setDfa(DFA dfa) {
        this.dfa = dfa;
        this.states = new ArrayList<>(dfa.getStates());
        this.symbols = new ArrayList<>(dfa.getSymbols());
        this.currentState = null; // al cargar un DFA nuevo, por defecto la flecha apunta al estado inicial
        fireTableStructureChanged();
    }

    /**
     * Actualiza el estado actual para que la columna "Estado" muestre "->" en dicho estado.
     * Si se pasa null, la flecha volverá al estado inicial del AFD.
     */
    public void setCurrentState(String state) {
        this.currentState = state;
        // Refrescar toda la tabla (o al menos la primera columna) para actualizar las marcas visuales
        if (getRowCount() > 0) {
            fireTableRowsUpdated(0, getRowCount() - 1);
        } else {
            fireTableDataChanged();
        }
    }

    @Override
    public int getRowCount() {
        return dfa == null ? 0 : states.size();
    }

    @Override
    public int getColumnCount() {
        return dfa == null ? 0 : 1 + symbols.size();
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) return "Estado";
        return symbols.get(column - 1);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String state = states.get(rowIndex);
        if (columnIndex == 0) {
            StringBuilder sb = new StringBuilder();
            // "->" para el estado actual si hay simulación; si no hay, para el estado inicial
            if (dfa != null) {
                if (currentState != null) {
                    if (Objects.equals(currentState, state)) sb.append("->");
                } else {
                    if (Objects.equals(dfa.getStartState(), state)) sb.append("->");
                }
                // "*" para estados de aceptación
                if (dfa.getAcceptStates().contains(state)) sb.append("*");
            }
            sb.append(state);
            return sb.toString();
        }
        String sym = symbols.get(columnIndex - 1);
        Map<String, String> map = dfa.getTransitions().get(state);
        String to = map != null ? map.get(sym) : null;
        if (to == null) return "—";
        // Decorar el destino igual que en la columna Estado, para que se vea como en la imagen
        StringBuilder sb = new StringBuilder();
        if (Objects.equals(dfa.getStartState(), to)) sb.append("->");
        if (dfa.getAcceptStates().contains(to)) sb.append("*");
        sb.append(to);
        return sb.toString();
    }
}
