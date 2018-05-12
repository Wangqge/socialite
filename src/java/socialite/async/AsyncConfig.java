package socialite.async;

import com.sun.java.swing.plaf.gtk.GTKConstants;
import socialite.parser.Variable;
import socialite.util.Assert;
import socialite.util.SociaLiteException;

import java.lang.reflect.Array;
import java.nio.charset.CharsetEncoder;
import java.util.*;
import  java.io.*;
import java.util.stream.Collectors;

import socialite.async.analysis.Z3Analysis;

public class AsyncConfig {
    private static AsyncConfig asyncConfig;
    private int checkInterval = -1;
    private int messageTableWaitingInterval = -1;
    private double threshold;
    private CheckerType checkType;
    private Cond cond;
    private boolean dynamic;
    private PriorityType priorityType;
    private double schedulePortion;
    private EngineType engineType;
    private boolean debugging;
    private boolean networkInfo;
    private boolean MVCC;
    private int threadNum;
    private int initSize;
    private int messageTableUpdateThreshold;
    private String savePath;
    private boolean printResult;
    private String datalogProg;

    public static void updateEngineType(EngineType type){
        asyncConfig.engineType=type;
        if (type== EngineType.SYNC)
            asyncConfig.priorityType=PriorityType.NONE;
//CODE BY WANG

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nCHECK_COND:").append(checkType == CheckerType.DELTA ? "DELTA" : "VALUE").append(getSCond()).append(" ").append(threshold).append("\n");
        sb.append("CHECK_INTERVAL:").append(checkInterval).append("\n");

        sb.append("PRIORITY_TYPE:");
        if (priorityType == PriorityType.NONE)
            sb.append("NONE");
        else if (priorityType == PriorityType.LOCAL)
            sb.append("LOCAL");
        else if (priorityType == PriorityType.GLOBAL)
            sb.append("GLOBAL");
        sb.append("\n");
        sb.append("DYNAMIC:").append(dynamic ? "TRUE" : "FALSE").append("\n");
        sb.append("ENGINE_TYPE:");
        if (engineType == EngineType.SYNC)
            sb.append("SYNC");
        else if (engineType == EngineType.SEMI_ASYNC)
            sb.append("SEMI-ASYNC");
        else if (engineType == EngineType.ASYNC)
            sb.append("ASYNC");
        sb.append("\n");
        sb.append("NETWORK_INFO:").append(networkInfo ? "TRUE" : "FALSE").append("\n");
        sb.append("MVCC:").append(MVCC ? "TRUE" : "FALSE").append("\n");
        sb.append("THREAD_NUM:").append(threadNum).append("\n");
        sb.append("INIT_SIZE:").append(initSize).append("\n");
        sb.append("MESSAGE_UPDATE_THRESHOLD:").append(messageTableUpdateThreshold).append("\n");
        sb.append("MESSAGE_TABLE_WAITING_INTERVAL:").append(messageTableWaitingInterval).append("\n");
        sb.append("PRINT_RESULT:").append(printResult ? "TRUE" : "FALSE").append("\n");
        sb.append("SAVE_PATH:").append(savePath);
        return sb.toString();
    }


    private String getSCond() {
        switch (cond) {
            case G:
                return ">";
            case GE:
                return ">=";
            case E:
                return "==";
            case LE:
                return "<=";
            case L:
                return "<";
        }
        Assert.impossible();
        return null;
    }

    public static AsyncConfig get() {
        if (asyncConfig == null) {
            throw new SociaLiteException("AsyncConfig is not create");
        }
        return asyncConfig;
    }

    public static void set(AsyncConfig _asyncConfig) {
        asyncConfig = _asyncConfig;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public double getThreshold() {
        return threshold;
    }

    public CheckerType getCheckType() {
        return checkType;
    }

    public Cond getCond() {
        return cond;
    }

    public PriorityType getPriorityType() {
        return priorityType;
    }

    public EngineType getEngineType() {
        return engineType;
    }

    public boolean isNetworkInfo() {
        return networkInfo;
    }

    public boolean isMVCC() {
        return MVCC;
    }

    public double getSchedulePortion() {
        return schedulePortion;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public int getInitSize() {
        return initSize;
    }

    public int getMessageTableUpdateThreshold() {
        return messageTableUpdateThreshold;
    }

    public int getMessageTableWaitingInterval() {
        return messageTableWaitingInterval;
    }

    public String getSavePath() {
        if (savePath == null) return "";
        return savePath;
    }

    public String getDatalogProg() {
        return datalogProg;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public boolean isPrintResult() {
        return printResult;
    }

    public boolean isDebugging() {
        return debugging;
    }


    public enum Cond {
        G, GE, E, L, LE
    }

    public enum CheckerType {
        VALUE, DELTA, DIFF_VALUE, DIFF_DELTA
    }

    public enum PriorityType {
        NONE, LOCAL, GLOBAL
    }

    public enum EngineType {
        ASYNC, SEMI_ASYNC, SYNC, AUTO
    }

    public static class Builder {
        private int checkInterval = -1;
        private Double threshold = null;
        private CheckerType checkType;
        private Cond cond;
        private EngineType engineType;
        private PriorityType priorityType;
        private boolean dynamic;
        private boolean debugging;
        private int threadNum;
        private int initSize;
        private boolean MVCC;
        private boolean networkInfo;
        private double schedulePortion;
        private int messageTableUpdateThreshold;
        private int messageTableWaitingInterval = -1;
        private boolean printResult;
        private String datalogProg;
        private String savePath;

        public Builder setCheckerType(CheckerType checkType) {
            this.checkType = checkType;
            return this;
        }

        public Builder setCheckInterval(int checkInterval) {
            this.checkInterval = checkInterval;
            return this;
        }

        public Builder setThreshold(double threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder setCheckerCond(Cond cond) {
            this.cond = cond;
            return this;
        }

        public Builder setPriorityType(PriorityType priorityType) {
            this.priorityType = priorityType;
            return this;
        }

        public Builder setSchedulePortion(double schedulePortion) {
            this.schedulePortion = schedulePortion;
            return this;
        }

        public Builder setDynamic(boolean dynamic) {
            this.dynamic = dynamic;
            return this;
        }

        public Builder setEngineType(EngineType engineType) {
            this.engineType = engineType;
            return this;
        }

        public Builder setMVCC(boolean MVCC) {
            this.MVCC = MVCC;
            return this;
        }

        public Builder setNetworkInfo(boolean networkInfo) {
            this.networkInfo = networkInfo;
            return this;
        }

        public Builder setDebugging(boolean debugging) {
            this.debugging = debugging;
            return this;
        }

        public Builder setThreadNum(int threadNum) {
            this.threadNum = threadNum;
            return this;
        }

        public Builder setInitSize(int initSize) {
            this.initSize = initSize;
            return this;
        }

        public Builder setMessageTableUpdateThreshold(int messageTableUpdateThreshold) {
            this.messageTableUpdateThreshold = messageTableUpdateThreshold;
            return this;
        }

        public Builder setMessageTableWaitingInterval(int messageTableWaitingInterval) {
            this.messageTableWaitingInterval = messageTableWaitingInterval;
            return this;
        }

        public Builder setPrintResult(boolean printResult) {
            this.printResult = printResult;
            return this;
        }

        public Builder setSavePath(String savePath) {
            this.savePath = savePath;
            return this;
        }

        public Builder setDatalogProg(String datalogProg) {
            this.datalogProg = datalogProg;
            return this;
        }

        public AsyncConfig build() {
            AsyncConfig asyncConfig = new AsyncConfig();
            if (threshold == null)
                throw new SociaLiteException("threshold is not set");
            if (checkType == null)
                throw new SociaLiteException("check type is not set");
            if (cond == null)
                throw new SociaLiteException("condition is not set");
            asyncConfig.checkInterval = checkInterval;
            asyncConfig.threshold = threshold;
            asyncConfig.checkType = checkType;
            asyncConfig.cond = cond;
            asyncConfig.engineType = engineType;
            asyncConfig.schedulePortion = schedulePortion;
            asyncConfig.priorityType = priorityType;
            asyncConfig.dynamic = dynamic;
            asyncConfig.MVCC = MVCC;
            asyncConfig.networkInfo = networkInfo;
            asyncConfig.debugging = debugging;
            asyncConfig.threadNum = threadNum;
            asyncConfig.initSize = initSize;
            asyncConfig.messageTableUpdateThreshold = messageTableUpdateThreshold;
            asyncConfig.messageTableWaitingInterval = messageTableWaitingInterval;
            asyncConfig.savePath = savePath;
            asyncConfig.printResult = printResult;
            asyncConfig.datalogProg = datalogProg;
            if (AsyncConfig.asyncConfig != null)
                throw new SociaLiteException("AsyncConfig already built");
           // System.out.println(engineType.toString()+priorityType.toString());
            if ((engineType == EngineType.SYNC||engineType==EngineType.SEMI_ASYNC )&& priorityType != PriorityType.NONE)
                throw new SociaLiteException("CAN NOTE USE Priority with Sync/Semi-Async Mode");
            if (engineType != EngineType.ASYNC && MVCC)
                throw new SociaLiteException("CAN NOTE USE MVCC with Sync/Semi-Async Mode");
            AsyncConfig.asyncConfig = asyncConfig;
            return asyncConfig;
        }
    }

    private static ArrayList<Double> getconsts(String aggr,String nonaggr){
        char []ondo=aggr.toCharArray();
        ArrayList<Double> consts=new ArrayList<Double>();
        StringBuilder tmp=new StringBuilder();
        for (int i=0;i<ondo.length;i++){
            if (Character.isDigit(ondo[i])){
                if(i==0||!Character.isAlphabetic(ondo[i-1]))
                tmp.append(ondo[i]);
            }else{
                if(ondo[i]=='.'&& Character.isDigit(ondo[i-1])){
                    tmp.append(ondo[i]);
                }
                else
                if (tmp.length()!=0){
                    consts.add(Double.parseDouble(tmp.toString()));
                    tmp.delete(0,tmp.length());
                }
            }
        }
        if (tmp.length()!=0){
            consts.add(Double.parseDouble(tmp.toString()));
            tmp.delete(0,tmp.length());
        }
        ondo=nonaggr.toCharArray();
        for (int i=0;i<ondo.length;i++){
            if (Character.isDigit(ondo[i])){
                if(i==0||!Character.isAlphabetic(ondo[i-1]))
                tmp.append(ondo[i]);
            }else{
                if(ondo[i]=='.'&& Character.isDigit(ondo[i-1])){
                    tmp.append(ondo[i]);
                }
                else if (tmp.length()!=0){
                    System.out.println(tmp.toString());
                    consts.add(Double.parseDouble(tmp.toString()));
                    tmp.delete(0,tmp.length());
                }
            }
        }
        if (tmp.length()!=0){
            consts.add(Double.parseDouble(tmp.toString()));
            tmp.delete(0,tmp.length());
        }

        return consts;

    }
    private static ArrayList<String> getVariables(String aggr,String nonaggr){
        ArrayList<String>variables=new ArrayList<String>();
        ArrayList<String>op=new ArrayList<String>();
        op.add("+");op.add("-");op.add("*");op.add("/");op.add("(");op.add(")");op.add("=");
        String ondo =aggr+"="+nonaggr;
        StringBuilder tmp =new StringBuilder();
        for(int i=0;i<ondo.length();i++){
            if (ondo.substring(i,i+1).equals("$")){
                 i=ondo.substring(i).indexOf("(")-1;
                 continue;
            }else{
                if (!op.contains(ondo.substring(i,i+1))){
                    tmp.append(ondo.substring(i,i+1));
                }else{
                    if(tmp.length()>0)
                    variables.add(tmp.toString());
                    tmp.delete(0,tmp.length());
                }
            }
        }
        if(tmp.length()>0)
            variables.add(tmp.toString());
        tmp.delete(0,tmp.length());
        return variables;
    }
    public static void parse(String configContent) {
        Map<String, String> configMap = new LinkedHashMap<>();
        StringBuilder prog = new StringBuilder();
        configContent = configContent.trim();
        String splitter = configContent.contains("\r\n") ? "\r\n" : "\n";
        List<String> lines = new ArrayList<>();
        lines.addAll(Arrays.asList(configContent.split(splitter)));
        lines = lines.stream().map(String::trim).filter(s -> s.length() > 0 && !s.startsWith("#")).collect(Collectors.toList());
        int lineNo;
        for (lineNo = 0; lineNo < lines.size(); lineNo++) {
            String line = lines.get(lineNo);
            if (line.startsWith("RULE:")) {
                lineNo++;
                break;
            }
            String[] tmp = line.split("\\s*=\\s*");
            configMap.put(tmp[0], tmp[1]);
        }
        while (lineNo < lines.size()) {
            String curnt=null;


            if(lineNo==lines.size()-1){//假設如果最後一行是遞歸規則
                curnt=lines.get(lineNo);
                ArrayList<String>Variables;
                ArrayList<Double>Consts;
                String aggrfun=null;
                String nonaggrfun=null;
                aggrfun=curnt.substring(curnt.indexOf(",")+1,curnt.substring(0,curnt.indexOf(":-")).lastIndexOf(")"));
                aggrfun=aggrfun.replaceAll("\\s*","");
                nonaggrfun=curnt.substring(curnt.lastIndexOf("=")+1,curnt.lastIndexOf("."));
                nonaggrfun=nonaggrfun.replaceAll("\\s*","");
                Variables=getVariables(aggrfun,nonaggrfun);
                Consts=getconsts(aggrfun,nonaggrfun);
                System.out.println(aggrfun+"\n"+nonaggrfun);
                String[] variables=null;
                double[] consts=new double[Consts.size()];
                for (int i=0;i<Consts.size();i++){
                      consts[i]=Consts.get(i);//const list generate
                        if (Variables.contains(Consts.get(i).toString())){//remove the same item between variables and consts
                            Variables.remove(Consts.get(i).toString());
                        }
                }
                variables=new String[Variables.size()];
                for (int i=0;i<Variables.size();i++){
                     variables[i]=Variables.get(i);
                }//變量生成
                Z3Analysis z3Analysis=new Z3Analysis(variables,consts,nonaggrfun,aggrfun);

                System.out.println(lines.get(lineNo));
               // z3Analysis.chkconvertable();
               curnt= z3Analysis.test1(curnt);
               if (curnt==null){
                   System.out.println("cant conver");
                   curnt=curnt=lines.get(lineNo);
               }
            }
            else {
                curnt=lines.get(lineNo);
            }


            prog.append(curnt).append("\n");
            lineNo++;
        }
        AsyncConfig.Builder asyncConfig = new AsyncConfig.Builder();
        configMap.forEach((key, val) -> {
            switch (key) {
                case "CHECK_INTERVAL":
                    asyncConfig.setCheckInterval(Integer.parseInt(val));
                    break;
                case "CHECK_TYPE":
                    switch (val) {
                        case "VALUE":
                            asyncConfig.setCheckerType(CheckerType.VALUE);
                            break;
                        case "DELTA":
                            asyncConfig.setCheckerType(CheckerType.DELTA);
                            break;
                        case "DIFF_VALUE":
                            asyncConfig.setCheckerType(CheckerType.DIFF_VALUE);
                            break;
                        case "DIFF_DELTA":
                            asyncConfig.setCheckerType(CheckerType.DIFF_DELTA);
                            break;
                        default:
                            throw new SociaLiteException("unknown check type: " + val);
                    }
                    break;
                case "CHECK_COND":
                    switch (val) {
                        case "G":
                            asyncConfig.setCheckerCond(Cond.G);
                            break;
                        case "GE":
                            asyncConfig.setCheckerCond(Cond.GE);
                            break;
                        case "E":
                            asyncConfig.setCheckerCond(Cond.E);
                            break;
                        case "LE":
                            asyncConfig.setCheckerCond(Cond.LE);
                            break;
                        case "L":
                            asyncConfig.setCheckerCond(Cond.L);
                            break;
                        default:
                            throw new SociaLiteException("unknown check condition: " + val);
                    }
                    break;
                case "CHECK_THRESHOLD":
                    asyncConfig.setThreshold(Double.parseDouble(val));
                    break;
                case "PRIORITY_TYPE":
                    if (val.equals("NONE"))
                        asyncConfig.setPriorityType(PriorityType.NONE);
                    else if (val.equals("LOCAL"))
                        asyncConfig.setPriorityType(PriorityType.LOCAL);
                    else if (val.equals("GLOBAL"))
                        asyncConfig.setPriorityType(PriorityType.GLOBAL);
                    else throw new SociaLiteException("unknown val: " + val);
                    break;
                case "NETWORK_INFO":
                    if (val.equals("TRUE"))
                        asyncConfig.setNetworkInfo(true);
                    else if (val.equals("FALSE"))
                        asyncConfig.setNetworkInfo(false);
                    else throw new SociaLiteException("unknown val: " + val);
                    break;
                case "MVCC":
                    if (val.equals("TRUE"))
                        asyncConfig.setMVCC(true);
                    else if (val.equals("FALSE"))
                        asyncConfig.setMVCC(false);
                    else throw new SociaLiteException("unknown val: " + val);
                    break;
                case "ENGINE_TYPE":
                    if (val.equals("ASYNC"))
                        asyncConfig.setEngineType(EngineType.ASYNC);
                    else if (val.equals("SEMI-ASYNC"))
                        asyncConfig.setEngineType(EngineType.SEMI_ASYNC);
                    else if (val.equals("SYNC"))
                        asyncConfig.setEngineType(EngineType.SYNC);
                    else if (val.equals("AUTO"))
                        asyncConfig.setEngineType(EngineType.AUTO);
                    else throw new SociaLiteException("unknown val: " + val);
                    break;
                case "SCHEDULE_PORTION":
                    asyncConfig.setSchedulePortion(Double.parseDouble(val));
                    break;
                case "DYNAMIC":
                    if (val.equals("TRUE"))
                        asyncConfig.setDynamic(true);
                    else if (val.equals("FALSE"))
                        asyncConfig.setDynamic(false);
                    else throw new SociaLiteException("unknown val: " + val);
                    break;
                case "PRINT_RESULT":
                    if (val.equals("TRUE"))
                        asyncConfig.setPrintResult(true);
                    else if (val.equals("FALSE"))
                        asyncConfig.setPrintResult(false);
                    else throw new SociaLiteException("unknown val: " + val);
                    break;
                case "THREAD_NUM":
                    asyncConfig.setThreadNum(Integer.parseInt(val));
                    break;
                case "INIT_SIZE":
                    asyncConfig.setInitSize(Integer.parseInt(val));
                    break;
                case "MESSAGE_TABLE_UPDATE_THRESHOLD":
                    asyncConfig.setMessageTableUpdateThreshold(Integer.parseInt(val));
                    break;
                case "MESSAGE_TABLE_WAITING_INTERVAL":
                    asyncConfig.setMessageTableWaitingInterval(Integer.parseInt(val));
                    break;
                case "SAVE_PATH":
                    val = val.replace("\"", "");
                    asyncConfig.setSavePath(val);
                    break;
                case "DEBUGGING":
                    if (val.equals("TRUE"))
                        asyncConfig.setDebugging(true);
                    else if (val.equals("FALSE"))
                        asyncConfig.setDebugging(false);
                    else throw new SociaLiteException("unknown val: " + val);
                    break;
                default:
                    throw new SociaLiteException("unknown option:" + key);
            }
        });
        asyncConfig.setDatalogProg(prog.toString());
        asyncConfig.build();
    }
}
