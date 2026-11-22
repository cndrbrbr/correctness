# 12. Glossar

| Begriff | Bedeutung | Im Code |
|---|---|---|
| Quelle | Woher der Text kam: Chat, Befehl, Schild, Buch, Amboss | `Punisher.Source`, `pruefen.*` |
| Wortliste | `woerter` (Teilwort), `ganze-woerter`, `ausnahmen` | `words.yml`, `WordFilter` |
| Normalisierung | Text vereinheitlichen, um Verschleierung zu erkennen | `WordFilter.normalizeWord` |
| Ausnahme | Harmloses Wort, das ein Schimpfwort enthält („Marsch“) | `ausnahmen` |
| Verstoß | Ein gefundenes Schimpfwort eines Spielers, mit Zeitpunkt | `OffenseTracker`, `offenses.yml` |
| Verfall | Nach `strafe.verfall-stunden` zählt ein Verstoß nicht mehr | `OffenseTracker.prune` |
| Strafstufe | Minuten Gefängnis für den n-ten zählenden Verstoß, 0 = Verwarnung | `strafe.stufen`, `minutesFor` |
| Verwarnung | Strafe ohne Gefängnis, kündigt die nächste Stufe an | `Punisher.punish` |
| Gefängnis / Insasse | Wie bei BlockWatch: Zelle, Abenteuermodus, keine Befehle | `JailManager`, `jail.yml` |
| Vergeben | Alle Verstöße eines Spielers löschen | `/cn vergeben` |
| Bypass | `correctness.bypass`: wird nicht geprüft | – |
