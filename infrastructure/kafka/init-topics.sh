#!/usr/bin/env bash
set -e

BOOTSTRAP_SERVER="kafka:9092"

echo "Waiting for Kafka to be ready..."
sleep 10

create_topic () {
  local topic=$1
  local partitions=${2:-3}
  local replication=${3:-1}

  echo "Creating topic: $topic"

  /opt/kafka/bin/kafka-topics.sh \
    --bootstrap-server "$BOOTSTRAP_SERVER" \
    --create \
    --if-not-exists \
    --topic "$topic" \
    --partitions "$partitions" \
    --replication-factor "$replication"
}

create_topic payments.payment-requested.v1
create_topic payments.payment-succeeded.v1
create_topic payments.payment-failed.v1
create_topic payments.payment-cancelled.v1

echo "Kafka topics initialized"
