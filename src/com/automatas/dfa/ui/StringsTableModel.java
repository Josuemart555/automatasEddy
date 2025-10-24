package com.automatas.dfa.ui;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class StringsTableModel extends AbstractTableModel {
    public static class Row {
        public String input;
        public String result; // Aceptada/Rechazada/Pendiente
        public Row(String input, String result) { this.input = input; this.result = result; }
    }

    private final List<Row> rows = new ArrayList<>();

    public void clear() {
        rows.clear();
        fireTableDataChanged();
    }

    public void addString(String s) {
        rows.add(new Row(s, "Pendiente"));
        fireTableRowsInserted(rows.size()-1, rows.size()-1);
    }

    public void setResult(int idx, boolean accepted) {
        if (idx < 0 || idx >= rows.size()) return;
        rows.get(idx).result = accepted ? "Aceptada" : "Rechazada";
        fireTableRowsUpdated(idx, idx);
    }

    public Row get(int idx) { return rows.get(idx); }

    public int size() { return rows.size(); }

    @Override
    public int getRowCount() { return rows.size(); }

    @Override
    public int getColumnCount() { return 2; }

    @Override
    public String getColumnName(int column) {
        return column == 0 ? "Cadena" : "Resultado";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Row r = rows.get(rowIndex);
        return columnIndex == 0 ? r.input : r.result;
    }
}
