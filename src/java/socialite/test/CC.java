package socialite.test;

import org.apache.commons.lang3.time.StopWatch;
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

public class CC {
    private static final Log L = LogFactory.getLog(CC.class);

    //dateset        node
    //livejournal    4847571
    //google         875713
    //berkstan       685230
    //0          1              2             3       4
    //single   node-count   node-path   edge-path
    //dist     node-count   node-path   edge-path   iter
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        if (args[0].equals("single")) {
            STGroup stg = new MySTGroupFile(CC.class.getResource("CC.stg"),
                    "UTF-8", '<', '>');
            stg.load();
            ST st = stg.getInstanceOf("Init");
            LocalEngine en = new LocalEngine();
            st.add("N", Integer.valueOf(args[1]));
            st.add("NPATH", args[2]);
            st.add("PATH", args[3]);
            st.add("SPLITTER", "\t");
            String init = st.render();
            L.info(init);
            en.run(init);
            st = stg.getInstanceOf("Iter");
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            String iterCode = st.render();
            en.run(iterCode);
            stopWatch.stop();
            L.info("recursive statement:" + stopWatch.getTime());
//            en.run("?- Comp(id, cid).", new QueryVisitor() {
//                @Override
//                public boolean visit(Tuple _0) {
//                    return super.visit(_0);
//                }
//            });
            en.shutdown();
        } else if (args[0].equals("dist")) {
            //TODO DIST CC has wrong result
            STGroup stg = new MySTGroupFile(CC.class.getResource("CC1.stg"),
                    "UTF-8", '<', '>');
            stg.load();
            ST st = stg.getInstanceOf("Init");
            st.add("N", Integer.valueOf(args[1]));
            st.add("NPATH", args[2]);
            st.add("PATH", args[3]);
            st.add("SPLITTER", "\t");
            int iter = Integer.parseInt(args[4]);
            String init = st.render();
            System.out.println(init);
            MasterNode.startMasterNode();
            while (!MasterNode.getInstance().allOneLine())//waiting workers online
                Thread.sleep(100);
            ClientEngine en = new ClientEngine();
            en.run(init);
            st = stg.getInstanceOf("Iter");
            StopWatch stopWatch = new StopWatch();
            for(int i=0;i<iter;i++) {
                stopWatch.reset();
                stopWatch.start();
                st.add("i",i);
                String iterCode = st.render();
                st.remove("i");
                en.run(iterCode);
                stopWatch.stop();
            }
            L.info("recursive statement:" + stopWatch.getTime());
            en.run("?- Comp(id, 0, cid).", new QueryVisitor() {
                @Override
                public boolean visit(Tuple _0) {
                    return super.visit(_0);
                }
            }, 0);
            en.shutdown();
        }
    }

}
