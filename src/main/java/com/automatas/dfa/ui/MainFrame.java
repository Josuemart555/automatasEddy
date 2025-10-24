package com.automatas.dfa.ui;


import com.automatas.dfa.model.DFA;
import com.automatas.dfa.model.DFASimulation;
import com.automatas.dfa.parser.DFAParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    // Menu
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu mFile = new JMenu("Archivo");
    // Cards
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);
    private final JPanel homePanel = new JPanel(new GridBagLayout());
    private final JPanel simulatorPanel = new JPanel(new BorderLayout());

    // Simulator components
    private final TransitionTableModel transitionTableModel = new TransitionTableModel();
    private final JTable transitionTable = new JTable(transitionTableModel);
    private final DiagramPanel diagramPanel = new DiagramPanel();
    private final StringsTableModel stringsTableModel = new StringsTableModel();
    private final JTable stringsTable = new JTable(stringsTableModel);

    private final JTextArea editor = new JTextArea(12, 60);

    private DFA currentDfa;
    private DFASimulation currentSim;

    // Controls
    private final JButton btnPrev = new JButton("Anterior");
    private final JButton btnNext = new JButton("Siguiente");
    private final JButton btnReset = new JButton("Reiniciar");
    private final JButton btnAuto = new JButton("Auto ▶");
    private javax.swing.Timer autoTimer;

    private final JTextField inputField = new JTextField(20);
    private final JButton btnAddString = new JButton("Añadir cadena");
    private final JButton btnRunAll = new JButton("Procesar todas");

    public MainFrame() {
        super("Simulador AFD — Automatas Eddy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
        setJMenuBar(createMenuBar());

        // Home panel (modern look)
        homePanel.setBackground(new Color(247, 250, 252));
        var title = new JLabel("Simulador de Autómatas Finitos Deterministas");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(new Color(33, 37, 41));
        var subtitle = new JLabel("Cargar, editar y simular paso a paso");
        subtitle.setForeground(new Color(80, 90, 100));

        JPanel buttons = new JPanel(new GridBagLayout());
        buttons.setOpaque(false);
        GridBagConstraints bgbc = new GridBagConstraints();
        bgbc.insets = new Insets(12,12,12,12);

        // Fila 1: Abrir archivo | Acerca de | Ayuda
        bgbc.gridy = 0; bgbc.gridx = 0;
        buttons.add(createHomeButton("Abrir archivo", e -> onOpen()), bgbc);
        bgbc.gridx = 1;
        buttons.add(createHomeButton("Acerca de", e -> showAbout()), bgbc);
        bgbc.gridx = 2;
        buttons.add(createHomeButton("Ayuda", e -> showHelp()), bgbc);

        // Fila 2: Ejemplo 1 | Ejemplo 2 | Ejemplo 3
        bgbc.gridy = 1; bgbc.gridx = 0;
        buttons.add(createHomeButton("Ejemplo 1", e -> loadExample(1)), bgbc);
        bgbc.gridx = 1;
        buttons.add(createHomeButton("Ejemplo 2", e -> loadExample(2)), bgbc);
        bgbc.gridx = 2;
        buttons.add(createHomeButton("Ejemplo 3", e -> loadExample(3)), bgbc);

        // Fila 3 (centrado): Salir
        bgbc.gridy = 2; bgbc.gridx = 1;
        buttons.add(createHomeButton("Salir", e -> dispose()), bgbc);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(8,8,8,8);
        homePanel.add(title, gbc);
        gbc.gridy = 1; homePanel.add(subtitle, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(24,8,8,8);
        homePanel.add(buttons, gbc);

        // Simulator panel
        simulatorPanel.add(createTopToolbar(), BorderLayout.NORTH);
        simulatorPanel.add(createCenterTabs(), BorderLayout.CENTER);
        simulatorPanel.add(createBottomControls(), BorderLayout.SOUTH);

        root.add(homePanel, "home");
        root.add(simulatorPanel, "sim");
        add(root);

        updateControlsEnabled();
    }

    private JMenuBar createMenuBar() {
        // Reuse a single menu bar instance and a single File menu
        menuBar.removeAll();

        // Build Archivo menu content
        mFile.removeAll();
        mFile.add(new JMenuItem(new AbstractAction("Abrir…") {
            @Override public void actionPerformed(ActionEvent e) { onOpen(); }
        }));
        mFile.add(new JMenuItem(new AbstractAction("Nuevo") {
            @Override public void actionPerformed(ActionEvent e) { onNew(); }
        }));
        mFile.add(new JMenuItem(new AbstractAction("Guardar…") {
            @Override public void actionPerformed(ActionEvent e) { onSave(); }
        }));
        mFile.addSeparator();
        mFile.add(new JMenuItem(new AbstractAction("Regresar a inicio") {
            @Override public void actionPerformed(ActionEvent e) { goHome(); }
        }));
        mFile.addSeparator();
        mFile.add(new JMenuItem(new AbstractAction("Salir") {
            @Override public void actionPerformed(ActionEvent e) { dispose(); }
        }));

        // Other menus can be constructed here if needed (e.g., Ejemplos, Acerca de),
        // but we control visibility of mFile depending on the active screen.

        // Do not add mFile here; it will be added when entering the simulator screen.
        return menuBar;
    }

    private JButton createHomeButton(String text, ActionListener listener) {
        JButton b = new JButton(text);
        b.addActionListener(listener);
        stylePrimaryButton(b);
        return b;
    }

    private void stylePrimaryButton(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(new Color(69, 123, 157));
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(12,18,12,18));
    }

    private JToolBar createTopToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.setBorder(new EmptyBorder(8,8,8,8));

        tb.add(new JLabel("Cadena:"));
        tb.add(inputField);
        JButton btnSet = new JButton("Cargar cadena");
        btnSet.addActionListener(e -> startSimulationFromInput());
        tb.add(btnSet);
        tb.addSeparator();
//        tb.add(btnAddString);
        btnAddString.addActionListener(e -> {
            String s = inputField.getText();
            if (s != null) {
                stringsTableModel.addString(s);
            }
        });
        tb.add(btnRunAll);
        btnRunAll.addActionListener(e -> processAllStrings());

        return tb;
    }

    private JComponent createCenterTabs() {
        // Construir una vista dividida en 4 cuadrantes usando JSplitPane anidados
        // Arriba-izquierda: Tabla transiciones
        // Abajo-izquierda: Cadenas
        // Arriba-derecha: Diagrama AFD
        // Abajo-derecha: Editor

        // Paneles con scroll donde aplica
        transitionTable.setFillsViewportHeight(true);
        transitionTable.setRowSelectionAllowed(true);
        transitionTable.setColumnSelectionAllowed(true);
        // Estilo para parecerse a la imagen
        transitionTable.setSelectionBackground(new Color(255, 255, 153));
        transitionTable.setSelectionForeground(Color.BLACK);
        transitionTable.setDefaultRenderer(Object.class, new TransitionCellRenderer());
        transitionTable.setRowHeight(22);
        JScrollPane transitionsScroll = new JScrollPane(transitionTable);

        stringsTable.setFillsViewportHeight(true);
        stringsTable.setRowSorter(new TableRowSorter<>(stringsTable.getModel()));
        JScrollPane stringsScroll = new JScrollPane(stringsTable);

        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane editorScroll = new JScrollPane(editor);

        JPanel diagWrap = new JPanel(new BorderLayout());
        diagWrap.add(diagramPanel, BorderLayout.CENTER);

        // Split vertical izquierdo (transiciones arriba, cadenas abajo)
        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, transitionsScroll, stringsScroll);
        leftSplit.setOneTouchExpandable(true);
        leftSplit.setResizeWeight(0.5);

        // Split vertical derecho (diagrama arriba, editor abajo)
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, diagWrap, editorScroll);
        rightSplit.setOneTouchExpandable(true);
        rightSplit.setResizeWeight(0.6);

        // Split horizontal principal (izquierda/derecha)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, rightSplit);
        mainSplit.setOneTouchExpandable(true);
        mainSplit.setResizeWeight(0.5);

        // Ajustes visuales
        mainSplit.setBorder(null);
        leftSplit.setBorder(null);
        rightSplit.setBorder(null);

        return mainSplit;
    }

    private JPanel createBottomControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(new EmptyBorder(8,8,8,8));
        btnPrev.addActionListener(e -> stepBack());
        btnNext.addActionListener(e -> stepForward());
        btnReset.addActionListener(e -> resetSimulation());
        btnAuto.addActionListener(e -> toggleAuto());
        panel.add(btnPrev);
        panel.add(btnNext);
        panel.add(btnReset);
//        panel.add(btnAuto);
        return panel;
    }

    // Actions
    private void onOpen() {
        JFileChooser ch = new JFileChooser();
        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = ch.getSelectedFile();
            try {
                DFAParser.ParseResult res = DFAParser.parseFileAll(f);
                setCurrentDfa(res.getDfa());
                editor.setText(new String(java.nio.file.Files.readAllBytes(f.toPath())));
                stringsTableModel.clear();
                for (String s : res.getStrings()) {
                    stringsTableModel.addString(s);
                }
                goToSim();
            } catch (Exception ex) {
                showError("No se pudo abrir el archivo: " + ex.getMessage());
            }
        }
    }

    private void onNew() {
        String template = "# Definición de AFD\n" +
                "symbols: a,b\n" +
                "states: q0,q1\n" +
                "start: q0\n" +
                "finals: q1\n" +
                "transitions:\n" +
                "q0,a->q1\n" +
                "q0,b->q0\n" +
                "q1,a->q1\n" +
                "q1,b->q0\n" +
                "strings:\n" +
                "abba\n";
        editor.setText(template);
        try {
            DFAParser.ParseResult res = DFAParser.parseAll(template);
            setCurrentDfa(res.getDfa());
            stringsTableModel.clear();
            for (String s : res.getStrings()) {
                stringsTableModel.addString(s);
            }
        } catch (Exception ex) {
            // ignore template errors
        }
        goToSim();
    }

    private void onSave() {
        String text = editor.getText();
        JFileChooser ch = new JFileChooser();
        if (ch.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = ch.getSelectedFile();
            try {
                DFAParser.saveTextToFile(text, f);
                JOptionPane.showMessageDialog(this, "Archivo guardado.");
            } catch (IOException e) {
                showError("No se pudo guardar: " + e.getMessage());
            }
        }
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "Simulador AFD\nUniversidad: [Tu Universidad]\nCurso: Teoría de Autómatas\nFecha: " + java.time.LocalDate.now() +
                        "\nIntegrantes: [Nombres]",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHelp() {
        try {
            // Intentar abrir el PDF desde los recursos
            var resource = getClass().getClassLoader().getResource("guia_de_usuario.pdf");

            if (resource != null) {
                // Si el PDF está en recursos, copiarlo temporalmente y abrirlo
                File tempFile = File.createTempFile("guia_usuario", ".pdf");
                tempFile.deleteOnExit();

                try (var in = resource.openStream();
                     var out = new java.io.FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

                // Abrir el PDF con la aplicación predeterminada
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.OPEN)) {
                        desktop.open(tempFile);
                        return;
                    }
                }

                // Si Desktop no está disponible, intentar con el navegador
                openInBrowser(tempFile.toURI().toString());
            } else {
                // Si no se encuentra el PDF, mostrar mensaje
                JOptionPane.showMessageDialog(this,
                        "No se encontró el archivo de ayuda (guia_usuario.pdf).\n\n" +
                                "Cómo usar:\n1) Abra o cree un AFD desde el menú Archivo.\n" +
                                "2) Edite la definición en la pestaña Editor si lo desea.\n" +
                                "3) Ingrese una cadena y presione Cargar cadena.\n" +
                                "4) Use los controles Anterior/Siguiente/Reiniciar/Auto para simular paso a paso.\n" +
                                "5) En la pestaña Cadenas agregue cadenas y presione 'Procesar todas'.",
                        "Ayuda", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            showError("No se pudo abrir la guía de usuario: " + ex.getMessage());
        }
    }

    private void goToSim() {
        cardLayout.show(root, "sim");
        setFileMenuVisible(true);
    }
    private void goHome() {
        cardLayout.show(root, "home");
        setFileMenuVisible(false);
    }

    private void setFileMenuVisible(boolean visible) {
        if (visible) {
            boolean present = false;
            for (int i = 0; i < menuBar.getMenuCount(); i++) {
                if (menuBar.getMenu(i) == mFile) { present = true; break; }
            }
            if (!present) {
                menuBar.add(mFile, 0);
                menuBar.revalidate();
                menuBar.repaint();
            }
            if (getJMenuBar() == null) {
                setJMenuBar(menuBar);
            }
        } else {
            menuBar.remove(mFile);
            menuBar.revalidate();
            menuBar.repaint();
        }
    }

    private void setCurrentDfa(DFA dfa) {
        this.currentDfa = dfa;
        transitionTableModel.setDfa(dfa);
        diagramPanel.setDfa(dfa);
        resetSimulation();
        updateControlsEnabled();
    }

    private void resetSimulation() {
        if (currentDfa == null || currentSim == null) return;
        currentSim.reset();
        updateDiagramHighlight();
        updateControlsEnabled();
    }

    private void startSimulationFromInput() {
        if (currentDfa == null) {
            showError("Primero cargue un AFD (Archivo > Abrir o Nuevo).");
            return;
        }
        String in = inputField.getText();
        if (in == null) in = "";
        currentSim = new DFASimulation(currentDfa, in);
        updateDiagramHighlight();
        updateControlsEnabled();
    }

    private void stepForward() {
        if (currentSim == null) { startSimulationFromInput(); }
        if (currentSim == null) return;
        if (currentSim.stepForward()) {
            updateDiagramHighlight();
            updateControlsEnabled();
        } else {
            if (autoTimer != null && autoTimer.isRunning()) autoTimer.stop();
        }
    }

    private void stepBack() {
        if (currentSim == null) return;
        if (currentSim.stepBack()) {
            updateDiagramHighlight();
            updateControlsEnabled();
        }
    }

    private void toggleAuto() {
        if (currentSim == null) { startSimulationFromInput(); }
        if (currentSim == null) return;
        if (autoTimer == null) {
            autoTimer = new javax.swing.Timer(700, e -> stepForward());
        }
        if (autoTimer.isRunning()) {
            autoTimer.stop();
            btnAuto.setText("Auto ▶");
        } else {
            autoTimer.start();
            btnAuto.setText("Pausar ⏸");
        }
    }

    private void processAllStrings() {
        if (currentDfa == null) { showError("Cargue un AFD primero."); return; }
        for (int i = 0; i < stringsTableModel.size(); i++) {
            String s = stringsTableModel.get(i).input;
            DFASimulation sim = new DFASimulation(currentDfa, s);
            while (sim.canStepForward()) sim.stepForward();
            stringsTableModel.setResult(i, sim.isAccepted());
        }
    }

    private void updateDiagramHighlight() {
        if (currentDfa == null || currentSim == null) return;
        java.util.List<String> path = currentSim.getPathStates();
        String current = path.get(path.size() - 1);
        String from = null, sym = null, to = null;
        int idx = currentSim.getIndex();
        if (idx > 0) {
            from = path.get(idx - 1);
            sym = String.valueOf(currentSim.getInput().charAt(idx - 1));
            to = current;
        }
        diagramPanel.setHighlight(current, from, sym, to);

        // Actualizar marcas "->" en la columna Estado
        transitionTableModel.setCurrentState(current);

        // Resaltar en la tabla de transiciones
        try {
            int row = -1;
            for (int r = 0; r < transitionTable.getRowCount(); r++) {
                Object val = transitionTable.getValueAt(r, 0);
                String raw = val == null ? null : val.toString().replace("->", "").replace("*", "");
                if (Objects.equals(current, raw)) { row = r; break; }
            }
            int col = 0; // columna 0 = "Estado"
            if (sym != null) {
                for (int c = 1; c < transitionTable.getColumnCount(); c++) {
                    if (Objects.equals(sym, transitionTable.getColumnName(c))) { col = c; break; }
                }
            }
            if (row >= 0) {
                transitionTable.changeSelection(row, col, false, false);
            } else {
                transitionTable.clearSelection();
            }
        } catch (Exception ignore) {}
    }

    private void updateControlsEnabled() {
        boolean hasDfa = currentDfa != null;
        boolean hasSim = currentSim != null;
        btnPrev.setEnabled(hasSim && currentSim.canStepBack());
        btnNext.setEnabled(hasSim && currentSim.canStepForward());
        btnReset.setEnabled(hasSim);
        btnAuto.setEnabled(hasSim);
    }

    private void loadExample(int n) {
        String text = switch (n) {
            case 1 -> EXAMPLE_1;
            case 2 -> EXAMPLE_2;
            default -> EXAMPLE_3;
        };
        editor.setText(text);
        try {
            DFAParser.ParseResult res = DFAParser.parseAll(text);
            setCurrentDfa(res.getDfa());
            stringsTableModel.clear();
            for (String s : res.getStrings()) {
                stringsTableModel.addString(s);
            }
            goToSim();
        } catch (Exception ex) {
            showError("Error en ejemplo: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void openInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new java.net.URI(url));
            } else {
                // Alternativa para sistemas sin Desktop API
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec("open " + url);
                } else if (os.contains("nix") || os.contains("nux")) {
                    Runtime.getRuntime().exec("xdg-open " + url);
                }
            }
        } catch (Exception e) {
            showError("No se pudo abrir el navegador: " + e.getMessage());
        }
    }

    // Example DFAs
    private static final String EXAMPLE_1 = "" +
            "# AFD que acepta cadenas con número par de a's\n" +
            "symbols: a,b\n" +
            "states: q0,q1\n" +
            "start: q0\n" +
            "finals: q0\n" +
            "transitions:\n" +
            "q0,a->q1\n" +
            "q0,b->q0\n" +
            "q1,a->q0\n" +
            "q1,b->q1\n" +
            "strings:\n" +
            "a\n" +
            "aa\n" +
            "aba\n";

    private static final String EXAMPLE_2 = "" +
            "# AFD que acepta cadenas que terminan con 'ab'\n" +
            "symbols: a,b\n" +
            "states: q0,q1,q2\n" +
            "start: q0\n" +
            "finals: q2\n" +
            "transitions:\n" +
            "q0,a->q1\n" +
            "q0,b->q0\n" +
            "q1,a->q1\n" +
            "q1,b->q2\n" +
            "q2,a->q1\n" +
            "q2,b->q0\n" +
            "strings:\n" +
            "ab\n" +
            "aab\n" +
            "b\n";

    private static final String EXAMPLE_3 = "" +
            "# AFD sobre {0,1} que acepta números binarios múltiplos de 3\n" +
            "symbols: 0,1\n" +
            "states: q0,q1,q2\n" +
            "start: q0\n" +
            "finals: q0\n" +
            "transitions:\n" +
            "q0,0->q0\n" +
            "q0,1->q1\n" +
            "q1,0->q2\n" +
            "q1,1->q0\n" +
            "q2,0->q1\n" +
            "q2,1->q2\n" +
            "strings:\n" +
            "0\n" +
            "11\n" +
            "101\n";
}
