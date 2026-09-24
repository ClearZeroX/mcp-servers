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

exec java -Xms8m -Xmx64m -XX:MaxMetaspaceSize=64m -XX:ReservedCodeCacheSize=24m -Xss512k -XX:+UseSerialGC -jar "$JAR"
