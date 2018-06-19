#!/bin/sh
function service_deploy(){
    if [[ "$1" == "consumer" ]]; then
      echo "Starting consumer service..."
      nohup java -jar \
            -Xms2G \
            -Xmx2G \
            -Dlogs.dir=/root/logs/consumer \
            -Xloggc:/root/logs/consumer/consumer_gc.log \
            -XX:+PrintGCDetails \
            /root/dists/mesh-consumer.jar \
            > /root/logs/service/s_consumer.log 2>&1 &
    elif [[ "$1" == "provider-small" ]]; then
      echo "Starting small provider service..."
      nohup java -jar \
            -Xms1G \
            -Xmx1G \
            -Ddubbo.protocol.port=20889 \
            -Ddubbo.application.qos.enable=false \
            -Dlogs.dir=/root/logs/provider-small \
            -Xloggc:/root/logs/provider-small/provider_gc.log \
            -XX:+PrintGCDetails \
            /root/dists/mesh-provider.jar \
            > /root/logs/service/s_provider-small.log 2>&1 &
    elif [[ "$1" == "provider-medium" ]]; then
      echo "Starting medium provider service..."
      nohup java -jar \
            -Xms2G \
            -Xmx2G \
            -Ddubbo.protocol.port=20890 \
            -Ddubbo.application.qos.enable=false \
            -Dlogs.dir=/root/logs/provider-medium \
            -Xloggc:/root/logs/provider-medium/provider_gc.log \
            -XX:+PrintGCDetails \
            /root/dists/mesh-provider.jar \
            > /root/logs/service/s_provider-medium.log 2>&1 &
    elif [[ "$1" == "provider-large" ]]; then
      echo "Starting large provider service..."
      nohup java -jar \
            -Xms2G \
            -Xmx2G \
            -Ddubbo.protocol.port=20891 \
            -Ddubbo.application.qos.enable=false \
            -Dlogs.dir=/root/logs/provider-large \
            -Xloggc:/root/logs/provider-large/provider_gc.log \
            -XX:+PrintGCDetails \
            /root/dists/mesh-provider.jar \
            > /root/logs/service/s_provider-large.log 2>&1 &
    else
      echo "Unrecognized arguments, exit."
      exit 1
    fi
}


function agent_deploy(){

    ETCD_HOST=104.236.138.7
    ETCD_PORT=2379
    ETCD_URL=http://$ETCD_HOST:$ETCD_PORT

    if [[ "$1" == "consumer" ]]; then
      echo "Starting consumer consumer..."
      nohup java -jar \
           -Xms1536M \
           -Xmx1536M \
           -Dtype=consumer \
           -Dserver.port=20000\
           -Detcd.url=$ETCD_URL \
           -Dlogs.dir=/root/logs/consumer \
           -Xloggc:/root/logs/consumer/agent_gc.log \
           /root/dists/mesh-agent.jar \
           > /root/logs/service/a_consumer.log 2>&1 &
    elif [[ "$1" == "provider-small" ]]; then
      echo "Starting small provider consumer..."
      nohup java -jar \
           -Xms512M \
           -Xmx512M \
           -Dtype=provider \
           -Dserver.port=30000\
           -Ddubbo.protocol.port=20889 \
           -Detcd.url=$ETCD_URL \
           -Dlogs.dir=/root/logs/provider-small \
           -Xloggc:/root/logs/provider-small/agent_gc.log \
           /root/dists/mesh-agent.jar \
           > /root/logs/service/a_provider-small.log 2>&1 &
    elif [[ "$1" == "provider-medium" ]]; then
      echo "Starting medium provider consumer..."
      nohup java -jar \
           -Xms1024M \
           -Xmx1024M \
           -Dtype=provider \
           -Dserver.port=30001\
           -Ddubbo.protocol.port=20890 \
           -Detcd.url=$ETCD_URL \
           -Dlogs.dir=/root/logs/provider-medium \
           -Xloggc:/root/logs/provider-medium/agent_gc.log \
           /root/dists/mesh-agent.jar \
           > /root/logs/service/a_provider-medium.log 2>&1 &
    elif [[ "$1" == "provider-large" ]]; then
      echo "Starting large provider consumer..."
      nohup java -jar \
           -Xms1024M \
           -Xmx1024M \
           -Dtype=provider \
           -Dserver.port=30002\
           -Ddubbo.protocol.port=20891 \
           -Detcd.url=$ETCD_URL \
           -Dlogs.dir=/root/logs/provider-large \
           -Xloggc:/root/logs/provider-large/agent_gc.log \
           /root/dists/mesh-agent.jar \
           > /root/logs/service/a_provider-large.log 2>&1 &
    else
      echo "Unrecognized arguments, exit."
      exit 1
    fi
}

kill -9 `ps -a|grep java|grep Xms|awk '{print $1}'`

# consumer
mvn -DskipTests=true package -f pom.xml
rm -rf /root/dists/mesh-agent.jar
cp ./mesh-agent/target/mesh-agent-1.0-SNAPSHOT.jar /root/dists/mesh-agent.jar
local_path=`pwd`
# consumer
cd /Users/gaoguili/project/Java/RPC_MQ/services-master
mvn -DskipTests=true package -f pom.xml
rm -rf /root/dists/mesh-consumer.jar
cp ./mesh-consumer/target/mesh-consumer-1.0-SNAPSHOT.jar /root/dists/mesh-consumer.jar
# provider
cd /Users/gaoguili/project/Java/RPC_MQ/services-provider
mvn -DskipTests=true package -f pom.xml
rm -rf /root/dists/mesh-provider.jar
cp ./mesh-provider/target/mesh-provider-1.0-SNAPSHOT.jar /root/dists/mesh-provider.jar
cd ${local_path}
echo '----------------------------------------------------------------'
echo "clear logs...."
rm -rf /root/logs/provider-medium/* /root/logs/provider-large/* /root/logs/provider-small/* /root/logs/consumer/*
rm -rf /root/logs/service/*

# small
service_deploy provider-small
agent_deploy provider-small
# "start provider-medium....."
service_deploy provider-medium
agent_deploy provider-medium
# "start provider-large....."
service_deploy provider-large
agent_deploy provider-large
# "start consumer....."
service_deploy consumer
agent_deploy consumer
echo "done"

