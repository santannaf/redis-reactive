
docker rm $(docker ps -a -q) -f

docker build -t thalessantanna/redis-reactive-demo .

docker-compose up -d