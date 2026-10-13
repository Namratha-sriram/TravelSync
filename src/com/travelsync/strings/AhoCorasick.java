package com.travelsync.strings;

import java.util.*;

public class AhoCorasick {

    private static class Node {
        Map<Character, Node> next = new HashMap<>();
        Node fail;
        List<String> output = new ArrayList<>();
    }

    private final Node root = new Node();

    public AhoCorasick(List<String> keywords) {
        for (String k : keywords) insert(k.toLowerCase());
        buildFailLinks();
    }

    private void insert(String word) {
        Node cur = root;
        for (char c : word.toCharArray()) {
            cur = cur.next.computeIfAbsent(c, x -> new Node());
        }
        cur.output.add(word);
    }

    // BFS to set failure links
    private void buildFailLinks() {
        Queue<Node> queue = new LinkedList<>();
        for (Node child : root.next.values()) {
            child.fail = root;
            queue.add(child);
        }
        while (!queue.isEmpty()) {
            Node cur = queue.poll();
            for (Map.Entry<Character, Node> e : cur.next.entrySet()) {
                char c = e.getKey();
                Node child = e.getValue();

                Node f = cur.fail;
                while (f != root && !f.next.containsKey(c)) f = f.fail;
                child.fail = f.next.containsKey(c) && f.next.get(c) != child ? f.next.get(c) : root;

                child.output.addAll(child.fail.output);   // inherit matches from suffix links
                queue.add(child);
            }
        }
    }

    // Returns entries like "goa @ 12" (keyword @ start index)
    public List<String> search(String text) {
        List<String> matches = new ArrayList<>();
        text = text.toLowerCase();
        Node cur = root;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            while (cur != root && !cur.next.containsKey(c)) cur = cur.fail;
            if (cur.next.containsKey(c)) cur = cur.next.get(c);
            for (String word : cur.output) {
                matches.add(word + " @ " + (i - word.length() + 1));
            }
        }
        return matches;
    }

    // Distinct keywords found (handy for tagging a description)
    public Set<String> distinctKeywords(String text) {
        Set<String> found = new LinkedHashSet<>();
        for (String m : search(text)) found.add(m.substring(0, m.indexOf(" @ ")));
        return found;
    }
}