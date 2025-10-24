package com.automatas.dfa;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            com.automatas.dfa.ui.MainFrame frame = new com.automatas.dfa.ui.MainFrame();
            frame.setVisible(true);
        });
    }
}
