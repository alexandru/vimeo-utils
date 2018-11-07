# vimeo-download-plus

A server implementation that redirects to a Vimeo raw video that was uploaded
by a Plus account.

Don't abuse.

## Compiling, Building Locally

Install [Scala SBT](https://www.scala-sbt.org/).

To execute the project locally, only once:

```
sbt run
```

For iterative development, to have the code continuously recompiled and the
server restarted when code changes are detected:

```
sbt ~reStart
```

For packaging the project for deployment as a DEB archive with all
dependencies and the scripts needed for execution (n.b. this needs
`fakeroot` to be installed on your system otherwise it throws an
error):

```
sbt debian:packageBin
```

This works for Ubuntu/Debian, producing a `.deb` package in `./target`, which
you can then install on a Debian machine with:

```
dpkg -i ./target/oriel-cmp-backend_0.0.1_all.deb
```

## LICENSE 

    Copyright (C) 2018  Alexandru Nedelcu

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
