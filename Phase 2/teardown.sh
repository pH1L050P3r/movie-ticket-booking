#!/bin/bash

pkill -f "kubectl port-forward"

# Deleting services
kubectl delete service user-service booking-service wallet-service h2db-service

# Deleting Deployments
kubectl delete deployment user-service booking-service wallet-service h2db-service

# Deleting Autoscaling
kubectl delete hpa booking-service-hpa

# wait for 60 seconds
sleep 60

# Deleting images
eval $(minikube docker-env)
docker rmi user-service:v0 booking-service:v0 wallet-service:v0 h2db-service:v0
eval $(minikube docker-env -u)

minikube stop
minikube delete