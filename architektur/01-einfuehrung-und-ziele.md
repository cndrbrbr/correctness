# 1. Einführung und Ziele

## 1.1 Aufgabe

Correctness hält auf Kinder- und Jugendservern den Ton sauber: Beleidigungen und Schimpfwörter in
**Chat, Chat-Befehlen, Schildern, Büchern und Amboss-Namen** werden blockiert, gezählt und mit
steigender Strafe belegt – erst Verwarnung, dann Gefängnis (Standard 0 / 3 / 10 / 30 Minuten,
Verstöße verfallen nach 24 Stunden). Admins werden benachrichtigt, alles wird protokolliert.

Entstanden im November 2026; das Gefängnis ist aus [BlockWatch](https://github.com/cndrbrbr/BlockWatch)
übernommen, weil es sich dort bewährt hat ([ADR-0003](adr/0003-gefaengnis-kopiert-statt-abhaengigkeit.md)).

**Kein Ziel:** Moderation von Sprache außerhalb Minecraft (Discord), Erkennung von Bedeutung oder
Kontext (Sarkasmus, Mobbing ohne Schimpfwort), Spam- oder Werbefilter.

## 1.2 Qualitätsziele

| Prio | Ziel | Woran man es merkt |
|---|---|---|
| 1 | **Verschleierung erkennen** | Leetspeak, Umlaute, Satzzeichen, Leerzeichen zwischen Buchstaben, Wiederholungen werden normalisiert; unit-getestet |
| 2 | **Wenig Fehlalarme** | Ausnahmenliste („Marsch“, „Barsch“), kurze Wörter nur als ganzes Wort; Test mit harmlosen Sätzen |
| 3 | **Fair und nachvollziehbar** | Erst Verwarnung, Strafen steigen, Verstöße verfallen; Admins sehen jeden Fall, `verstoesse.log` |
| 4 | **Ohne Admin vor Ort wirksam** | Strafe automatisch, Gefängnis überlebt Logout/Neustart |
| 5 | **Einfach anpassbar** | Wortliste als YAML, `/cn reload`, `/cn test <text>` |

## 1.3 Beteiligte

| Rolle | Erwartung |
|---|---|
| Spieler (Kinder/Jugendliche) | Klare Meldung, was passiert ist; Chance durch Verwarnung |
| Admins/OPs | Wenig Aufwand, Fälle sehen, Liste pflegen, vergeben können |
| Serverbetreiber | Sauberer Umgangston auf dem Server |

## 1.4 Randbedingungen

- Spigot 26.3, Java 25, Maven; Bau im Docker-Image `maven:3.9-eclipse-temurin-25`.
- Keine fremden Bibliotheken, keine Datenbank (nur YAML und eine Logdatei).
- Texte deutsch; die Wortliste enthält deutsche und englische Wörter.
