#!/bin/sh
VERSION=${1}
sudo docker login --username=wuyubohehe registry.cn-hangzhou.aliyuncs.com
docker rmi agent:${VERSION}
docker rmi registry.cn-hangzhou.aliyuncs.com/bobotianchi/agent:${VERSION}
docker build -t agent:${VERSION} .

sudo docker tag agent:${VERSION} registry.cn-hangzhou.aliyuncs.com/bobotianchi/agent:${VERSION}
sudo docker push registry.cn-hangzhou.aliyuncs.com/bobotianchi/agent:${VERSION}

