#!/bin/sh

set -ue

ID=$(curl -q -H "Content-Type: application/json" -XPOST -d '{"name":"Test"}' -v localhost:8080/user | jq -r .id)

echo "Warmup"
hey -z 120s http://localhost:8080/user/$ID

echo "Test"
hey -z 10s http://localhost:8080/user/$ID
