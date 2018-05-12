#!/usr/bin/python
# -*- coding:utf-8 -*-
from common import *
import sys

cmd = """%s/bin/java
 -Xmx%dm
 -ea
 -Dsocialite.master=%s
 -Dsocialite.worker.num=%d
 -Dlog4j.configuration=file:%s
 -Dsocialite.worker.num_threads=4
 -Dsocialite.output.dir=%s
 -classpath %s
 socialite.dist.worker.WorkerNode 2>&1 | tee %s""" % (
    JAVA_HOME,
    HEAP_SIZE, MASTER_HOSTNAME, WORKER_NUM, SOCIALITE_PREFIX + '/conf/log4j.properties',
    SOCIALITE_PREFIX + '/gen', class_path, SOCIALITE_PREFIX + "/logs/worker.log")
cmd = cmd.replace('\n', '')

if len(sys.argv) != 2:
    raise IOError("[usage: %s install/update/run]" % sys.argv[0])
if sys.argv[1] == 'install':
    os.system('tar -zcf /tmp/out_$USER.tar.gz -C %s out ext conf' % SOCIALITE_PREFIX)
    for worker_hostname in WORKER_HOSTNAME_LIST:
        if worker_hostname != MASTER_HOSTNAME:
            os.system('scp /tmp/out_$USER.tar.gz %s:/tmp' % worker_hostname)
            os.system('ssh -n %s "mkdir %s 2> /dev/null"' % (worker_hostname, SOCIALITE_PREFIX))
            os.system('ssh -n %s "mkdir %s/logs 2> /dev/null"' % (worker_hostname, SOCIALITE_PREFIX))
            os.system('ssh -n %s "tar zxf /tmp/out_$USER.tar.gz -C %s"' % (worker_hostname, SOCIALITE_PREFIX))
elif sys.argv[1] == 'update':
    os.system('tar -zcf /tmp/out_$USER.tar.gz -C %s out' % SOCIALITE_PREFIX)
    for worker_hostname in WORKER_HOSTNAME_LIST:
        if worker_hostname != MASTER_HOSTNAME:
            os.system('scp /tmp/out_$USER.tar.gz %s:/tmp' % worker_hostname)
            os.system('ssh -n %s "tar zxf /tmp/out_$USER.tar.gz -C %s"' % (worker_hostname, SOCIALITE_PREFIX))
elif sys.argv[1] == 'run':
    for worker_hostname in WORKER_HOSTNAME_LIST:
        os.system('ssh -n %s "pkill -f WorkerNode"''' % worker_hostname)
        os.system('ssh -f -n %s "%s"' % (worker_hostname, cmd))
