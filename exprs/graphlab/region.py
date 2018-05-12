from graphlab import SFrame, SGraph, connected_components, pagerank, shortest_path
from graphlab import deploy
import os
os.environ['OMPNUMTHREADS'] = '32'
def PageRank():
    url = '/clueweb/PageRank/clueweb_20M/edge_pair.txt'
    data = SFrame.read_csv(url, delimiter='\t', header=False, column_type_hints=[int, int])
    graph = SGraph().add_edges(data, src_field='X1', dst_field='X2')
    pr_model = pagerank.create(graph, reset_probability=0.2, threshold=0.000000000001, max_iterations=42, _distributed=True)
    pr_model.summary()
ec2config = deploy.Ec2Config(region='us-east-2',
                              instance_type='c4.2xlarge',
                              aws_access_key_id='AKIAJTPZWRXASA6FI6LQ',
                              aws_secret_access_key='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')
ec2 = deploy.ec2_cluster.create(name='ec2', s3_path='s3://graphlabexpr/graphlab', ec2_config=ec2config,nu)
ec2.start()
job_ec2 = deploy.job.create(PageRank, environment=ec2)
job_ec2.get_results()
ec2.stop()