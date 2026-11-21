package io.github.cndrbrbr.correctness.filter;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Die mitgelieferte words.yml: trifft typische Beleidigungen, aber keine harmlosen Saetze. */
class DefaultWordsTest {

    private static WordFilter load() {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(
                DefaultWordsTest.class.getResourceAsStream("/words.yml"), StandardCharsets.UTF_8));
        return new WordFilter(yaml.getStringList("woerter"), yaml.getStringList("ganze-woerter"),
                yaml.getStringList("ausnahmen"));
    }

    private final WordFilter filter = load();

    @Test
    void catchesInsults() {
        for (String text : new String[]{"du arschloch", "Hurensohn!", "halt die fresse", "du bist so behindert",
                "Fick dich", "scheiße", "voll der Spast", "du missgeburt", "fuck off", "you bitch", "Wichser",
                "du Idiot", "a r s c h", "H u r e n s o h n"}) {
            assertTrue(filter.find(text).isPresent(), text);
        }
    }

    @Test
    void allowsHarmlessSentences() {
        for (String text : new String[]{"Wir marschieren zum Schloss", "Ich habe einen Barsch geangelt",
                "Die Mongolei ist groß", "Wer hat Lust auf Spaß?", "Kann mir jemand Redstone geben?",
                "war schön heute", "Ich mache eine Arschbombe in den See", "Warschau ist in Polen",
                "Das Essen war harsch gewürzt", "Wo ist der Teppich?", "Ich bin müde, gute Nacht",
                "lol 1 2 3 los", "Hast du Diamanten?", "Die Behinderung ist weg", "Schuhregal bauen",
                "Ich spiele Klavier", "Der Hund frisst sein Fressen", "Shiitake-Pilze", "GG, nice!"}) {
            assertTrue(filter.find(text).isEmpty(), text + " -> " + filter.find(text).orElse(""));
        }
    }
}
