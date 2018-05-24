#!/bin/sh
VERSION=${1}
docker rmi agent:${VERSION}
docker rmi registry.cn-hangzhou.aliyuncs.com/tianchi04/tianchi:${VERSION}
docker build -t agent:${VERSION} .
echo "jianli1004"|sudo docker tag agent:${VERSION} registry.cn-hangzhou.aliyuncs.com/tianchi04/tianchi:${VERSION}
echo "jianli1004"|sudo docker push registry.cn-hangzhou.aliyuncs.com/tianchi04/tianchi:${VERSION}
