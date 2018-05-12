package socialite.async.test;

import socialite.resource.PartitionNodeMap;
import socialite.util.BitUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LoadTest {
    public static void main(String[] args) throws IOException {
//        LoadTest loadTest = new LoadTest();
//        loadTest.init(4, 4);
//        for (int i = 0; i < 875713; i++)
//        System.out.println(loadTest.machineIndexForHash(424959));
//        MasterNode.startMasterNode();
//        while (!MasterNode.getInstance().allOneLine()) ;
//        ClientEngine clientEngine = new ClientEngine();
//        clientEngine.run("Edge(int s, int t).");
//        clientEngine.run("Edge(s, t) :- l=$read(\"/home/gongsf/socialite/examples/prog2_edge.txt\"), (s1,s2)=$split(l, \"\t\"),\n" +
//                "             s=$toInt(s1), t=$toInt(s2).");
//        clientEngine.shutdown();
//        LocalEngine localEngine = new LocalEngine();
//        localEngine.run("Edge(int x, (int y)).");
//        localEngine.run("Edge(s, t) :- l=$read(\"/home/gengl/socialite/examples/prog2_edge.txt\"), (s1,s2)=$split(l, \"\t\"),\n" +
//                "             s=$toInt(s1), t=$toInt(s2).");
        BufferedReader reader = new BufferedReader(new FileReader("/home/gengl/Range/edge_result_0.txt"));
        String line;
        int last = -1;
        while ((line = reader.readLine()) != null) {
            String[] tmp = line.split("\\s+");
            int src = Integer.parseInt(tmp[0]);
            int dst = Integer.parseInt(tmp[1]);
            if (last != -1)
                if (src - last > 1)
                    throw new ArrayIndexOutOfBoundsException(String.format("error node:%d last:%d", src, last));
            last = src;
        }
        reader.close();
    }


    private int maskForHashIndex;
    private PartitionNodeMap partitionNodeMap;

    public void init(int numWorkers, int threadNum) {
        int maxNumWorkers = numWorkers * 4;
        int partitionNum = threadNum * 8;
        int defaultPartitionNum = BitUtils.nextHighestPowerOf2(partitionNum);
        int totalPartitionNum = maxNumWorkers * defaultPartitionNum;
        assert BitUtils.isPowerOf2(totalPartitionNum);
        maskForHashIndex = totalPartitionNum - 1;

        int nodeNum = numWorkers;
        partitionNodeMap = PartitionNodeMap.create(nodeNum, totalPartitionNum);
    }

    public int machineIndexForHash(int hash) {
        hash = hash >> 12;
        if (hash < 0) {
            hash = -hash;
            if (hash == Integer.MIN_VALUE) {
                hash = 0;
            }
        }
        int partitionIdx = hash & maskForHashIndex;
        return partitionNodeMap.node(partitionIdx);
    }
}
