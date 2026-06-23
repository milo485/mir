# Mir

**Mir** ist ein in Java geschriebenes, freies Content-Management-System der
**Mir-coders group** für das Indymedia-Netzwerk. Es betrieb zahlreiche
Indymedia-Seiten – darunter de.indymedia.org bis Mai 2014 – und basiert auf
Java / Tomcat (Servlets) mit einer PostgreSQL-Datenbank.

> *„dont hate the media, become the media!"*

Mir war eine der prägenden Open-Publishing-Plattformen der frühen
Indymedia-Bewegung: ein offenes Veröffentlichungssystem für freie, alternative
und unabhängige Medien, mit dem Leute ohne redaktionelle Hürden eigene Beiträge,
Bilder und Audio publizieren konnten.

## Lizenz

Mir steht unter der **GNU General Public License, Version 2 (oder neuer)**.
Der vollständige Lizenztext liegt in [`COPYING`](COPYING).

Die Quelldateien tragen zusätzlich eine GPL-Linking-Ausnahme, die das Linken
gegen Bibliotheken unter der Apache Software License sowie die Bibliotheken
Sun Java Advanced Imaging (JAI) und Sun JIMI erlaubt. Den genauen Wortlaut
findest du im Kopf jeder Datei unter [`source/`](source/).

```
Copyright (C) 2001, 2002 The Mir-coders group
```

## Herkunft dieses Repositorys

Die ursprünglichen Projektseiten und das Code-Hosting sind seit Langem offline:

- `mir.indymedia.org` / `mir.indymedia.de` (Projektseite & Release-Downloads)
- `cvs.codecoop.org:/cvsroot/mir` (das ursprüngliche CVS-Repository)

Dieses Repository wurde aus den Release-Tarballs und den wöchentlichen
Quellcode-Snapshots rekonstruiert, die in der **Wayback Machine des Internet
Archive** erhalten geblieben sind. Jeder Commit entspricht einem archivierten
Snapshot oder Release und ist auf sein Originaldatum datiert – die Historie
bildet damit den tatsächlichen Entwicklungsverlauf von **November 2002 bis
November 2003** ab.

Getaggte Releases:

| Tag      | Datum      | Anmerkung                |
|----------|------------|--------------------------|
| `v1.0.0` | 2003-05-20 | Erstes stabiles Release  |
| `v1.1`   | 2003-11-10 | Letztes Release          |

Original-Download-URLs (über die Wayback Machine, zur Überprüfung):

- `https://web.archive.org/web/20070611144809id_/http://mir.indymedia.de/download/mir-1.1.tar.gz`
- `https://web.archive.org/web/20031018221223id_/http://mir.indymedia.org/download/mir-1.0.0.tar.bz2`
- wöchentliche Snapshots unter `http://mir.indymedia.de/download/snapshot/`

## Bauen

Mir nutzt Apache Ant. Der Build wird über [`build.xml`](build.xml) gesteuert;
Datenbankschema und Setup-Skripte liegen in [`dbscripts/`](dbscripts/), weitere
Dokumentation findet sich unter [`doc/`](doc/).

## Technik im Überblick

- **Sprache:** Java (J2EE / Servlets)
- **Application-Server:** Apache Tomcat
- **Datenbank:** PostgreSQL
- **Build:** Apache Ant
- **Mehrsprachig:** Lokalisierungen in rund einem Dutzend Sprachen
  (siehe [`bundles/`](bundles/))

## Dank

Dank an die Mir-coders für die Erstellung und Weiterentwicklung von Mir.
Kontakt damals: `mir-coders@lists.indymedia.org`.
