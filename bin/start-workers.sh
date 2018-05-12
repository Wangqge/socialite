#!/usr/bin/env bash
BIN=`dirname "$0"`
BIN=`cd "$BIN"; pwd`

. ${BIN}/common.sh

function start-node(){
    ${BIN}/kill-all.sh ${MACHINES}

    WORKER_CMD="java -Xmx6G -ea"
    WORKER_CMD+=" -Dsocialite.worker.num=$((MACHINES_NUM-1))"
    WORKER_CMD+=" -Dsocialite.output.dir=${SOCIALITE_PREFIX}/gen"
    WORKER_CMD+=" -Dsocialite.master=${MASTER_HOST}"
    WORKER_CMD+=" -Dlog4j.configuration=file:${SOCIALITE_PREFIX}/conf/log4j.properties"
    WORKER_CMD+=" -cp $1:${JAR_PATH} socialite.dist.worker.WorkerNode"

    while IFS='' read -r line || [[ -n "$line" ]]; do
        # if master as worker, start worker locally
        if [ ${line} != ${MASTER_HOST} ]; then
            ssh -n $USER@${line} "sh -c 'cd $SOCIALITE_PREFIX; nohup $WORKER_CMD > /dev/null 2>&1 &'"
        fi
    done < "${SOCIALITE_PREFIX}/conf/machines"
}

if [ "$#" != "1" ] || ([ "$1" != "-copy-jar" ] && [ "$1" != "-copy-classes" ]); then
    echo "please specify -copy-jar or -copy-classes"
    exit 1
else
    if [ "$1" == "-copy-jar" ]
    then
        CODE_CLASSPATH=${SOCIALITE_PREFIX}/classes/socialite.jar
        while IFS='' read -r line || [[ -n "$line" ]]; do
            if [ ${line} == ${MASTER_HOST} ]; then
                continue
            fi
            ssh -n ${USER}@${line} "mkdir $SOCIALITE_PREFIX/classes 2> /dev/null"
            scp ${CODE_CLASSPATH} ${USER}@${line}:"$SOCIALITE_PREFIX/classes/"
        done < "${MACHINES}"
    elif [ "$1" == "-copy-classes" ]; then
        CODE_CLASSPATH=${SOCIALITE_PREFIX}/out/production/socialite
        cd ${SOCIALITE_PREFIX}
        tar -zcf /tmp/out.tar.gz -C ${SOCIALITE_PREFIX} out conf examples
        while IFS='' read -r line || [[ -n "$line" ]]; do
            if [ ${line} == ${MASTER_HOST} ]; then
                continue
            fi
            scp /tmp/out.tar.gz ${USER}@${line}:"/tmp/out.tar.gz"
            ssh -n ${USER}@${line} "rm -rf ${SOCIALITE_PREFIX}/out 2> /dev/null && tar -zxf /tmp/out.tar.gz -C ${SOCIALITE_PREFIX}/ && rm /tmp/out.tar.gz"
        done < "${MACHINES}"
    fi
    start-node ${CODE_CLASSPATH}
fi