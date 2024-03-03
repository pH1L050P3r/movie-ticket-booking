#!/bin/bash

pkill -f "kubectl port-forward"

# Deleting services
kubectl delete service tarun-user-service tarun-booking-service tarun-wallet-service tarun-h2db-service

# Deleting Deployments
kubectl delete deployment tarun-user-service tarun-booking-service tarun-wallet-service tarun-h2db-service

# Deleting Autoscaling
kubectl delete hpa tarun-booking-service-hpa

# wait for 60 seconds
sleep 60

# Deleting images
eval $(minikube docker-env)
docker rmi tarun-user-service:v0 tarun-booking-service:v0 tarun-wallet-service:v0 tarun-h2db-service:v0
eval $(minikube docker-env -u)

minikube stop
minikube delete