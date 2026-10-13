package com.travelsync.service;

import com.travelsync.model.Destination;
import com.travelsync.model.Hotel;
import com.travelsync.strings.AhoCorasick;
import com.travelsync.strings.KMP;
import com.travelsync.suffix.LCPKasai;
import com.travelsync.suffix.SuffixArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SearchService {

    public static final List<String> THEMES = Arrays.asList(
            "beach", "snow", "mountain", "temple", "luxury", "seafood", "nightlife",
            "palace", "adventure", "history", "greenery", "museum", "garden", "sport",
            "valley", "desert", "shopping", "water", "resort", "train");

    public static class Match {
        public final Destination destination;
        public final List<String> matched;
        public final int score;

        Match(Destination destination, List<String> matched) {
            this.destination = destination;
            this.matched = matched;
            this.score = matched.size();
        }
    }

    public static class Similar {
        public final Destination destination;
        public final String commonPhrase;

        Similar(Destination destination, String commonPhrase) {
            this.destination = destination;
            this.commonPhrase = commonPhrase;
        }
    }

    private final List<Destination> destinations;
    private final List<Hotel> hotels;
    private final AhoCorasick matcher = new AhoCorasick(THEMES);
    private final SuffixArray hotelIndex;
    private final int[] starts;

    public SearchService(List<Destination> destinations, List<Hotel> hotels) {
        this.destinations = destinations;
        this.hotels = hotels;

        // one searchable text for all hotels, records separated by '|'
        StringBuilder sb = new StringBuilder();
        starts = new int[hotels.size()];
        for (int i = 0; i < hotels.size(); i++) {
            Hotel h = hotels.get(i);
            starts[i] = sb.length();
            sb.append((h.getName() + " " + h.getCity() + " " + h.getDescription()).toLowerCase());
            if (i < hotels.size() - 1) sb.append('|');
        }
        hotelIndex = new SuffixArray(sb.toString());   // Suffix Array index
    }

    // Aho-Corasick: which travel themes appear in a piece of text
    public Set<String> highlights(String text) {
        return matcher.distinctKeywords(text);
    }

    // Smart trip finder: find themes in the user's sentence, score every destination
    public List<Match> recommend(String query) {
        Set<String> wanted = matcher.distinctKeywords(query);
        List<Match> result = new ArrayList<>();
        for (Destination d : destinations) {
            String text = (d.getName() + " " + d.getDescription() + " "
                    + String.join(" ", d.getAttractions())).toLowerCase();
            List<String> matched = new ArrayList<>();
            for (String k : wanted) {
                if (!KMP.search(text, k).isEmpty()) matched.add(k);
            }
            if (!matched.isEmpty()) result.add(new Match(d, matched));
        }
        result.sort((a, b) -> b.score - a.score);   // stable: ties keep file order
        return result;
    }

    // LCP / Kasai: destinations whose descriptions share the longest common phrase
    public List<Similar> similarTo(Destination dest, int limit) {
        List<Similar> list = new ArrayList<>();
        for (Destination other : destinations) {
            if (other == dest) continue;
            String phrase = LCPKasai.longestCommonSubstring(dest.getDescription(), other.getDescription());
            if (phrase.length() >= 8) list.add(new Similar(other, phrase));
        }
        list.sort((a, b) -> b.commonPhrase.length() - a.commonPhrase.length());
        return list.subList(0, Math.min(limit, list.size()));
    }

    // Suffix Array: substring search over hotel name, city and description
    public List<Hotel> searchHotels(String query) {
        List<Hotel> result = new ArrayList<>();
        String q = query.toLowerCase().trim();
        if (q.isEmpty() || q.contains("|")) return result;

        Set<Integer> ids = new TreeSet<>();
        for (int pos : hotelIndex.search(q)) ids.add(hotelAt(pos));
        for (int id : ids) result.add(hotels.get(id));
        return result;
    }

    private int hotelAt(int pos) {
        for (int i = starts.length - 1; i >= 0; i--) {
            if (starts[i] <= pos) return i;
        }
        return 0;
    }
}