#!/bin/bash

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

if [ "$#" -lt 3 ]; then
    echo "Usage: ./check.sh <source.kt> <ref.jar> <test_case_1> [<test_case_2> ...]"
    exit 1
fi

STUDENT_SRC=$1
REFERENCE_JAR=$2

shift 2

STUDENT_JAR="${STUDENT_SRC%.*}.jar"

echo -e "--- Компиляция $STUDENT_SRC ---"
kotlinc "$STUDENT_SRC" -include-runtime -d "$STUDENT_JAR"

if [ $? -ne 0 ]; then
    echo -e "${RED}Ошибка компиляции!${NC}"
    exit 1
fi

HAS_ERROR=0
TEST_NUM=1

for INPUT_DATA in "$@"; do
    echo -e "\n=== Тест #$TEST_NUM: \"$INPUT_DATA\" ==="

    java -jar "$REFERENCE_JAR" $INPUT_DATA > expected.txt

    java -jar "$STUDENT_JAR" $INPUT_DATA > actual.txt

    diff -w -B expected.txt actual.txt

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}OK${NC}"
    else
        echo -e "${RED}FAIL${NC}"
        HAS_ERROR=1
    fi

    ((TEST_NUM++))
done

rm "$STUDENT_JAR" expected.txt actual.txt 2>/dev/null

echo -e "\n-----------------------------------"
if [ $HAS_ERROR -eq 0 ]; then
    echo -e "${GREEN}ВСЕ ТЕСТЫ ПРОЙДЕНЫ${NC}"
    exit 0
else
    echo -e "${RED}ЕСТЬ ОШИБКИ${NC}"
    exit 1
fi