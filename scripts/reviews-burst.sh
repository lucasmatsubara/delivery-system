#!/usr/bin/env bash
# Sends a burst of reviews (random dish and rating) and shows the ranking after the next flush.
# Usage: ./scripts/reviews-burst.sh [reviews=500] [host=localhost]

TOTAL=${1:-500}
HOST=${2:-localhost}

echo "Publishing ${TOTAL} reviews..."
seq "${TOTAL}" | xargs -P 50 -I{} bash -c '
  dish=$(( RANDOM % 5 + 1 )); rating=$(( RANDOM % 5 + 1 ))
  curl -s -o /dev/null -w "%{http_code}\n" -X POST "http://'"${HOST}"':8080/reviews" \
    -H "Content-Type: application/json" \
    -d "{\"dishId\": ${dish}, \"rating\": ${rating}, \"comment\": \"Load test\"}"
' | sort | uniq -c | awk '{ printf "  HTTP %s: %s\n", $2, $1 }'

echo "Waiting for the review-service flush (5 s)..."
sleep 6
curl -s "http://${HOST}:8083/reviews/ranking"
echo
