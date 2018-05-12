package socialite.test;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import socialite.dist.master.MasterNode;
import socialite.engine.ClientEngine;
import socialite.engine.LocalEngine;
import socialite.util.MySTGroupFile;

import java.io.FileNotFoundException;

public class SSSP {
    public static final int SRC_NODE = 0;
    private static final Log L = LogFactory.getLog(SSSP.class);

    //dateset        node         iter   distSum
    //livejournal    4847571      16     2243012525
    //google         875713       26     590940903
    //berkstan       685230       356
    //0            1                2
    //single       node-num        edge-path
    //dist         node-num        edge-path
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        if (args[0].equals("single")) {
            STGroup stg = new MySTGroupFile(SSSP.class.getResource("SSSP.stg"),
                    "UTF-8", '<', '>');
            stg.load();
            LocalEngine en = new LocalEngine();
            int nodeCount = Integer.parseInt(args[1]);
            ST st = stg.getInstanceOf("Init");
            st.add("N", nodeCount);
            st.add("PATH", args[2]);
            st.add("SPLITTER", "\t");
            String init = st.render();
            System.out.println(init);

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            en.run(init);
            stopWatch.stop();
            L.info("elapsed " + stopWatch.getTime());
//            en.run("?- Path(n, dist, x).", new QueryVisitor() {
//                @Override
//                public boolean visit(Tuple _0) {
//                    return super.visit(_0);
//                }
//            });
            en.shutdown();
        } else if (args[0].equals("dist")) {
            STGroup stg = new MySTGroupFile(SSSP.class.getResource("SSSP.stg"),
                    "UTF-8", '<', '>');
            stg.load();
            MasterNode.startMasterNode();
            while (!MasterNode.getInstance().allOneLine())//waiting workers online
                Thread.sleep(100);
            ClientEngine en = new ClientEngine();
            int nodeCount = Integer.parseInt(args[1]);
            ST st = stg.getInstanceOf("Init");
            st.add("N", nodeCount);
            st.add("PATH", args[2]);
            st.add("SPLITTER", "\t");
            String init = st.render();
            en.run(init);
//            en.run("?- Path(n, 0, dist).", new QueryVisitor() {
//                @Override
//                public boolean visit(Tuple _0) {
//                    return super.visit(_0);
//                }
//            }, 0);
            en.shutdown();
        }
        System.exit(0);
    }
}
