#!/bin/bash
# Solo Ledger — Build Script
# Run from the solo/ directory: bash ../build.sh

set -e
cd "$(dirname "$0")"

echo "=============================================="
echo " Solo Ledger — Android Build"
echo "=============================================="

# Detect platform
if command -v gradle &>/dev/null; then
    echo "[Gradle detected]"
    gradle assembleDebug --no-daemon 2>&1 | tail -30
elif command -v ./gradlew &>/dev/null; then
    echo "[Gradle Wrapper]"
    chmod +x ./gradlew
    ./gradlew assembleDebug --no-daemon 2>&1 | tail -30
else
    echo "[No Gradle — listing available options]"
    echo "  - Install Gradle: sudo apt install gradle"
    echo "  - Or open in Android Studio: File > Open > select this folder"
    echo "  - Or use IntelliJ IDEA with Android plugin"
    echo ""
    echo "Project structure:"
    find . -type f -not -path './.gradle/*' -not -path './build/*' | sort
fi
