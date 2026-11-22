# ADR-0003: Gefängnis aus BlockWatch kopiert statt Abhängigkeit

- **Status:** angenommen
- **Datum:** 2026-11-21
- **Version:** 0.1.0

## Kontext

BlockWatch hat ein im Spiel bewährtes Gefängnis (Zelle, Abenteuermodus, keine Befehle/Teleports,
übersteht Logout und Neustart). Gewünscht war dasselbe Verhalten „wie beim JavaScript-Workshop“.
Correctness soll aber auch auf Servern ohne BlockWatch laufen.

## Entscheidung

`JailManager` wird kopiert und angepasst (eigene `jail.yml`, Schlüssel `jail.*` in der config,
eigene Rechte und Texte). Die automatisch gebaute Zelle liegt 6 Blöcke neben dem Spawn, damit sie
nicht in der BlockWatch-Zelle landet.

## Alternativen

- Abhängigkeit von BlockWatch (API): Correctness ginge nur mit BlockWatch.
- Gemeinsame Bibliothek (shaded): sauber, aber mehr Bauaufwand für zwei kleine Plugins.

## Folgen

- Beide Plugins unabhängig installierbar.
- Fehlerkorrekturen müssen in beiden Repos gemacht werden.
- Die beiden Gefängnisse wissen nichts voneinander – Problem bei Doppel-Einsperrung auf einem
  Server ([Risiken](../11-risiken.md)). Wenn beide zusammen eingesetzt werden sollen, diese
  Entscheidung überdenken (gemeinsame Bibliothek oder gegenseitige Abfrage).
