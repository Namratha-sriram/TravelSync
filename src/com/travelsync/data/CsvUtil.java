package com.travelsync.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

// Small CSV helper: quoted fields, commas inside quotes, "" escapes, header row, UTF-8 (BOM safe)
public class CsvUtil {

    public static List<String> parseLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out;
    }

    public static String escape(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    public static String join(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }

    // Reads all data rows (the first non-empty line is the header and is skipped)
    public static List<String[]> read(String path) {
        List<String[]> rows = new ArrayList<>();
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            System.out.println("File not found: " + path);
            return rows;
        }
        try {
            boolean headerSkipped = false;
            for (String line : Files.readAllLines(p, StandardCharsets.UTF_8)) {
                line = line.replace("\uFEFF", "");          // Excel may add a BOM
                if (line.trim().isEmpty()) continue;
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                List<String> f = parseLine(line);
                String[] arr = new String[f.size()];
                for (int i = 0; i < arr.length; i++) arr[i] = f.get(i).trim();
                rows.add(arr);
            }
        } catch (IOException e) {
            System.out.println("Could not read file: " + path);
        }
        return rows;
    }

    // Overwrites the file with a header and the given rows
    public static void write(String path, String header, List<String> rows) {
        try {
            Path p = Paths.get(path);
            if (p.getParent() != null) Files.createDirectories(p.getParent());
            List<String> lines = new ArrayList<>();
            lines.add(header);
            lines.addAll(rows);
            Files.write(p, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Could not write file: " + path);
        }
    }

    // Appends one row; creates the file (and folders) with a header if needed
    public static void append(String path, String header, String row) {
        try {
            Path p = Paths.get(path);
            if (p.getParent() != null) Files.createDirectories(p.getParent());
            StringBuilder sb = new StringBuilder();
            if (!Files.exists(p) || Files.size(p) == 0) {
                sb.append(header).append(System.lineSeparator());
            } else {
                byte[] all = Files.readAllBytes(p);
                if (all[all.length - 1] != '\n') sb.append(System.lineSeparator());
            }
            sb.append(row).append(System.lineSeparator());
            Files.write(p, sb.toString().getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Could not write file: " + path);
        }
    }
}