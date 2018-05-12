#!/usr/bin/env bash

mpiexec --mca btl_tcp_if_include eth0 -n 4 -hostfile ~/machines PowerGraph/release/toolkits/graph_analytics/pagerank --powerlaw=100000
mpiexec --mca btl_tcp_if_include eth0 -hostfile ~/machines \
~/PowerGraph/release/toolkits/graph_analytics/pagerank \
--graph /home/gengl/Datasets/PageRank/Google/edge.txt \
--format tsv \
--iterations 42 \
--ncpus 4 \
--engine synchronous

mpiexec --mca btl_tcp_if_include eth0 \
~/PowerGraph/release/toolkits/graph_analytics/pagerank \
--graph /home/gengl/Datasets/PageRank/Google/edge.txt \
--format tsv \
--iterations 42 \
--ncpus 4 \
--engine synchronous
