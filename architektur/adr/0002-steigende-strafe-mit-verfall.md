# ADR-0002: Steigende Strafe mit Verfall, erste Stufe Verwarnung

- **Status:** angenommen
- **Datum:** 2026-11-21
- **Version:** 0.1.0

## Kontext

Ein einzelnes Wort kann ein Ausrutscher oder ein Fehlalarm sein; wiederholtes Beleidigen soll spürbar
werden. Strafen sollen nicht ewig nachwirken.

## Entscheidung

Jeder Verstoß wird mit Zeitpunkt gespeichert. Die Strafe richtet sich nach der Zahl der Verstöße
innerhalb von `strafe.verfall-stunden` (24): `strafe.stufen` = `[0, 3, 10, 30]` Minuten, 0 =
Verwarnung, die letzte Stufe gilt für alle weiteren. Die Nachricht wird in jedem Fall blockiert.
Admins können mit `/cn vergeben` zurücksetzen.

## Alternativen

- Feste Strafe je Verstoß: zu hart für den ersten Fehlalarm, zu weich für Wiederholer.
- Kick/Bann: zu hart für Kinder, braucht Admin zum Entsperren.
- Nur blockieren: keine Wirkung.

## Folgen

- Erster Fehlalarm kostet nur eine Verwarnung.
- Zustand muss gespeichert werden (`offenses.yml`), damit Ausloggen nicht hilft.
