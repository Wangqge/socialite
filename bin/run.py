#!/usr/bin/python
# -*- coding:utf-8 -*-
from common import *
from kill import kill_all
import sys

if len(sys.argv) != 2:
    raise IOError("Datalog Program is nedded")

PROG_PATH = sys.argv[1]
if not os.path.exists(PROG_PATH):
    raise IOError("Invalid program path %s" % PROG_PATH)

cmd = """LD_LIBRARY_PATH=/home/wangqg/z3/build/ %s/bin/mpirun
 --prefix %s
 --machinefile %s
 --mca btl_tcp_if_include eno1
 --mca btl ^openib
 %s/bin/java
 -Xmx%dm
 -Dsocialite.master=%s
 -Dsocialite.worker.num=%d
 -Dlog4j.configuration=file:%s
 -Djava.library.path=/opt/openmpi-3.0.0/lib:/home/wangqg/z3/build
 -Dsocialite.worker.num_threads=4
 -Dsocialite.output.dir=%s
 -classpath %s
 socialite.async.DistEntry %s 2>&1 | tee %s""" % (
    MPI_HOME,MPI_HOME, MACHINE_FILE, JAVA_HOME,
    HEAP_SIZE, MASTER_HOSTNAME, WORKER_NUM, SOCIALITE_PREFIX + '/conf/log4j.properties',
    SOCIALITE_PREFIX + '/gen', class_path, PROG_PATH, SOCIALITE_PREFIX + "/logs/master.log")
cmd = cmd.replace('\n', '')
kill_all()
os.system(cmd)
