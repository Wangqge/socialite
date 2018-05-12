package socialite.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import socialite.dist.master.MasterNode;
import socialite.engine.ClientEngine;
import socialite.engine.LocalEngine;
import socialite.tables.QueryVisitor;
import socialite.tables.Tuple;
import socialite.util.MySTGroupFile;

import java.io.FileNotFoundException;

public class PageRank {
    //dateset        node         iter
    //livejournal    4847571      25
    //google         875713       28
    //berkstan       685230       30
    private static final Log L = LogFactory.getLog(PageRank.class);

    // 0       1             2       3
    //single node-count edge-path   iter-num
    //dist   node-count   edge-path iter-num
    //-Dsocialite.worker.num=2 -ea -Xmx512M -Dlog4j.configuration=file:/home/gengl/Desktop/gengl/socialite/conf/log4j.properties -Dsocialite.output.dir=gen
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        STGroup stg = new MySTGroupFile(PageRank.class.getResource("PageRank.stg"),
                "UTF-8", '<', '>');
        stg.load();
        if (args[0].equals("single")) {
            LocalEngine en = new LocalEngine();//config
            int nodeCount = Integer.parseInt(args[1]);

            ST st = stg.getInstanceOf("Init");
            st.add("N", nodeCount);
            st.add("PATH", args[2]);//web-BerkStan_fix
            String init = st.render();
            System.out.println(init);
            en.run(init);

            st = stg.getInstanceOf("Iter");
            st.add("N", nodeCount);
            long start = System.currentTimeMillis();
            for (int i = 0; i < Integer.parseInt(args[3]); i++) {
                st.add("i", i);
                String iterCode = st.render();
                st.remove("i");
                System.out.println("run "+iterCode);
                en.run(iterCode);
                System.out.println("iter:" + i);
            }
            System.out.println("recursive statement:" + (System.currentTimeMillis() - start));
//            en.run("?- Rank(n, 0, pr).", new QueryVisitor() {
//                @Override
//                public boolean visit(Tuple _0) {
//                    return super.visit(_0);
//                }
//            });
//            TIntFloatMap result = new TIntFloatHashMap();
//            double[] vals = new double[1];
//            en.run("?- Rank(n, 0, rank).", new QueryVisitor() {
//
//                @Override
//                public boolean visit(Tuple _0) {
//                    System.out.println(_0);
//                    vals[0] += _0.getDouble(2);
//                    return true;
//                }
//            });
//            System.out.println(vals[0]);
            en.shutdown();
        } else if (args[0].equals("dist")) {
            int nodeCount = Integer.parseInt(args[1]);//875713 berkstan 685230
            ST st = stg.getInstanceOf("Init");
            st.add("N", nodeCount);
            st.add("PATH", args[2]);
            String init = st.render();
            System.out.println(init);
            MasterNode.startMasterNode();
            while (!MasterNode.getInstance().allOneLine())//waiting workers online
                Thread.sleep(100);
            ClientEngine en = new ClientEngine();
            en.run(init);
            st = stg.getInstanceOf("Iter");
            st.add("N", nodeCount);
            long start = System.currentTimeMillis();
            for (int i = 0; i < Integer.parseInt(args[3]); i++) {
                st.add("i", i);
                String iterCode = st.render();
                st.remove("i");
                en.run(iterCode);
                System.out.println("iter:" + i);
            }
            L.info("recursive statement:" + (System.currentTimeMillis() - start));
            en.run("?- Rank(n, 0, pr).", new QueryVisitor() {
                @Override
                public boolean visit(Tuple _0) {
                    return super.visit(_0);
                }
            },0);
//            TIntFloatMap result = new TIntFloatHashMap();
//            double[] vals = new double[1];
//            en.run("?- Rank(n, 0, rank).", new QueryVisitor() {
//
//                @Override
//                public boolean visit(Tuple _0) {
//                    System.out.println(_0);
//                    vals[0] += _0.getDouble(2);
//                    return true;
//                }
//            }, 0);
//            System.out.println(vals[0]);
            en.shutdown();
        }
        System.exit(0);
    }
}
