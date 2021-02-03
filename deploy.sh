#!/bin/bash
profile=$1
server=$2
export profile=$1

echo "These commands will be run on: $( uname -a )"
echo "They are executed by: $( whoami )"
echo "These are the environment vars passed in: ${server} ${profile} "

mkdir -p transaction-service
cp docker-compose.yml transaction-service/
scp -r transaction-service jenkins@$server:/tmp/
ssh  -tt jenkins@$server   -t "export  profile=$profile; /bin/bash" << 'ENDSSH'
#!mkdir -p /tmp/docker-configs/transaction-service

docker login https://mjenzi.tospay.net -u tsp_registry -p tsp_registry
cd /tmp/transaction-service

echo "These commands will be run on: $( uname -a )"
echo "They are executed by: $( whoami )"
echo "These are the environment vars passed in: ${server} ${profile} "
docker-compose pull
docker-compose down
docker-compose up --no-build -d
exit
ENDSSH