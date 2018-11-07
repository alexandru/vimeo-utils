#!/usr/bin/env bash
#
# Script that detects if Scala's SBT is installed and if
# not then it automatically downloads and installs it at
# a specified path.
#
# Author: Alexandru Nedelcu (https://alexn.org)
#

set -e

VERSION="1.2.3"
SBT_DIR="$(dirname $0)/.sbt"

if [ -x "$(command -v sbt)" ]; then
    SBT_COMMAND="sbt"
elif ! [ -d "${SBT_DIR}" ]; then
    DIR="$(mktemp -d)"
    echo "Downloading SBT (temp dir: ${DIR}), please wait..."
    wget -q "https://piccolo.link/sbt-$VERSION.tgz" -O ${DIR}/sbt.tgz
    echo "Uncompressing and copying SBT ..."
    tar xzf "${DIR}/sbt.tgz" -C "${DIR}"
    mv "${DIR}/sbt" "${SBT_DIR}"
    SBT_COMMAND="${SBT_DIR}/bin/sbt"
else
    SBT_COMMAND="${SBT_DIR}/bin/sbt"
fi

echo "Using SBT command: ${SBT_COMMAND}"
exec "${SBT_COMMAND}" "$@"
