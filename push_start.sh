#!/bin/sh
VERSION=${1}
docker image prune
sudo docker login --username=tb395460398 registry.cn-hangzhou.aliyuncs.com
docker rmi agent:${VERSION}
docker rmi registry.cn-hangzhou.aliyuncs.com/tianchi04/tianchi:${VERSION}
docker build -t agent:${VERSION} .
sudo docker tag agent:${VERSION} registry.cn-hangzhou.aliyuncs.com/tianchi04/tianchi:${VERSION}
sudo docker push registry.cn-hangzhou.aliyuncs.com/tianchi04/tianchi:${VERSION}
echo registry.cn-hangzhou.aliyuncs.com/tianchi04/tianchi:${VERSION}
