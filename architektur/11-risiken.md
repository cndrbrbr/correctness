# 11. Risiken und technische Schulden

## 11.1 Risiken

| Risiko | Folge | Gegenmaßnahme / Stand |
|---|---|---|
| **Correctness und BlockWatch auf demselben Server, Spieler sitzt in einem Gefängnis und wird vom anderen Plugin auch eingesperrt** | Jedes Plugin blockiert die Teleports des anderen als „fremd“. Das zweite Plugin merkt sich die erste Zelle als „alten Ort“ und *Abenteuer* als Spielmodus. Am Ende wird der Spieler in die erste Zelle „freigelassen“, im Abenteuermodus – aus der Grundgesteinszelle kommt er ohne Admin nicht heraus | Heute keine Überschneidung (Correctness nur auf meck mc2, BlockWatch nicht dort). Vor gemeinsamem Einsatz lösen, z. B. Insassen des anderen Plugins nicht erneut einsperren, oder ein gemeinsames Gefängnis ([ADR-0003](adr/0003-gefaengnis-kopiert-statt-abhaengigkeit.md)) |
| Fehlalarme durch Teilwort-Treffer | Harmlose Nachricht blockiert, Kind wird bestraft | Ausnahmenliste, `ganze-woerter`, `DefaultWordsTest`; Verwarnung als erste Stufe; `/cn vergeben` |
| Umgehung (Wort auf zwei Nachrichten verteilt, andere Schreibweisen, Unicode-Ähnlichkeiten wie kyrillisches „а“) | Nicht erkannt | Liste ergänzen; Admins bekommen weiterhin alles im Chat mit |
| Beleidigung ohne Schimpfwort | Nicht erkannt | Bewusst nicht im Umfang |
| `verstoesse.log` wächst unbegrenzt und enthält Originaltexte Minderjähriger | Datenschutz, Speicher | Noch keine Rotation/Aufbewahrungsfrist (siehe Schulden) |
| Spieler-Events im Spiel kaum erprobt | Unerwartetes Verhalten bei Büchern/Amboss | Nach ersten Fällen `verstoesse.log` und Server-Log prüfen |

## 11.2 Technische Schulden

| Schuld | Wo | Aufwand |
|---|---|---|
| Gefängnis ist eine Kopie aus BlockWatch – Fehler müssen doppelt behoben werden | `jail.JailManager` | mittel (gemeinsame Bibliothek), sobald beide weiterwachsen |
| Keine Aufbewahrungsfrist/Rotation für `verstoesse.log` | `Punisher` | klein |
| `ChatListener` und `Punisher` ohne Tests (nur Filter und Zählung getestet) | `listener`, `punish` | mittel |
| `offenses.yml` wird bei jedem Verstoß synchron komplett geschrieben | `Punisher.save` | klein; erst bei sehr vielen Spielern relevant |
