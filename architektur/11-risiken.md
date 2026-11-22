# 11. Risiken und technische Schulden

## 11.1 Risiken

| Risiko | Folge | Gegenmaßnahme / Stand |
|---|---|---|
| Correctness und BlockWatch auf demselben Server, Spieler sitzt schon im Gefängnis des anderen Plugins | Bis 0.1.1: Teleports blockierten sich gegenseitig, Spieler endete in einer Zelle im Abenteuermodus | **Behoben in 0.1.1 / BlockWatch 0.3.2:** gegenseitige Markierung, kein Doppel-Einsperren ([ADR-0005](adr/0005-gefaengnisse-markieren-sich-gegenseitig.md)). Wirkt nur, wenn beide Plugins aktuell sind; im Spiel mit zwei Plugins noch nicht getestet |
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
