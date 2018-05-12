package socialite.async.test;

import socialite.dist.master.MasterNode;
import socialite.engine.ClientEngine;
import socialite.parser.Table;
import socialite.resource.SRuntimeWorker;
import socialite.resource.TableInstRegistry;
import socialite.tables.QueryVisitor;
import socialite.tables.TableInst;
import socialite.tables.Tuple;
import socialite.visitors.VisitorImpl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ForMaster {

    //-ea -Dsocialite.master=master -Dsocialite.worker.num=1 -Dsocialite.output.dir=gen -Dlog4j.configuration=file:/home/gengl/socialite/conf/log4j.properties
    public static void main(String[] args) throws InterruptedException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        LocalEngine localEngine = new LocalEngine();
//        localEngine.run("edge1(int src:0..875712, (int dst)).\n" +
//                "edge2(int src:0..875712, (int dst)).\n" +
//                "edge_join(int src:0..875712, (int dst)).\n" +
//                "edge1(s,t) :- l=$read(\"hdfs://master:9000/Datasets/PageRank/Google/edge.txt\"),(s1, s2)=$split(l, \"\t\"),s=$toInt(s1),t=$toInt(s2).\n" +
//                "edge2(s,t) :- edge1(s, t).\n" +
//                "edge_join(src, dst) :- edge1(src, d), edge2(d, dst).");
//        {
//            int[] tmp = new int[1];
//            long[] sum = new long[1];
//            localEngine.run("?- edge_join(s, t).", new QueryVisitor() {
//                @Override
//                public boolean visit(Tuple _0) {
//                    sum[0] += _0.getInt(0);
//                    synchronized (tmp) {
//                        tmp[0]++;
//                    }
//                    return true;
//                }
//            });
//            System.out.println(tmp[0]);
//        }
//        localEngine.shutdown();
//        System.exit(0);
        //-Dsocialite.master=gengl -Dsocialite.worker.num=1 -Dlog4j.configuration=file:/home/gongsf/socialite/conf/log4j.properties -Dsocialite.output.dir=gen
        //~/socialite/examples/prog1.dl
        MasterNode.startMasterNode();
        while (!MasterNode.getInstance().allOneLine())
            Thread.sleep(100);
        ClientEngine clientEngine = new ClientEngine();
//        clientEngine.run("edge(int src, int dst).");
//        clientEngine.run("edge(int src, int dst).");
//        clientEngine.run("edge(s,t) :- l=$read(\"hdfs://master:9000/Datasets/PageRank/wikipedia_link_en/edge_pair.txt\"),(s1, s2)=$split(l, \"\t\"),s=$toInt(s1),t=$toInt(s2).");
//        clientEngine.run("edge(s,t) :- l=$read(\"hdfs://master:9000/Datasets/PageRank/Google/edge.txt\"),(s1, s2)=$split(l, \"\t\"),s=$toInt(s1),t=$toInt(s2).\n" );
//        clientEngine.run("edge(int src:0..875712, (int dst)).\n" +
//                "edge(s,t) :- l=$read(\"hdfs://master:9000/Datasets/PageRank/Google/edge.txt\"),(s1, s2)=$split(l, \"\t\"),s=$toInt(s1),t=$toInt(s2).\n");

//        String prog = "Node(int n:0..875712).\n" +
//                "Rank(int n:0..875712, double rank).\n" +
//                "Edge(int n:0..875712, (int t)).\n" +
//                "EdgeCnt(int n:0..875712, int cnt).\n" +
//                "Edge(s, t) :- l=$read(\"hdfs://master:9000/Datasets/PageRank/Google/edge.txt\"), (s1,s2)=$split(l, \"\t\"), s=$toInt(s1), t=$toInt(s2).\n" +
//                "Node(n) :- l=$read(\"hdfs://master:9000/Datasets/PageRank/Google/node.txt\"), n=$toInt(l).\n" +
//                "EdgeCnt(s, $inc(1)) :- Edge(s, t).\n" +
//                "Rank(n, r) :- Node(n), r = 0.2 / 875713.\n" +
//                "Rank(y, $dsum(r1)) :- Rank(x, r), Edge(x, y),  EdgeCnt(x, d), r1 = 0.8 * r / d.";
//        String prog = "Path(int n:0..875712, int dist, int prev).\n" +
//                "Edge(int src:0..875712, (int sink, int len)).\n" +
//                "Edge(s,t,cnt) :- l=$read(\"hdfs://master:9000/Datasets/SSSP/Google/edge.txt\"), (s1,s2,s3)=$split(l, \"\t\"), s=$toInt(s1), t=$toInt(s2), cnt=$toInt(s3).\n" +
//                "Path(n, $min(d), prev) :- n=0, d=0, prev=-1 ;\n" +
//                "                       :- Path(s, d1, prev1), Edge(s, n, weight), d=d1+weight, prev=s.";
//        clientEngine.run(prog);
//        clientEngine.run("?- Path(s, d, x).", new QueryVisitorForSave(), 0);
        clientEngine.run("Edge(int n:0..875712, (int t)).\n" +
                "Edge(s, t) :- l=$read(\"hdfs://master:9000/Datasets/PageRank/Google/edge.txt\"), (s1,s2)=$split(l, \"\t\"), s=$toInt(s1), t=$toInt(s2).\n");


        clientEngine.test();
//        clientEngine.run("edge1(int src:0..875712, (int dst)).\n" +
//                "edge2(int src:0..875712, (int dst)).\n" +
//                "edge_join(int src:0..875712, (int dst)).\n" +
//                "edge1(s,t) :- l=$read(\"hdfs://master:9000/Datasets/PageRank/Google/edge.txt\"),(s1, s2)=$split(l, \"\t\"),s=$toInt(s1),t=$toInt(s2).\n" +
//                "edge2(s,t) :- edge1(s, t).\n" +
//                "edge_join(src, dst) :- edge1(src, d), edge2(d, dst).");
//        {
//        int[] tmp = new int[1];
//        long[] sum = new long[1];
//        clientEngine.run("?- edge(s, t).", new QueryVisitor() {
//            @Override
//            public boolean visit(Tuple _0) {
//                sum[0] += _0.getInt(0);
//                synchronized (tmp) {
//                    tmp[0]++;
//                }
//                return true;
//            }
//        }, 0);
//        System.out.println(tmp[0]);
//        }
//        clientEngine.shutdown();

//        clientEngine.test();
//        AsyncMaster asyncMaster = new AsyncMaster(AsyncConfig.get().getDatalogProg());
//        asyncMaster.startMaster();
    }


    static class QueryVisitorForSave extends QueryVisitor {
        BufferedWriter writer;

        QueryVisitorForSave() {
            try {
                writer = new BufferedWriter(new FileWriter("/home/gengl/sssp_result.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean visit(Tuple _0) {
//            return super.visit(_0);
            try {
                writer.write(_0.toString() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    }
}
