package com.automatas.dfa.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Renderer para la tabla de transiciones:
 * - Texto en azul y negrita para celdas que muestran un estado (cualquier valor distinto de "—").
 * - Selección con fondo amarillo pálido para asemejarse a la imagen.
 */
public class TransitionCellRenderer extends DefaultTableCellRenderer {
    private static final Color YELLOW_SEL = new Color(255, 255, 153);
    private static final Color BLUE_TEXT = new Color(0, 85, 170);
    private static final Color NORMAL_TEXT = new Color(33, 37, 41);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        String text = value == null ? "" : value.toString();
        boolean isStateValue = !text.isEmpty() && !"—".equals(text);

        // Colores
        setForeground(isStateValue ? BLUE_TEXT : NORMAL_TEXT);
        setFont(getFont().deriveFont(isStateValue ? Font.BOLD : Font.PLAIN));

        // Fondo de selección amarillo
        if (isSelected) {
            setBackground(YELLOW_SEL);
        } else {
            setBackground(Color.WHITE);
        }

        setOpaque(true);
        setHorizontalAlignment(LEFT);
        return this;
    }
}
