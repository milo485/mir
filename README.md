# Mir

**Mir** is a Java-based, free-software content management system written by the
**Mir-coders group** for the Indymedia network. It powered numerous Indymedia
sites — among them de.indymedia.org until May 2014 — and is built on
Java / Tomcat (servlets) with a PostgreSQL backend.

> *"dont hate the media, become the media!"*

## License

Mir is released under the **GNU General Public License, version 2 (or later)**.
The full text is in [`COPYING`](COPYING).

The source files additionally carry a GPL linking exception that permits linking
against libraries under the Apache Software License as well as the Sun Java
Advanced Imaging (JAI) and Sun JIMI libraries. See the header of any file under
[`source/`](source/) for the exact wording.

```
Copyright (C) 2001, 2002 The Mir-coders group
```

## Provenance of this repository

The original homepages and code hosting are long offline:

- `mir.indymedia.org` / `mir.indymedia.de` (project site & release downloads)
- `cvs.codecoop.org:/cvsroot/mir` (the original CVS repository)

This repository was reassembled from the release tarballs and weekly source
snapshots preserved by the **Internet Archive Wayback Machine**. Each commit
corresponds to one archived snapshot or release, dated to its original date, so
the history reflects the project's actual development timeline from
**November 2002 to November 2003**.

Tagged releases:

| Tag      | Date       | Notes                |
|----------|------------|----------------------|
| `v1.0.0` | 2003-05-20 | First stable release |
| `v1.1`   | 2003-11-10 | Final release        |

Original download URLs (via the Wayback Machine), should you wish to verify:

- `https://web.archive.org/web/20070611144809id_/http://mir.indymedia.de/download/mir-1.1.tar.gz`
- `https://web.archive.org/web/20031018221223id_/http://mir.indymedia.org/download/mir-1.0.0.tar.bz2`
- weekly snapshots under `http://mir.indymedia.de/download/snapshot/`

## Building

Mir uses Apache Ant. The build is driven by [`build.xml`](build.xml); database
schema and setup scripts live in [`dbscripts/`](dbscripts/), and further
documentation is under [`doc/`](doc/).

## Credits

Thanks to the Mir-coders for creating and maintaining Mir.
Contact at the time: `mir-coders@lists.indymedia.org`.
