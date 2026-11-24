#!/usr/bin/env bash
# Compiles the Online Examination System + JUnit 5 tests and runs the test suite.
# Usage:  ./run_tests.sh                 -> run all tests, XML report in ./out/reports
#         ./run_tests.sh --demo          -> run the console demo application instead
#         ./run_tests.sh --web           -> start the web UI on http://localhost:9090/
#         EXAM_SECONDS=60 ./run_tests.sh --web  -> web UI with a 1-minute exam
set -euo pipefail
cd "$(dirname "$0")"

JUNIT=lib/junit-platform-console-standalone-1.11.4.jar
# Use the java that sits next to javac (avoids picking up an older JRE on PATH)
JAVA="$(dirname "$(command -v javac)")/java"
# Classpath separator: ';' on Windows (Git Bash), ':' elsewhere
case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=';' ;; *) SEP=':' ;; esac

rm -rf out/main out/test
javac -d out/main $(find src/main/java -name '*.java')

if [[ "${1:-}" == "--demo" ]]; then
  "$JAVA" -cp out/main com.oes.Main
  exit $?
fi

if [[ "${1:-}" == "--web" ]]; then
  "$JAVA" "-Does.examSeconds=${EXAM_SECONDS:-600}" "-Does.port=${PORT:-9090}" -cp out/main com.oes.web.WebServer
  exit $?
fi

javac -d out/test -cp "out/main${SEP}${JUNIT}" $(find src/test/java -name '*.java')
"$JAVA" -jar "$JUNIT" execute \
  --class-path "out/main${SEP}out/test" \
  --scan-class-path \
  --details tree \
  --disable-banner \
  --reports-dir "${REPORT_DIR:-out/reports}"
