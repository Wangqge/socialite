package socialite.async.test;

import socialite.codegen.Analysis;
import socialite.engine.ClientEngine;
import socialite.engine.LocalEngine;
import socialite.parser.Parser;

public class ParserTest {
    public static void main(String[] args) {
        String program = "Path(int n:0..875712, int dist, int prev).\n" +
                "Edge(int src:0..875712, (int sink, int len)).\n" +
                "Edge(s,t,cnt) :- l=$read(\"hdfs://master:9000/Datasets/SSSP/Google/edge.txt\"), (s1,s2,s3)=$split(l, \"\t\"), s=$toInt(s1), t=$toInt(s2), cnt=$toInt(s3).\n" +
                "Path(n, $min(d), prev) :- n=0, d=0, prev=-1 ;\n" +
                "                       :- Path(s, d1, prev1), Edge(s, n, weight), d=d1+weight, prev=s.";
//        LocalEngine localEngine = new LocalEngine();
//        localEngine.run(program);
//        localEngine.shutdown();
        Parser parser = new Parser();
        parser.parse(program);
        Analysis analysis = new Analysis(parser);
        analysis.run();
//        ClientEngine clientEngine = new ClientEngine();
//        clientEngine.run(program);
//        clientEngine.shutdown();
    }
}
