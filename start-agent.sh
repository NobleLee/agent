#!/bin/bash

ETCD_HOST=etcd
ETCD_PORT=2379
ETCD_URL=http://$ETCD_HOST:$ETCD_PORT

echo ETCD_URL = $ETCD_URL

if [[ "$1" == "consumer" ]]; then
  echo "Starting consumer agent..."
  java -jar \
       -Xms1G \
       -Xmx1G \
       -Dtype=consumer \
       -Dserver.port=20000 \
       -Detcd.url=$ETCD_URL \
       -Dlogs.dir=/root/logs \
       -Xloggc:/root/logs/gc.log \
       -XX:+PrintGCDateStamps \
       -XX:+PrintGCDetails \
       -XX:+PrintTenuringDistribution \
       -XX:NewRatio=2 \
       -XX:SurvivorRatio=10 \
       /root/dists/mesh-agent.jar
elif [[ "$1" == "provider-small" ]]; then
  echo "Starting small provider agent..."
  java -jar \
       -Xms512M \
       -Xmx512M \
       -Dtype=provider \
       -Ddubbo.protocol.port=20880 \
       -Detcd.url=$ETCD_URL \
       -Dlogs.dir=/root/logs \
       -Xloggc:/root/logs/gc.log \
       -XX:+PrintGCDateStamps \
       -XX:+PrintGCDetails \
       -XX:+PrintTenuringDistribution \
       -XX:NewRatio=2 \
       -XX:SurvivorRatio=8 \
       /root/dists/mesh-agent.jar
elif [[ "$1" == "provider-medium" ]]; then
  echo "Starting medium provider agent..."
  java -jar \
       -Xms1536M \
       -Xmx1536M \
       -Dtype=provider \
       -Ddubbo.protocol.port=20880 \
       -Detcd.url=$ETCD_URL \
       -XX:+PrintGCDetails \
       -Dlogs.dir=/root/logs \
       -XX:+PrintGCDateStamps \
       -Xloggc:/root/logs/gc.log \
       -XX:+PrintTenuringDistribution \
       -XX:NewRatio=2 \
       -XX:SurvivorRatio=8 \
       /root/dists/mesh-agent.jar
elif [[ "$1" == "provider-large" ]]; then
  echo "Starting large provider agent..."
  java -jar \
       -Xms2536M \
       -Xmx2536M \
       -Dtype=provider \
       -Ddubbo.protocol.port=20880 \
       -Detcd.url=$ETCD_URL \
       -XX:+PrintGCDetails \
       -Dlogs.dir=/root/logs \
       -XX:+PrintGCDateStamps \
       -XX:+PrintTenuringDistribution \
       -XX:NewRatio=2 \
       -XX:SurvivorRatio=8 \
       -Xloggc:/root/logs/gc.log \
       /root/dists/mesh-agent.jar
else
  echo "Unrecognized arguments, exit."
  exit 1
fi
