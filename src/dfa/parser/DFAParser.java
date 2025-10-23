package dfa.parser;

import dfa.model.DFA;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Parses and writes DFA definitions using a simple text format, e.g.:
 * symbols: a,b
 * states: q0,q1,q2
 * start: q0
 * finals: q2
 * transitions:
 * q0,a->q1
 * q1,b->q2
 * q2,a->q2
 * strings:
 * ab
 * aab
 */
public class DFAParser {

    public static DFA parse(String text) {
        BufferedReader br = new BufferedReader(new StringReader(text));
        String line;
        Set<String> symbols = new LinkedHashSet<>();
        Set<String> states = new LinkedHashSet<>();
        String start = null;
        Set<String> finals = new LinkedHashSet<>();
        Map<String, Map<String, String>> transitions = new LinkedHashMap<>();
        List<String> strings = new ArrayList<>();

        boolean inTransitions = false;
        boolean inStrings = false;
        int transRowIndex = 0; // para formato de tabla (filas sin estado origen explícito)

        try {
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue;

                String lower = line.toLowerCase(Locale.ROOT);
                if (lower.equals("transitions:") || lower.equals("transiciones:")) {
                    inTransitions = true; inStrings = false; transRowIndex = 0; continue;
                }
                if (lower.equals("strings:") || lower.equals("cadenas:") || lower.equals("cadenas a analizar:")) {
                    inStrings = true; inTransitions = false; continue;
                }

                if (!inTransitions && !inStrings) {
                    if (lower.startsWith("symbols:") || lower.startsWith("simbolos:") || lower.startsWith("símbolos:")) {
                        symbols.addAll(splitCSV(line.substring(line.indexOf(':') + 1)));
                    } else if (lower.startsWith("states:") || lower.startsWith("estados:")) {
                        states.addAll(splitCSV(line.substring(line.indexOf(':') + 1)));
                    } else if (lower.startsWith("start:") || lower.startsWith("estado inicial:")) {
                        start = line.substring(line.indexOf(':') + 1).trim();
                    } else if (lower.startsWith("finals:") ||
                            lower.startsWith("finales:") ||
                            lower.startsWith("estados de aceptación:") ||
                            lower.startsWith("estados de aceptacion:")) {
                        finals.addAll(splitCSV(line.substring(line.indexOf(':') + 1)));
                    }
                } else if (inTransitions) {
                    // Soportar:
                    // 1) from,sym->to
                    // 2) from: to0,to1,...
                    // 3) to0,to1,... (fila sin 'from', se deduce por orden de 'states')
                    if (line.contains("->")) {
                        String[] parts = line.split("->");
                        if (parts.length != 2) throw new IllegalArgumentException("Transición inválida: " + line);
                        String left = parts[0].trim();
                        String to = parts[1].trim();
                        String[] ls = left.split(",");
                        if (ls.length != 2) throw new IllegalArgumentException("Transición inválida (lado izquierdo): " + line);
                        String from = ls[0].trim();
                        String sym = ls[1].trim();
                        transitions.computeIfAbsent(from, k -> new LinkedHashMap<>()).put(sym, to);
                    } else if (line.contains(":")) {
                        String[] p = line.split(":", 2);
                        String from = p[0].trim();
                        List<String> tos = splitCSV(p[1]);
                        List<String> syms = new ArrayList<>(symbols);
                        if (tos.size() != syms.size()) {
                            throw new IllegalArgumentException("Fila de transición no coincide con número de símbolos: " + line);
                        }
                        Map<String, String> map = transitions.computeIfAbsent(from, k -> new LinkedHashMap<>());
                        for (int i = 0; i < syms.size(); i++) {
                            map.put(syms.get(i), tos.get(i).trim());
                        }
                    } else {
                        // Fila de destinos sin estado origen explícito (formato de tabla)
                        List<String> tos = splitCSV(line);
                        List<String> syms = new ArrayList<>(symbols);
                        List<String> sts = new ArrayList<>(states);

                        if (tos.size() != syms.size()) {
                            throw new IllegalArgumentException("Fila de transición no coincide con número de símbolos (" + syms.size() + "): " + line + 
                                    ". Use el formato 'estado_origen,símbolo->estado_destino' o proporcione todos los destinos separados por comas");
                        }
                        if (transRowIndex >= sts.size()) {
                            // En formato de tabla, solo procesamos tantas filas como estados hay
                            // Las filas adicionales se ignoran silenciosamente
                            continue;
                        }
                        String from = sts.get(transRowIndex++);
                        Map<String, String> map = transitions.computeIfAbsent(from, k -> new LinkedHashMap<>());
                        for (int i = 0; i < syms.size(); i++) {
                            map.put(syms.get(i), tos.get(i).trim());
                        }
                    }
                } else if (inStrings) {
                    // En parse() no usamos strings, pero ignoramos líneas aquí
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (start == null) throw new IllegalArgumentException("Falta 'start:'");
        return new DFA(symbols, states, start, finals, transitions);
    }

    public static String toText(DFA dfa, List<String> strings) {
        StringBuilder sb = new StringBuilder();
        sb.append("symbols: ").append(String.join(",", dfa.getSymbols())).append('\n');
        sb.append("states: ").append(String.join(",", dfa.getStates())).append('\n');
        sb.append("start: ").append(dfa.getStartState()).append('\n');
        sb.append("finals: ").append(String.join(",", dfa.getAcceptStates())).append('\n');
        sb.append("transitions:").append('\n');
        for (String from : dfa.getTransitions().keySet()) {
            Map<String, String> map = dfa.getTransitions().get(from);
            for (String sym : map.keySet()) {
                String to = map.get(sym);
                sb.append(from).append(',').append(sym).append("->").append(to).append('\n');
            }
        }
        sb.append("strings:").append('\n');
        if (strings != null) {
            for (String s : strings) sb.append(s).append('\n');
        }
        return sb.toString();
    }

    public static DFA parseFile(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return parse(text);
        }
    }

    public static void saveTextToFile(String text, File file) throws IOException {
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static List<String> splitCSV(String s) {
        List<String> list = new ArrayList<>();
        for (String p : s.split(",")) {
            String t = p.trim();
            if (!t.isEmpty()) list.add(t);
        }
        return list;
    }

    // Nuevo: resultado con DFA y cadenas
    public static class ParseResult {
        private final DFA dfa;
        private final List<String> strings;
        public ParseResult(DFA dfa, List<String> strings) {
            this.dfa = dfa;
            this.strings = Collections.unmodifiableList(new ArrayList<>(strings));
        }
        public DFA getDfa() { return dfa; }
        public List<String> getStrings() { return strings; }
    }

    // Nuevo: parsea devolviendo AFD y cadenas
    public static ParseResult parseAll(String text) {
        BufferedReader br = new BufferedReader(new StringReader(text));
        String line;
        Set<String> symbols = new LinkedHashSet<>();
        Set<String> states = new LinkedHashSet<>();
        String start = null;
        Set<String> finals = new LinkedHashSet<>();
        Map<String, Map<String, String>> transitions = new LinkedHashMap<>();
        List<String> strings = new ArrayList<>();

        boolean inTransitions = false;
        boolean inStrings = false;
        int transRowIndex = 0;

        try {
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue;

                String lower = line.toLowerCase(Locale.ROOT);
                if (lower.equals("transitions:") || lower.equals("transiciones:")) {
                    inTransitions = true; inStrings = false; transRowIndex = 0; continue;
                }
                if (lower.equals("strings:") || lower.equals("cadenas:") || lower.equals("cadenas a analizar:")) {
                    inStrings = true; inTransitions = false; continue;
                }

                if (!inTransitions && !inStrings) {
                    if (lower.startsWith("symbols:") || lower.startsWith("simbolos:") || lower.startsWith("símbolos:")) {
                        symbols.addAll(splitCSV(line.substring(line.indexOf(':') + 1)));
                    } else if (lower.startsWith("states:") || lower.startsWith("estados:")) {
                        states.addAll(splitCSV(line.substring(line.indexOf(':') + 1)));
                    } else if (lower.startsWith("start:") || lower.startsWith("estado inicial:")) {
                        start = line.substring(line.indexOf(':') + 1).trim();
                    } else if (lower.startsWith("finals:") ||
                            lower.startsWith("finales:") ||
                            lower.startsWith("estados de aceptación:") ||
                            lower.startsWith("estados de aceptacion:")) {
                        finals.addAll(splitCSV(line.substring(line.indexOf(':') + 1)));
                    }
                } else if (inTransitions) {
                    if (line.contains("->")) {
                        // from,sym->to
                        String[] parts = line.split("->");
                        if (parts.length != 2) throw new IllegalArgumentException("Transición inválida: " + line);
                        String left = parts[0].trim();
                        String to = parts[1].trim();
                        String[] ls = left.split(",");
                        if (ls.length != 2) throw new IllegalArgumentException("Transición inválida (lado izquierdo): " + line);
                        String from = ls[0].trim();
                        String sym = ls[1].trim();
                        transitions.computeIfAbsent(from, k -> new LinkedHashMap<>()).put(sym, to);
                    } else if (line.contains(":")) {
                        // from: to0,to1,...
                        String[] p = line.split(":", 2);
                        String from = p[0].trim();
                        List<String> tos = splitCSV(p[1]);
                        List<String> syms = new ArrayList<>(symbols);
                        if (tos.size() != syms.size()) {
                            throw new IllegalArgumentException("Fila de transición no coincide con número de símbolos: " + line);
                        }
                        Map<String, String> map = transitions.computeIfAbsent(from, k -> new LinkedHashMap<>());
                        for (int i = 0; i < syms.size(); i++) {
                            map.put(syms.get(i), tos.get(i).trim());
                        }
                    } else {
                        // to0,to1,... (sin 'from', deducir por el orden de estados - formato de tabla)
                        List<String> tos = splitCSV(line);
                        List<String> syms = new ArrayList<>(symbols);
                        List<String> sts = new ArrayList<>(states);

                        if (tos.size() != syms.size()) {
                            throw new IllegalArgumentException("Fila de transición no coincide con número de símbolos (" + syms.size() + "): " + line + 
                                    ". Use el formato 'estado_origen,símbolo->estado_destino' o proporcione todos los destinos separados por comas");
                        }
                        if (transRowIndex >= sts.size()) {
                            // En formato de tabla, solo procesamos tantas filas como estados hay
                            // Las filas adicionales se ignoran silenciosamente
                            continue;
                        }
                        String from = sts.get(transRowIndex++);
                        Map<String, String> map = transitions.computeIfAbsent(from, k -> new LinkedHashMap<>());
                        for (int i = 0; i < syms.size(); i++) {
                            map.put(syms.get(i), tos.get(i).trim());
                        }
                    }
                } else if (inStrings) {
                    // Permitir cadenas separadas por comas dentro de la misma línea (ej. 1,0,0,1 o x,x,y)
                    // Interpretar las comas como separadores de símbolos
                    List<String> symbolsList = splitCSV(line);
                    String s = String.join("", symbolsList);
                    strings.add(s);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (start == null) throw new IllegalArgumentException("Falta 'start:'");
        DFA dfa = new DFA(symbols, states, start, finals, transitions);
        return new ParseResult(dfa, strings);
    }

    // Nuevo: versión desde archivo
    public static ParseResult parseFileAll(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return parseAll(text);
        }
    }
}
