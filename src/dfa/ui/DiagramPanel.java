package dfa.ui;

import dfa.model.DFA;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Simple diagram painter for DFA. Draws states in a circle and transitions as arrows.
 * Highlights current state and last traversed edge using provided parameters.
 */
public class DiagramPanel extends JPanel {
    private DFA dfa;
    private String highlightState;
    private String lastFrom;
    private String lastSym;
    private String lastTo;

    public DiagramPanel() {
        setBackground(new Color(245, 247, 250));
    }

    public void setDfa(DFA dfa) {
        this.dfa = dfa;
        repaint();
    }

    public void setHighlight(String state, String from, String sym, String to) {
        this.highlightState = state;
        this.lastFrom = from;
        this.lastSym = sym;
        this.lastTo = to;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (dfa == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int cx = w / 2;
        int cy = h / 2;
        int radius = Math.max(80, Math.min(w, h) / 2 - 60);

        java.util.List<String> states = new ArrayList<>(dfa.getStates());
        int n = states.size();
        Map<String, Point> pos = new HashMap<>();
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            int x = cx + (int) (radius * Math.cos(angle));
            int y = cy + (int) (radius * Math.sin(angle));
            pos.put(states.get(i), new Point(x, y));
        }

        // draw transitions first
        for (String from : dfa.getTransitions().keySet()) {
            Map<String, String> map = dfa.getTransitions().get(from);
            for (Map.Entry<String, String> e : map.entrySet()) {
                String sym = e.getKey();
                String to = e.getValue();
                Point p1 = pos.get(from);
                Point p2 = pos.get(to);
                if (p1 == null || p2 == null) continue;
                boolean highlightEdge = Objects.equals(lastFrom, from) && Objects.equals(lastSym, sym) && Objects.equals(lastTo, to);
                drawArrow(g2, p1, p2, sym, highlightEdge);
            }
        }

        // draw states
        for (String s : states) {
            Point p = pos.get(s);
            boolean isAccept = dfa.getAcceptStates().contains(s);
            boolean isStart = dfa.getStartState().equals(s);
            boolean isHighlight = Objects.equals(highlightState, s);
            drawState(g2, p.x, p.y, s, isStart, isAccept, isHighlight);
        }
        g2.dispose();
    }

    private void drawState(Graphics2D g2, int x, int y, String name, boolean start, boolean accept, boolean highlight) {
        int r = 26;
        Color base = highlight ? new Color(69, 123, 157) : new Color(96, 108, 118);
        g2.setColor(new Color(255,255,255));
        g2.fillOval(x - r, y - r, 2 * r, 2 * r);
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(base);
        g2.drawOval(x - r, y - r, 2 * r, 2 * r);
        if (accept) {
            g2.drawOval(x - r + 4, y - r + 4, 2 * (r - 4), 2 * (r - 4));
        }
        // start arrow
        if (start) {
            g2.setColor(new Color(38, 70, 83));
            g2.drawLine(x - 50, y, x - r, y);
            g2.fillPolygon(new int[]{x - r, x - r - 8, x - r - 8}, new int[]{y, y - 5, y + 5}, 3);
        }
        // name
        g2.setColor(new Color(33, 37, 41));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(name);
        g2.drawString(name, x - tw / 2, y + fm.getAscent() / 2 - 2);
    }

    private void drawArrow(Graphics2D g2, Point p1, Point p2, String label, boolean highlight) {
        int dx = p2.x - p1.x;
        int dy = p2.y - p1.y;
        double dist = Math.hypot(dx, dy);
        if (dist < 1) {
            // self-loop
            int r = 30;
            g2.setColor(highlight ? new Color(230, 57, 70) : new Color(160, 174, 192));
            g2.drawArc(p1.x - r, p1.y - r - 20, 2 * r, 2 * r, 200, 220);
            g2.drawString(label, p1.x - r, p1.y - r - 24);
            return;
        }
        double ux = dx / dist;
        double uy = dy / dist;
        int startOffset = 28;
        int endOffset = 28;
        int x1 = (int) (p1.x + ux * startOffset);
        int y1 = (int) (p1.y + uy * startOffset);
        int x2 = (int) (p2.x - ux * endOffset);
        int y2 = (int) (p2.y - uy * endOffset);
        g2.setStroke(new BasicStroke(1.8f));
        g2.setColor(highlight ? new Color(230, 57, 70) : new Color(160, 174, 192));
        g2.drawLine(x1, y1, x2, y2);
        // arrow head
        int ah = 8;
        int aw = 6;
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int xh1 = (int) (x2 - ah * Math.cos(angle) + aw * Math.sin(angle));
        int yh1 = (int) (y2 - ah * Math.sin(angle) - aw * Math.cos(angle));
        int xh2 = (int) (x2 - ah * Math.cos(angle) - aw * Math.sin(angle));
        int yh2 = (int) (y2 - ah * Math.sin(angle) + aw * Math.cos(angle));
        g2.fillPolygon(new int[]{x2, xh1, xh2}, new int[]{y2, yh1, yh2}, 3);
        // label
        int lx = (x1 + x2) / 2;
        int ly = (y1 + y2) / 2;
        g2.setColor(new Color(33, 37, 41));
        g2.drawString(label, lx + 4, ly - 4);
    }
}
