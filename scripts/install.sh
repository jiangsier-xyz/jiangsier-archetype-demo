#!/usr/bin/env bash

source $(dirname ${BASH_SOURCE[0]})/setenv.sh

check_helm

helm install --kubeconfig ${KUBE_CONFIG} --namespace ${NAMESPACE} --create-namespace -f ${values_yaml} \
  --set-file mysql.initdbScripts.1-schema\\.sql=${PROJECT_PATH}/${DAL_MODULE}/src/main/resources/sql/schema.sql \
  --set cert.clusterIssuer.enabled=false \
  --set deployment.enabled=true \
  --set deployment.pvc.enabled=false \
  ${ARGS[*]} \
  ${PROJECT_NAME} ${HELM_CONFIG_HOME}