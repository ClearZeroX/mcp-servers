#!/usr/bin/env bash
set -euo pipefail

PLUGIN_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAR="$PLUGIN_DIR/target/business-diagnostic-mcp.jar"

if [[ ! -f "$JAR" ]]; then
  (
    cd "$PLUGIN_DIR"
    mvn -q -DskipTests package
  )
fi

exec java -jar "$JAR"
