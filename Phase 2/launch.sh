#!/bin/bash

# Start minikube
minikube start

# Set minikube Docker environment variables
eval $(minikube docker-env)

# Build images for all four services
# docker compose up
docker build -t user-service:v0 user
docker build -t h2db:v0 h2-db-service
docker build -t wallet-service:v0 wallet
docker build -t booking-service:v0 booking


# Launch deployments for all four services
kubectl apply -f user/deployment.yaml
kubectl apply -f wallet/deployment.yaml
kubectl apply -f booking/deployment.yaml
kubectl apply -f h2-db-service/deployment.yaml

# Launch services/load-balancer
kubectl apply -f user/service.yaml
kubectl apply -f wallet/service.yaml
kubectl apply -f booking/service.yaml
kubectl apply -f h2-db-service/service.yaml

minikube tunnel


# Expose main three services at ports 8080, 8081, and 8082
# minikube service <team-member1-service-name> --url | sed 's/.*:\([0-9]*\)$/\1/' | xargs -I {} kubectl port-forward service/<team-member1-service-name> 8080:{}
# minikube service <team-member2-service-name> --url | sed 's/.*:\([0-9]*\)$/\1/' | xargs -I {} kubectl port-forward service/<team-member2-service-name> 8081:{}
# minikube service <team-member3-service-name> --url | sed 's/.*:\([0-9]*\)$/\1/' | xargs -I {} kubectl port-forward service/<team-member3-service-name> 8082:{}
