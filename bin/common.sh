#!/usr/bin/env bash
SOCIALITE_PREFIX=/home/${USER}/socialite
EXT=${SOCIALITE_PREFIX}/ext
#HADOOP_COMMON=${HADOOP_HOME}/share/hadoop/common
#HADOOP_HDFS=${HADOOP_HOME}/share/hadoop/hdfs
#HADOOP_YARN=${HADOOP_HOME}/share/hadoop/yarn

JAR_PATH=${EXT}/annotations-5.1.jar
JAR_PATH=${JAR_PATH}:${EXT}/antlr-3.5.2-complete-no-st3.jar
JAR_PATH=${JAR_PATH}:${EXT}/antlrworks-1.4.3.jar
JAR_PATH=${JAR_PATH}:${EXT}/antlrworks-1.5.jar
JAR_PATH=${JAR_PATH}:${EXT}/commons-collections-3.2.1.jar
JAR_PATH=${JAR_PATH}:${EXT}/commons-configuration-1.6.jar
JAR_PATH=${JAR_PATH}:${EXT}/commons-lang-2.6.jar
JAR_PATH=${JAR_PATH}:${EXT}/commons-lang3-3.1.jar
JAR_PATH=${JAR_PATH}:${EXT}/commons-logging-1.1.1.jar
JAR_PATH=${JAR_PATH}:${EXT}/commons-logging-api-1.0.4.jar
JAR_PATH=${JAR_PATH}:${EXT}/guava-18.0.jar
JAR_PATH=${JAR_PATH}:${EXT}/log4j-1.2.16.jar
JAR_PATH=${JAR_PATH}:${EXT}/RoaringBitmap-0.5.18.jar
JAR_PATH=${JAR_PATH}:${EXT}/ST-4.0.7.jar
JAR_PATH=${JAR_PATH}:${EXT}/trove-3.0.3.jar
JAR_PATH=${JAR_PATH}:${EXT}/serialize/libthrift-0.9.3.jar
JAR_PATH=${JAR_PATH}:${EXT}/serialize/protobuf-java-2.5.0.jar
JAR_PATH=${JAR_PATH}:${EXT}/serialize/slf4j-api-1.7.13.jar
JAR_PATH=${JAR_PATH}:${EXT}/serialize/slf4j-log4j12-1.7.13.jar
JAR_PATH=${JAR_PATH}:${EXT}/hadoop/commons-io-2.4.jar
JAR_PATH=${JAR_PATH}:${EXT}/hadoop/commons-cli-1.2.jar #needed
JAR_PATH=${JAR_PATH}:${EXT}/hadoop/servlet-api-2.5.jar #needed
JAR_PATH=${JAR_PATH}:${EXT}/hadoop/hadoop-auth-2.7.1.jar
JAR_PATH=${JAR_PATH}:${EXT}/hadoop/hadoop-common-2.7.1.jar
JAR_PATH=${JAR_PATH}:${EXT}/hadoop/hadoop-hdfs-2.7.1.jar
JAR_PATH=${JAR_PATH}:${EXT}/hadoop/hadoop-yarn-api-2.7.1.jar
JAR_PATH=${JAR_PATH}:${EXT}/hadoop/hadoop-yarn-client-2.7.1.jar
JAR_PATH=${JAR_PATH}:${EXT}/hadoop/hadoop-yarn-common-2.7.1.jar
JAR_PATH=${JAR_PATH}:${EXT}/hadoop/hadoop-yarn-registry-2.7.1.jar
JAR_PATH=${JAR_PATH}:${EXT}/hadoop/htrace-core-3.2.0-incubating.jar
JAR_PATH=${JAR_PATH}:${EXT}/python/jython-standalone-2.7.1b2.jar
JAR_PATH=${JAR_PATH}:${EXT}/com.microsoft.z3.jar
JAR_PATH=${JAR_PATH}:${EXT}/libz3java.so
MACHINES=${SOCIALITE_PREFIX}/conf/machines
#MACHINES1=${SOCIALITE_PREFIX}/conf/machines1
#generate mpj hosts
#echo "localhost" > $MACHINES
#echo "$HOSTNAME" > $MACHINES
#echo "$HOSTNAME slots=1" > $MACHINES1

#while IFS='' read -r line || [[ -n "$line" ]]; do
#    # if master as worker, start worker locally
#    if [ ${line} != $HOSTNAME ]; then
#        echo "$line" >> $MACHINES
#        echo "$line slots=1" >> $MACHINES1
#    fi
#done < "${SOCIALITE_PREFIX}/conf/machines"

mkdir $SOCIALITE_PREFIX/logs 2> /dev/null

MACHINES_NUM=$(cat ${MACHINES} | grep -c '[^[:space:]]')
MASTER_HOST=$(head -n 1 ${MACHINES})
if [ ${MASTER_HOST} != ${HOSTNAME} ]; then
    echo "error: please fix conf/machines or run this script on master machine"
    exit 1
fi
