package io.github.cndrbrbr.correctness.filter;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordFilterTest {

    private final WordFilter filter = new WordFilter(
            List.of("arsch", "hure", "scheisse", "fuck", "idiot"),
            List.of("mongo", "huso"),
            List.of("marsch", "barsch", "arschbombe"));

    private void hit(String text, String expected) {
        assertEquals(Optional.of(expected), filter.find(text), text);
    }

    private void clean(String text) {
        assertTrue(filter.find(text).isEmpty(), text);
    }

    @Test
    void findsPlainWords() {
        hit("du arsch", "arsch");
        hit("Du ARSCH!", "arsch");
        hit("Arschloch", "arsch");
        hit("hurensohn", "hure");
        hit("what the fuck", "fuck");
    }

    @Test
    void findsDisguisedWords() {
        hit("4rsch", "arsch");
        hit("a.r.s.c.h", "arsch");
        hit("a r s c h", "arsch");
        hit("aaarrrsch", "arsch");
        hit("1d10t", "idiot");
        hit("§cArsch", "arsch");
        hit("f_u_c_k", "fuck");
        hit("Scheiße", "scheisse");
        hit("SCHEISSE", "scheisse");
        hit("scheise", "scheisse");
        hit("Schei$$e", "scheisse");
    }

    @Test
    void wholeWordsOnlyAsWholeWords() {
        hit("du mongo", "mongo");
        hit("Mongo!", "mongo");
        clean("die Mongolei ist groß");
        hit("huso", "huso");
        clean("Husos? nein, Husky");
    }

    @Test
    void exceptionsAreAllowed() {
        clean("Wir marschieren los");
        clean("Ein Barsch im Teich");
        clean("Arschbombe ins Wasser!");
        hit("Marschhure", "hure");
    }

    @Test
    void harmlessTextIsAllowed() {
        clean("Hallo, wie geht es dir?");
        clean("Ich baue ein Haus mit 4 Türmen und 10 Fenstern.");
        clean("");
        clean(null);
        clean("war schön heute");
    }
}
