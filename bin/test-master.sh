#!/usr/bin/env bash
BIN=`dirname "$0"`
BIN=`cd "$BIN"; pwd`

. ${BIN}/common.sh
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
java -Xmx2G -Dsocialite.worker.num=2 -Dsocialite.master=master -Dlog4j.configuration=file:${SOCIALITE_PREFIX}/conf/log4j.properties -cp ${CODE_CLASSPATH}:${JAR_PATH} socialite.test.TestSocialite