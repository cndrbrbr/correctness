# 4. Lösungsstrategie

| Ziel | Ansatz | Details |
|---|---|---|
| Verschleierung erkennen, wenig Fehlalarme | Text und Liste gleich normalisieren, Teilwort- und Ganzwort-Liste plus Ausnahmen | [ADR-0001](adr/0001-wortliste-mit-normalisierung.md), [8.1](08-konzepte.md#81-normalisierung) |
| Fair | Steigende Strafe mit Verfall statt fester Strafe | [ADR-0002](adr/0002-steigende-strafe-mit-verfall.md) |
| Ohne Admin wirksam | Gefängnis aus BlockWatch übernommen | [ADR-0003](adr/0003-gefaengnis-kopiert-statt-abhaengigkeit.md) |
| Kein Lag, kein Thread-Chaos | Filter unveränderlich und `volatile` (auch im Async-Chat lesbar); Strafe immer auf dem Server-Thread | [8.2](08-konzepte.md#82-threads) |
| Testbar | Filter und Verstoßzählung ohne Bukkit (`WordFilter`, `OffenseTracker`) | [5](05-bausteinsicht.md) |
| Schnell zu warten | Nur YAML-Dateien, `/cn reload` lädt Liste und Strafstufen neu | [ADR-0004](adr/0004-yaml-statt-datenbank.md) |
