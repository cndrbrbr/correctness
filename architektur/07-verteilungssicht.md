# 7. Verteilungssicht

## 7.1 Dateien

Eine Jar `correctness-<version>.jar` in `plugins/`, Server neu starten.

| Datei in `plugins/Correctness/` | Inhalt | Geschrieben von |
|---|---|---|
| `config.yml` | Quellen, Befehle, Strafstufen, Verfall, Zellenposition | Admin; Plugin (Defaults ergänzen, `setjail`) |
| `words.yml` | Wortliste, wird nur beim ersten Start angelegt, danach nie überschrieben | Admin |
| `offenses.yml` | Zählende Verstöße je UUID (Zeitstempel) | Plugin, bei jedem Verstoß |
| `jail.yml` | Aktuelle Insassen | Plugin |
| `verstoesse.log` | Eine Zeile je Verstoß, mit Originaltext | Plugin, nur anhängen |

## 7.2 Im Einsatz (Stand 2026-11)

| Server | Installiert über | Bemerkung |
|---|---|---|
| meckminecraft mc2 (Inigresso) | von Hand | seit 2026-11-21; im Spiel mit echten Spielern noch kaum erprobt |

Noch kein GitHub-Release und kein Eintrag im Plugin-Katalog.

## 7.3 Bau und Test

| Schritt | Wie |
|---|---|
| Bauen + Tests | `docker run … maven:3.9-eclipse-temurin-25 mvn -q -B package` (siehe README) |
| Wortliste prüfen | `/cn test <text>` im Spiel oder über die Konsole |
| Server-Test | Isolierter Spigot 26.3: Laden und `correctness test` geprüft |
