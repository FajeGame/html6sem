#!/bin/bash

BASE_URL="http://localhost:8080"
CONTENT_TYPE="Content-Type: application/json"

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

if ! command -v jq &> /dev/null; then
    echo -e "${RED}Ошибка: утилита jq не установлена.${NC}"
    echo "Установите её (sudo apt install jq / brew install jq) и попробуйте снова."
    exit 1
fi

echo "Ожидаю запуск сервера на $BASE_URL..."
RETRIES=30 # Пытаемся 30 раз (30 * 2 сек = 1 минута макс)
count=0

until curl -s "$BASE_URL/greeting" > /dev/null; do
    count=$((count+1))
    if [ $count -ge $RETRIES ]; then
        echo -e "${RED}Ошибка: Сервер не запустился за минуту.${NC}"
        exit 1
    fi
    echo "Ждем запуска... ($count/$RETRIES)"
    sleep 2
done

echo -e "${GREEN}Сервер доступен! Начинаем тесты.${NC}"

echo "Запуск тестов API на $BASE_URL..."
echo "---------------------------------------------------"

# ==========================================
# ТЕСТ 1: Проверка метода Hello (GET /greeting)
# ==========================================
echo -n "Тест 1: Проверка GET /greeting (default)... "

RESPONSE=$(curl -s -X GET "$BASE_URL/greeting")

TEXT_VALUE=$(echo "$RESPONSE" | jq -r '.text')

if [ "$TEXT_VALUE" != "null" ] && [ "$TEXT_VALUE" == "Hello World" ]; then
    echo -e "${GREEN}OK${NC}"
else
    echo -e "${RED}FAIL${NC}"
    echo "  Ожидалось поле 'text': 'Hello World'"
    echo "  Получено: $RESPONSE"
    exit 1
fi

# ==========================================
# ТЕСТ 2: Полный цикл (POST -> GET)
# ==========================================
echo -n "Тест 2: Проверка цикла POST -> GET... "

# Данные для теста
TEST_NAME="Bash"
TEST_SURNAME="Script"
JSON_PAYLOAD="{\"name\": \"$TEST_NAME\", \"surname\": \"$TEST_SURNAME\"}"

POST_RESPONSE=$(curl -s -X POST \
  -H "$CONTENT_TYPE" \
  -d "$JSON_PAYLOAD" \
  "$BASE_URL/greeting")

USER_ID=$(echo "$POST_RESPONSE" | jq -r '.id')

if [ "$USER_ID" == "null" ] || [ -z "$USER_ID" ]; then
    echo -e "${RED}FAIL (POST)${NC}"
    echo "  Не удалось получить ID из ответа: $POST_RESPONSE"
    exit 1
fi

GET_RESPONSE=$(curl -s -X GET "$BASE_URL/greeting/$USER_ID")

GOT_NAME=$(echo "$GET_RESPONSE" | jq -r '.name')
GOT_SURNAME=$(echo "$GET_RESPONSE" | jq -r '.surname')

if [ "$GOT_NAME" == "$TEST_NAME" ] && [ "$GOT_SURNAME" == "$TEST_SURNAME" ]; then
    echo -e "${GREEN}OK${NC}"
else
    echo -e "${RED}FAIL (GET)${NC}"
    echo "  Отправлено: name=$TEST_NAME, surname=$TEST_SURNAME"
    echo "  Получено:   name=$GOT_NAME, surname=$GOT_SURNAME"
    echo "  ID: $USER_ID"
    exit 1
fi

# ==========================================
# ТЕСТ 3: GET через query param
# ==========================================
echo -n "Тест 3: GET через query param... "

GET_RESPONSE=$(curl -s -X GET "$BASE_URL/greeting?id=$USER_ID")

GOT_NAME=$(echo "$GET_RESPONSE" | jq -r '.name')
GOT_SURNAME=$(echo "$GET_RESPONSE" | jq -r '.surname')

if [ "$GOT_NAME" == "$TEST_NAME" ] && [ "$GOT_SURNAME" == "$TEST_SURNAME" ]; then
    echo -e "${GREEN}OK${NC}"
else
    echo -e "${RED}FAIL (GET)${NC}"
    echo "  Отправлено: name=$TEST_NAME, surname=$TEST_SURNAME"
    echo "  Получено:   name=$GOT_NAME, surname=$GOT_SURNAME"
    echo "  ID: $USER_ID"
    exit 1
fi

echo "---------------------------------------------------"
echo -e "${GREEN}Все тесты успешно пройдены!${NC}"
exit 0