# Builder container
# FROM registry.cn-hangzhou.aliyuncs.com/aliware2018/services AS builder

# maven dependency
#FROM registry.cn-hangzhou.aliyuncs.com/acs/maven
FROM service:0.0.1 AS builder

COPY . /root/workspace/agent
WORKDIR /root/workspace/agent
RUN set -ex
#&& mvn clean package


# Runner container
FROM registry.cn-hangzhou.aliyuncs.com/aliware2018/debian-jdk8

COPY --from=builder /root/workspace/services/mesh-provider/target/mesh-provider-1.0-SNAPSHOT.jar /root/dists/mesh-provider.jar
COPY --from=builder /root/workspace/services/mesh-consumer/target/mesh-consumer-1.0-SNAPSHOT.jar /root/dists/mesh-consumer.jar
COPY --from=builder /root/workspace/agent/mesh-agent/target/mesh-agent-1.0-SNAPSHOT.jar /root/dists/mesh-agent.jar

COPY --from=builder /usr/local/bin/docker-entrypoint.sh /usr/local/bin
COPY start-agent.sh /usr/local/bin

RUN set -ex && mkdir -p /root/logs && chmod u+x /usr/local/bin/docker-entrypoint.sh && chmod u+x /usr/local/bin/start-agent.sh

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
