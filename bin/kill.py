#!/usr/bin/python
# -*- coding:utf-8 -*-
from common import *
import sys
import os


def kill_all():
    for WORKER_HOSTNAME in WORKER_HOSTNAME_LIST:
        os.system(
            """ssh -n ${USER}@%s "kill -9 $(ps aux|grep '[s]ocialite.master='|awk '{print $2}')" 2> /dev/null""" % (
                WORKER_HOSTNAME))
        print('killed on %s' % WORKER_HOSTNAME)


if __name__ == '__main__':
    kill_all()
