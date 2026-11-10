package com.scc.ui;

import java.util.ArrayList;
import java.util.List;

// Prints clean, bordered console tables. Long text wraps inside its column,
// so every row stays aligned no matter how long the data is.
public final class TablePrinter {

    private TablePrinter() {
    }

    public static void print(String[] headers, int[] widths,
                             List<String[]> rows) {
        print(headers, widths, rows, new boolean[headers.length]);
    }

    public static void print(String[] headers, int[] widths,
                             List<String[]> rows, boolean[] rightAlign) {

        String border = border(widths);

        System.out.println(border);
        System.out.println(formatLine(headers, widths, rightAlign));
        System.out.println(border);

        for (String[] row : rows) {
            List<List<String>> cells = new ArrayList<>();
            int height = 1;

            for (int i = 0; i < widths.length; i++) {
                String value = i < row.length ? row[i] : "";
                List<String> lines = wrap(value, widths[i]);
                cells.add(lines);
                height = Math.max(height, lines.size());
            }

            for (int line = 0; line < height; line++) {
                String[] parts = new String[widths.length];
                for (int i = 0; i < widths.length; i++) {
                    parts[i] = line < cells.get(i).size()
                            ? cells.get(i).get(line) : "";
                }
                System.out.println(formatLine(parts, widths, rightAlign));
            }
        }

        System.out.println(border);
    }

    private static String border(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append("-".repeat(w + 2)).append("+");
        }
        return sb.toString();
    }

    private static String formatLine(String[] cells, int[] widths,
                                     boolean[] rightAlign) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            String format = rightAlign[i]
                    ? "%" + widths[i] + "s"
                    : "%-" + widths[i] + "s";
            sb.append(" ").append(String.format(format, cells[i]))
                    .append(" |");
        }
        return sb.toString();
    }

    private static List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        String remaining = text == null
                ? "" : text.replaceAll("[\\r\\n]+", " ").trim();

        while (remaining.length() > width) {
            int cut = remaining.lastIndexOf(' ', width);
            if (cut <= 0) {
                cut = width;
            }
            lines.add(remaining.substring(0, cut).trim());
            remaining = remaining.substring(cut).trim();
        }

        lines.add(remaining);
        return lines;
    }
}