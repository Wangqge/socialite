#!/usr/bin/env bash
while IFS='' read -r line || [[ -n "$line" ]]; do
    if [[ ${line} != ${HOSTNAME} ]]; then
        ssh -n ${USER}@${line} "kill -9 \$(ps aux|grep '[s]ocialite.master='|awk '{print \$2}') 2> /dev/null"
        ssh -n ${USER}@${line} "pkill -f 'MPJDaemon' 2> /dev/null"
    fi
done < "$1"