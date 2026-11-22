# ADR-0004: YAML-Dateien statt Datenbank

- **Status:** angenommen
- **Datum:** 2026-11-21
- **Version:** 0.1.0

## Kontext

Correctness speichert wenig: zählende Verstöße (meist leer, Verfall 24 h), aktuelle Insassen und ein
Protokoll. Admins sollen die Daten ohne Werkzeuge lesen können.

## Entscheidung

`offenses.yml` und `jail.yml` werden bei jeder Änderung komplett neu geschrieben, `verstoesse.log` ist
eine Textdatei, an die angehängt wird. Keine Datenbank.

## Alternativen

- SQLite wie BlockWatch: unnötig bei so wenigen Daten.

## Folgen

- Einfach, mit jedem Editor lesbar, `grep` auf das Log.
- Keine Auswertungen über lange Zeiträume; das Log hat keine Aufbewahrungsfrist (siehe Schulden).
