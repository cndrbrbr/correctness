# 3. Kontextabgrenzung

```mermaid
flowchart LR
    Spieler([Spieler]) -- "Chat, /msg …, Schild,<br/>Buch, Amboss" --> Spigot
    Spigot[(Spigot 26.3)] -- "Chat-, Befehl-, Schild-,<br/>Buch-, Inventar-Events" --> CN[[Correctness]]
    CN -- "abbrechen, Meldung,<br/>Gefängnis" --> Spigot
    CN -- "Hinweis je Verstoß" --> Admin([Admins online])
    Admin -- "/cn test, jail, frei,<br/>vergeben, reload" --> CN
    CN --> Dateien[(words.yml, config.yml,<br/>offenses.yml, jail.yml,<br/>verstoesse.log)]
    CN -. "Zelle 6 Blöcke daneben" .- BW[BlockWatch-Gefängnis]
```

| Partner | Richtung | Inhalt |
|---|---|---|
| Spigot-Events | ein | `AsyncPlayerChatEvent`, `PlayerCommandPreprocessEvent`, `SignChangeEvent`, `PlayerEditBookEvent`, `InventoryClickEvent` (Amboss-Ergebnis), Join/Respawn/Teleport für das Gefängnis |
| Spigot | aus | Events abbrechen, Teleport, Spielmodus, Zelle bauen |
| Spieler | aus | Meldung „nicht gesendet“, Verwarnung oder Gefängnis mit Countdown |
| Admins | beide | Hinweise je Verstoß (`correctness.admin`), Befehle `/cn …` |
| Dateien | beide | siehe [Verteilungssicht](07-verteilungssicht.md) |
| BlockWatch | – | Keine Code-Abhängigkeit; nur die automatische Zelle liegt bewusst daneben |

Nicht geprüft: Spielernamen, Konsole/RCON, Texte, die andere Plugins senden, Befehle, die nicht in
`befehle` stehen.
