#!/bin/bash

##################################
##    Make graphene directory    #
##################################
export GRAPHENE_CONF_DIR=${GRAPHENE_CONF_DIR:-"/graphene/conf"}
export GRAPHENE_DATA_DIR=${GRAPHENE_DATA_DIR:-"/graphene/data"}
export GRAPHENE_LOG_DIR=${GRAPHENE_LOG_DIR:-"/graphene/log"}
export GRAPHENE_PROGRAM_DIR=${GRAPHENE_PROGRAM_DIR:-"/graphene/program"}

mkdir -p ${GRAPHENE_CONF_DIR}
mkdir -p ${GRAPHENE_DATA_DIR}
mkdir -p ${GRAPHENE_LOG_DIR}
mkdir -p ${GRAPHENE_PROGRAM_DIR}

#################################
#         Resolve HOST NAME     #
#################################
export HOST_NAME=$(HOSTNAME)

#################################
#         Set Heap Option       #
#################################
export GRAPHENE_HEAP_OPTS=${GRAPHENE_HEAP_OPTS:-"-Xmx1G -Xms1G"}

#################################
#   Resolve confd template      #
#################################
mkdir -p /etc/${GRAPHENE_TYPE}
confd --onetime --backend env

#################################
#   Move to graphene directory  #
#################################
mv /etc/${GRAPHENE_TYPE}/application.yml ${GRAPHENE_CONF_DIR}/application.yml
mv /tmp/graphene/${GRAPHENE_TYPE}/build/libs/${GRAPHENE_TYPE}-${GRAPHENE_VERSION}.jar ${GRAPHENE_PROGRAM_DIR}/

#################################
#   Execute graphene process    #
#################################
if [[ -z "$1" ]];
then
  exec java $GRAPHENE_HEAP_OPTS -jar ${GRAPHENE_PROGRAM_DIR}/${GRAPHENE_TYPE}-${GRAPHENE_VERSION}.jar --spring.config.location=file:${GRAPHENE_CONF_DIR}/application.yml
else
  exec "$@"
fi
