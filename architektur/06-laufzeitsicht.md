# 6. Laufzeitsicht

## 6.1 Schimpfwort im Chat (zweiter Verstoß)

```mermaid
sequenceDiagram
    participant Pl as Spieler
    participant C as ChatListener (Chat-Thread, LOWEST)
    participant F as WordFilter
    participant S as Server-Thread
    participant P as Punisher
    participant J as JailManager
    Pl->>C: AsyncPlayerChatEvent "du 4rsch"
    C->>F: find(text)
    F-->>C: "arsch"
    C->>C: Event abbrechen (Nachricht geht nicht raus)
    C->>S: runTask(...)
    S->>Pl: "Deine Nachricht wurde nicht gesendet."
    S->>P: punish(spieler, CHAT, "arsch", text)
    P->>P: Verstoß zählen → 2 (innerhalb 24 h)<br/>offenses.yml, verstoesse.log
    P->>J: jail(spieler, 3 min)
    J->>Pl: Zelle, Abenteuermodus, Countdown
    P-->>S: Hinweis an Admins online
```

Beim **ersten** Verstoß gibt es statt Gefängnis eine Verwarnung mit Ankündigung der nächsten Strafe.

## 6.2 Andere Quellen

| Quelle | Wann geprüft | Was blockiert wird |
|---|---|---|
| Chat-Befehl | `PlayerCommandPreprocessEvent`; nur Befehle aus `befehle`, Text ab dem n-ten Argument, auch mit Präfix (`/minecraft:msg`) | Befehl wird nicht ausgeführt |
| Schild | `SignChangeEvent`, alle Zeilen zusammen | Schild bleibt unbeschriftet |
| Buch | `PlayerEditBookEvent`, Titel und Seiten | Buch wird nicht gespeichert |
| Amboss | Klick auf den Ergebnis-Slot – nicht schon beim Tippen | Ergebnis nicht herausgegeben; Inventar wird im nächsten Tick geschlossen, dann bestraft |

Alle synchronen Quellen bestrafen direkt; Chat und Amboss über `runTask`, weil Teleports dort
nicht erlaubt sind (Async-Thread bzw. mitten im Inventar-Event).
