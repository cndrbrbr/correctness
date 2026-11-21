# Correctness

Spigot-Plugin (Minecraft 26.3) zur Chat-Kontrolle: Beleidigungen und Schimpfwörter werden
**geblockt** und mit **Verwarnung und Gefängnis** bestraft. Das Gefängnis funktioniert wie
bei BlockWatch auf dem JavaScript-Workshop-Server.

## Was passiert bei einem Schimpfwort?

1. Die Nachricht wird **nicht gesendet** (Schild nicht beschriftet, Buch nicht gespeichert,
   Amboss-Ergebnis nicht herausgegeben).
2. Der Verstoß wird gezählt. Die Strafe steigt (Standard): **1. Mal Verwarnung**, 2. Mal 3 Min.,
   3. Mal 10 Min., danach jedes Mal 30 Min. Gefängnis. Verstöße verfallen nach 24 Stunden.
3. Spieler mit `correctness.admin` sehen eine Meldung, alles steht im Server-Log und in
   `plugins/Correctness/verstoesse.log` (Zeit, Spieler, Quelle, Wort, Strafe, Originaltext).

Geprüft werden: **Chat**, **Chat-Befehle** (`/msg`, `/tell`, `/w`, `/me`, `/say`, `/r`, `/teammsg`, …),
**Schilder**, **Bücher** (Titel und Seiten) und **Amboss-Namen**. Jede Quelle lässt sich in der
`config.yml` abschalten. OPs (`correctness.bypass`) werden nicht gefiltert.

## Gefängnis

- Eine Grundgesteinszelle (innen 3×3×3, Seelaterne) wird beim ersten Bedarf hoch über dem Spawn der
  Hauptwelt gebaut – 6 Blöcke neben der BlockWatch-Zelle, damit sich beide Plugins nicht stören.
  Verschieben mit `/correctness setjail`.
- Insassen sind im Abenteuermodus, dürfen keine Befehle benutzen und werden nicht wegteleportiert.
  Restzeit steht in der Actionbar. Chatten dürfen sie (gefiltert wie alle anderen).
- Der Zustand steht in `jail.yml` und überlebt Logout, Tod und Neustart; die Zeit läuft auch offline.
  Danach geht es zurück an den alten Ort im alten Spielmodus.

## Befehle (`/correctness` oder `/cn`, Permission `correctness.admin`, Standard: OP)

| Befehl | Wirkung |
|---|---|
| `/cn test <text>` | Zeigt, ob und welches Wort gefiltert würde |
| `/cn jail <spieler> [minuten]` | Spieler einsperren (Standard `jail-minuten`) |
| `/cn frei <spieler>` | Vorzeitig freilassen (offline: beim nächsten Login) |
| `/cn setjail` | Gefängniszelle an die eigene Position setzen |
| `/cn verstoesse <spieler>` | Zählende Verstöße und nächste Strafe |
| `/cn vergeben <spieler>` | Verstöße löschen |
| `/cn reload` | `config.yml` und `words.yml` neu laden |

## Wortliste (`plugins/Correctness/words.yml`)

- `woerter`: Treffer auch innerhalb eines Wortes („hure“ trifft „Hurensohn“).
- `ganze-woerter`: nur als ganzes Wort (kurze Wörter wie „mongo“, die sonst in „Mongolei“ stecken).
- `ausnahmen`: harmlose Wörter, die ein Schimpfwort enthalten („Marsch“, „Barsch“, „Arschbombe“).

Automatisch erkannt werden Groß/Klein, Umlaute (ä=ae, ß=ss), Leetspeak (`4rsch`, `1d10t`, `Schei$$e`),
Zeichen im Wort (`a.r.s.c.h`), einzeln getippte Buchstaben (`a r s c h`), wiederholte Buchstaben
(`aaarsch`) und Farbcodes. Nach Änderungen `/cn reload`, prüfen mit `/cn test`.

## Konfiguration (`config.yml`)

```yaml
pruefen: {chat: true, befehle: true, schilder: true, buecher: true, amboss: true}
befehle: {msg: 1, tell: 1, me: 0, ...}   # Befehl: Anzahl Argumente vor dem Text
strafe:
  stufen: [0, 3, 10, 30]                 # Minuten je Verstoß, 0 = Verwarnung
  verfall-stunden: 24                    # 0 = nie vergessen
jail-minuten: 5                          # Standard für /cn jail
jail: {world: ..., x: ..., y: ..., z: ...}   # wird automatisch gesetzt
```

## Bauen

```
docker run --rm -v $PWD:/src -v ~/.m2:/root/.m2 -w /src maven:3.9-eclipse-temurin-25 mvn -q -B package
```

Ergebnis: `target/correctness-<version>.jar` → in `plugins/` des Servers legen.
