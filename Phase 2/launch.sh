#!/bin/bash

# Start minikube
minikube start
minikube addons enable metrics-server
eval $(minikube docker-env)

# Build images for all four services
# docker compose up
docker build -t user-service:v0 user
docker build -t h2db-service:v0 h2-db-service
docker build -t wallet-service:v0 wallet
docker build -t booking-service:v0 booking


# Launch deployments for all four services
kubectl apply -f user/deployment.yaml
kubectl apply -f wallet/deployment.yaml

kubectl apply -f booking/deployment.yaml
kubectl apply -f booking/autoscale.yaml

kubectl apply -f h2-db-service/deployment.yaml

# Launch services/load-balancer
kubectl apply -f user/service.yaml
kubectl apply -f wallet/service.yaml
kubectl apply -f booking/service.yaml
kubectl apply -f h2-db-service/service.yaml


pkill -f "kubectl port-forward"

echo "Wait for 30 seconds"
sleep 30
kubectl port-forward service/user-service 8080:8080 &
kubectl port-forward service/booking-service 8081:8081 &
kubectl port-forward service/wallet-service 8082:8082 &

sleep 30
echo "Cluster Ready to run..."