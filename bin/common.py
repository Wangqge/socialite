#!/usr/bin/python
# -*- coding:utf-8 -*-
import os
import re
import socket

SOCIALITE_PREFIX = os.getenv('SOCIALITE_PREFIX')
JAVA_HOME = os.getenv('JAVA_HOME')
MPI_HOME = os.getenv("MPI_HOME")
# SOCIALITE_PREFIX = '/home/gongsf/socialite'
# JAVA_HOME = '/home/gongsf/jdk1.8.0_144'
# MPI_HOME = '/home/gongsf/openmpi-2.1.2'

MACHINE_FILE = SOCIALITE_PREFIX + '/conf/machines'
CLASS_PATH_LIST = []
ENTRY_CLASS_PATH = SOCIALITE_PREFIX + '/classes/socialite.jar'
HOST_NAME = socket.gethostname()
MASTER_HOSTNAME = None
WORKER_HOSTNAME_LIST = []
WORKER_NUM = 0
HEAP_SIZE = 7000
THREAD_NUM = 4

error_var = None

if SOCIALITE_PREFIX is None:
    error_var = "SOCIALITE_PREFIX"
elif JAVA_HOME is None:
    error_var = "JAVA_HOME"
elif MPI_HOME is None:
    error_var = "MPI_HOME"
if error_var is not None:
    raise EnvironmentError('CAN NOT FOUND ENVIRONMENT VARIABLE: %s' % error_var)

if not os.path.exists(MACHINE_FILE):
    raise IOError('Can not found machine file in %s' % MACHINE_FILE)

with open(MACHINE_FILE, 'r') as fi:
    regex = re.compile('(.+?)\s+slots=(\d+)\n?')
    for line in fi:
        match = regex.search(line)
        if match is not None:
            host_name = match.groups()[0]
            slots = int(match.groups()[1])
            if MASTER_HOSTNAME is None:
                MASTER_HOSTNAME = host_name
                if slots > 1:
                    WORKER_HOSTNAME_LIST.append(host_name)
                    WORKER_NUM += slots - 1
            else:
                WORKER_HOSTNAME_LIST.append(host_name)
                WORKER_NUM += slots
        else:
            raise EnvironmentError('Error Line:%s' % line)

if HOST_NAME != MASTER_HOSTNAME:
    raise EnvironmentError(
        '%s != %s\n"Run this script in master node or correct the [conf/machines] file"' % (HOST_NAME, MASTER_HOSTNAME))


def add_class_path(root):
    file_name_list = os.listdir(root)
    for file_name in file_name_list:
        path = root + '/' + file_name
        if os.path.isdir(path):
            add_class_path(path)
        else:
            CLASS_PATH_LIST.append(path)


add_class_path(SOCIALITE_PREFIX + '/' + 'ext')
CLASS_PATH_LIST.append(ENTRY_CLASS_PATH)
class_path = ':'.join(CLASS_PATH_LIST)
