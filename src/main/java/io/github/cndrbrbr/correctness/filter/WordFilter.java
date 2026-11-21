package io.github.cndrbrbr.correctness.filter;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Findet Schimpfwoerter in einem Text. Reine Logik ohne Bukkit.
 *
 * <p>Text und Wortlisten werden gleich normalisiert: Kleinschreibung, Farbcodes weg, Umlaute
 * (ae/oe/ue/ss) und Akzente vereinheitlicht, Leetspeak (4→a, 3→e, 1→i, 0→o, 5→s, 7→t, @→a, $→s)
 * zurueckgewandelt, alles ausser Buchstaben entfernt ("a.r.s.c.h") und Buchstabenwiederholungen
 * zusammengefasst ("aaarsch"). Gesucht wird pro Wort des Textes; einzeln geschriebene Buchstaben
 * ("a r s c h") werden vorher zusammengezogen.
 *
 * <ul>
 *   <li>{@code woerter}: Treffer auch innerhalb eines Wortes ("Hurensohn" enthaelt "hure").</li>
 *   <li>{@code ganzeWoerter}: Treffer nur, wenn das ganze Wort passt (fuer kurze Woerter).</li>
 *   <li>{@code ausnahmen}: harmlose Woerter, die ein Schimpfwort enthalten ("Marsch", "Barsch").
 *       Sie werden vor der Suche aus dem Wort entfernt.</li>
 * </ul>
 */
public final class WordFilter {

    /** Normalisierte Form → Eintrag, wie er in der Liste steht (fuer Meldungen). */
    private final Map<String, String> words = new LinkedHashMap<>();
    private final Map<String, String> wholeWords = new LinkedHashMap<>();
    private final List<String> exceptions = new ArrayList<>();

    public WordFilter(Collection<String> words, Collection<String> wholeWords, Collection<String> exceptions) {
        for (String word : words) {
            String key = normalizeWord(word);
            if (!key.isEmpty()) {
                this.words.putIfAbsent(key, word);
            }
        }
        for (String word : wholeWords) {
            String key = normalizeWord(word);
            if (!key.isEmpty()) {
                this.wholeWords.putIfAbsent(key, word);
            }
        }
        for (String exception : exceptions) {
            String key = normalizeWord(exception);
            if (!key.isEmpty()) {
                this.exceptions.add(key);
            }
        }
        // Lange Ausnahmen zuerst entfernen, damit kuerzere sie nicht zerschneiden.
        this.exceptions.sort((a, b) -> b.length() - a.length());
    }

    public int size() {
        return words.size() + wholeWords.size();
    }

    /** Das erste gefundene Schimpfwort (so wie es in der Liste steht), sonst leer. */
    public Optional<String> find(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        for (String token : tokens(text)) {
            Optional<String> hit = check(token);
            if (hit.isPresent()) {
                return hit;
            }
        }
        return Optional.empty();
    }

    private Optional<String> check(String token) {
        String whole = wholeWords.get(token);
        if (whole != null) {
            return Optional.of(whole);
        }
        for (Map.Entry<String, String> entry : words.entrySet()) {
            String word = entry.getKey();
            for (int at = token.indexOf(word); at >= 0; at = token.indexOf(word, at + 1)) {
                if (!insideException(token, at, at + word.length())) {
                    return Optional.of(entry.getValue());
                }
            }
        }
        return Optional.empty();
    }

    /** Liegt [start, end) komplett in einer Ausnahme ("arsch" in "marsch")? */
    private boolean insideException(String token, int start, int end) {
        for (String exception : exceptions) {
            int from = Math.max(0, end - exception.length());
            for (int at = token.indexOf(exception, from); at >= 0 && at <= start; at = token.indexOf(exception, at + 1)) {
                if (at + exception.length() >= end) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Normalisierte Woerter des Textes; Folgen einzelner Buchstaben werden zu einem Wort. */
    static List<String> tokens(String text) {
        List<String> result = new ArrayList<>();
        StringBuilder letters = new StringBuilder();
        int singles = 0;
        for (String raw : stripColors(text).split("\\s+")) {
            String token = normalizeWord(raw);
            if (token.isEmpty()) {
                continue;
            }
            if (token.length() == 1) {
                letters.append(token);
                singles++;
                continue;
            }
            if (singles >= 2) {
                result.add(collapse(letters.toString()));
            }
            letters.setLength(0);
            singles = 0;
            result.add(token);
        }
        if (singles >= 2) {
            result.add(collapse(letters.toString()));
        }
        return result;
    }

    /** Ein einzelnes Wort normalisieren (siehe Klassenbeschreibung). */
    static String normalizeWord(String raw) {
        String s = stripColors(raw).toLowerCase(Locale.ROOT)
                .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss");
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        s = trimPunctuation(s);
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = leet(s.charAt(i));
            if (Character.isLetter(c)) {
                out.append(c);
            }
        }
        return collapse(out.toString());
    }

    /** Satzzeichen am Wortrand entfernen, damit "Spast!" nicht als "spasti" gelesen wird. */
    private static String trimPunctuation(String s) {
        int start = 0;
        int end = s.length();
        while (start < end && isEdgeJunk(s.charAt(start))) {
            start++;
        }
        while (end > start && isEdgeJunk(s.charAt(end - 1))) {
            end--;
        }
        return s.substring(start, end);
    }

    private static boolean isEdgeJunk(char c) {
        return !Character.isLetterOrDigit(c) && c != '@' && c != '$';
    }

    private static char leet(char c) {
        return switch (c) {
            case '4', '@' -> 'a';
            case '3' -> 'e';
            case '1', '!' -> 'i';
            case '0' -> 'o';
            case '5', '$' -> 's';
            case '7' -> 't';
            default -> c;
        };
    }

    /** Gleiche Buchstaben hintereinander zu einem zusammenfassen. */
    private static String collapse(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (out.isEmpty() || out.charAt(out.length() - 1) != c) {
                out.append(c);
            }
        }
        return out.toString();
    }

    /** Minecraft-Farbcodes (§x und &x) entfernen. */
    private static String stripColors(String s) {
        return s.replaceAll("(?i)[§&][0-9a-fk-orx]", "");
    }
}
