# 8. Querschnittliche Konzepte

## 8.1 Normalisierung

Text **und** Wortliste laufen durch dieselbe Funktion `WordFilter.normalizeWord`:

1. Farbcodes (`§x`, `&x`) entfernen, Kleinschreibung
2. Umlaute → `ae/oe/ue/ss`, Akzente entfernen (NFD)
3. Satzzeichen am Wortrand abschneiden („Spast!“ ≠ „spasti“)
4. Leetspeak zurück: `4 @→a`, `3→e`, `1 !→i`, `0→o`, `5 $→s`, `7→t`
5. Alles außer Buchstaben entfernen (`a.r.s.c.h` → `arsch`)
6. Gleiche Buchstaben hintereinander zusammenfassen (`aaarsch` → `arsch`, `scheisse` = `scheise`)

Danach wird der Text in Wörter zerlegt; Folgen von mindestens zwei einzelnen Buchstaben
(`a r s c h`) werden zu einem Wort zusammengezogen. Geprüft wird je Wort:

- **`ganze-woerter`**: exakter Vergleich (kurze Wörter wie „mongo“, sonst „Mongolei“).
- **`woerter`**: Teilwort; ein Treffer zählt nicht, wenn er komplett in einer **Ausnahme** liegt
  („arsch“ in „marsch“). Lange Ausnahmen werden zuerst geprüft.

Gemeldet wird das erste gefundene Wort so, wie es in der Liste steht.

## 8.2 Threads

| Was | Thread |
|---|---|
| Chat-Prüfung | Async-Chat-Thread; liest nur den unveränderlichen `WordFilter` (Feld `volatile`, bei `reload` ersetzt) |
| Strafe, Teleport, Dateien | Immer Server-Thread (`Punisher` ist nicht thread-sicher) |
| Gefängnis-Takt | Server-Thread, jede Sekunde |

## 8.3 Event-Prioritäten

Alle Prüfungen auf `LOWEST` mit `ignoreCancelled`: möglichst früh abbrechen, bevor andere Plugins
(Chat-Formatierung, Discord-Brücken) die Nachricht weiterreichen. Spieler mit `correctness.bypass`
(Standard: OP) werden gar nicht geprüft – zum Testen deshalb `/cn test`.

## 8.4 Speicherung

Nur YAML und eine Logdatei ([ADR-0004](adr/0004-yaml-statt-datenbank.md)). `offenses.yml` wird bei
jedem Verstoß komplett neu geschrieben und enthält nur noch zählende Verstöße (verfallene werden
beim Speichern entfernt). `config.yml` wird beim Start mit neuen Standardwerten ergänzt; `words.yml`
nie überschrieben, damit eigene Anpassungen bleiben.

## 8.5 Datenschutz

`verstoesse.log` enthält Spielernamen und den **vollständigen Originaltext** (auch Bücher und
private Nachrichten über `/msg`) und wird nie gekürzt. Auf Servern mit Minderjährigen ist das
personenbezogen und sensibel: Zugriff nur für Admins, regelmäßig aufräumen
([Risiken](11-risiken.md)).

## 8.6 Konfiguration

`/cn reload` liest `config.yml` und `words.yml` neu und setzt Strafstufen/Verfall neu. Die Liste der
geprüften Quellen und Befehle wird bei jedem Event aus der Konfiguration gelesen und gilt sofort.
