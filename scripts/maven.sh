#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
# Homebrew Java 17 fixes machines whose JAVA_HOME still points at Java 8.
if [[ "$(uname -s)" == Darwin ]]; then
  for candidate in /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home /usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home; do
    if [[ -x "$candidate/bin/java" ]]; then export JAVA_HOME="$candidate"; break; fi
  done
fi
exec mvn "$@"
