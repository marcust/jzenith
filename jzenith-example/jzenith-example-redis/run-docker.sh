#!/bin/sh -u

REDIS_RUNNING=$(docker ps | grep redis)
if [ -z "${REDIS_RUNNING}" ]; then
    docker run -d --cpuset-cpus=0 --memory=100m --name redis redis:4
fi
    
docker run -d -p 8080:8080 -e "REST_HOST=0.0.0.0" --cpuset-cpus=1-3 --memory 256m --link redis:redis -e "REDIS_HOST=\${REDIS_PORT_6379_TCP_ADDR}" -e "REDIS_PORT=\${REDIS_PORT_6379_TCP_PORT}" $1
