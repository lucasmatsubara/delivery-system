#!/usr/bin/env bash
# Fires N simultaneous POST /orders for the flash-sale dish and summarizes the status codes.
# Usage: ./scripts/race-test.sh [requests=50] [dishId=1] [host=localhost]
# Expected: exactly 10 x 201 and the dish stock ends at 0.
# With the rate limit on (default), part of the burst gets 429; to see only 201/409,
# start order-service with: ./gradlew bootRun --args='--delivery.rate-limit.enabled=false'

REQUESTS=${1:-50}
DISH_ID=${2:-1}
HOST=${3:-localhost}
URL="http://${HOST}:8080"

echo "Stock before: $(curl -s "${URL}/dishes/${DISH_ID}")"
echo "Sending ${REQUESTS} simultaneous orders for dish ${DISH_ID}..."

seq "${REQUESTS}" | xargs -P "${REQUESTS}" -I{} \
  curl -s -o /dev/null -w "%{http_code}\n" -X POST "${URL}/orders" \
  -H "Content-Type: application/json" \
  -d "{\"dishId\": ${DISH_ID}, \"quantity\": 1}" \
  | sort | uniq -c | awk '{ printf "  HTTP %s: %s\n", $2, $1 }'

echo "Stock after:  $(curl -s "${URL}/dishes/${DISH_ID}")"
