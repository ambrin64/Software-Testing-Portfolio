#!/usr/bin/env bash
# Compiles the Online Examination System + JUnit 5 tests and runs the test suite.
# Usage:  ./run_tests.sh                 -> run all tests, XML report in ./out/reports
#         ./run_tests.sh --demo          -> run the console demo application instead
set -euo pipefail
cd "$(dirname "$0")"

JUNIT=lib/junit-platform-console-standalone-1.11.4.jar
# Classpath separator: ';' on Windows (Git Bash), ':' elsewhere
case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=';' ;; *) SEP=':' ;; esac

rm -rf out/main out/test
javac -d out/main $(find src/main/java -name '*.java')

if [[ "${1:-}" == "--demo" ]]; then
  java -cp out/main com.oes.Main
  exit $?
fi

javac -d out/test -cp "out/main${SEP}${JUNIT}" $(find src/test/java -name '*.java')
java -jar "$JUNIT" execute \
  --class-path "out/main${SEP}out/test" \
  --scan-class-path \
  --details tree \
  --disable-banner \
  --reports-dir "${REPORT_DIR:-out/reports}"
