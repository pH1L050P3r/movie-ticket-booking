#!/bin/bash

# Deleting services
kubectl delete service user-service booking-service wallet-service h2db-service

# Deleting Deployments
kubectl delete deployment user-service booking-service wallet-service h2db-service

sleep 60

# Deleting images
eval $(minikube docker-env)
docker rmi user-service:v0 booking-service:v0 wallet-service:v0 h2db-service:v0