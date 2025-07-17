#!/bin/bash

set -e

HTTP_PORT="${HTTP_PORT:-9000}"
LAST_MODIFIED="$(date --iso-8601=ns)"
RUNTIME_HOST="$(hostname -i)"
OUTDIR="~/.akka/local"
OUTFILE="${OUTDIR}/$(hostname).conf"

mkdir -p "$OUTDIR"

cat > "$OUTFILE" <<EOF
{
    "http-port" : "$HTTP_PORT",
    "last-modified" : "$LAST_MODIFIED",
    "runtime-host" : "$RUNTIME_HOST"
}
EOF