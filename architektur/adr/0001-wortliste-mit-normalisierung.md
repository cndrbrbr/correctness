# ADR-0001: Wortliste mit Normalisierung statt regulärer Ausdrücke oder KI

- **Status:** angenommen
- **Datum:** 2026-11-21
- **Version:** 0.1.0

## Kontext

Kinder umgehen einfache Filter sofort: `4rsch`, `a.r.s.c.h`, `a r s c h`, `aaarsch`, `Scheiße` vs.
`scheisse`. Gleichzeitig dürfen harmlose Wörter („Marsch“, „Mongolei“) nicht bestraft werden. Admins
ohne Programmierkenntnisse sollen die Liste pflegen können.

## Entscheidung

Eine einfache YAML-Wortliste (`woerter`, `ganze-woerter`, `ausnahmen`). Text und Liste werden mit
derselben Normalisierung vereinheitlicht ([8.1](../08-konzepte.md#81-normalisierung)); verglichen
wird danach als Teilwort bzw. ganzes Wort. Die Logik ist Bukkit-frei und unit-getestet.

## Alternativen

- Reguläre Ausdrücke in der Liste: mächtig, aber für Admins fehleranfällig und schwer zu testen.
- KI-/Online-Moderationsdienst: erkennt auch Kontext, aber Kosten, Latenz, Datenschutz (Texte von
  Minderjährigen an Dritte), Abhängigkeit vom Netz.
- Fertiges Chatfilter-Plugin: meist englisch, Normalisierung für deutsche Umlaute/Leetspeak fehlt
  oder ist nicht anpassbar.

## Folgen

- Liste leicht zu pflegen, `/cn test` zeigt sofort das Ergebnis.
- Doppelbuchstaben in der Liste sind egal; Wiederholungen werden zusammengefasst.
- Grenzen: kein Kontext, über Nachrichten verteilte Wörter und Unicode-Doppelgänger werden nicht erkannt.
