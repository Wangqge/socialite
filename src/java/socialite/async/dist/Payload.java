package socialite.async.dist;

import socialite.async.AsyncConfig;

import java.util.LinkedHashMap;

public class Payload {
    private LinkedHashMap<String, byte[]> byteCodes;
    private String recTableName;
    private String edgeTableName;
    private String extraTableName;
    private AsyncConfig asyncConfig;

    private Payload() {
    }

    public Payload(AsyncConfig asyncConfig, LinkedHashMap<String, byte[]> byteCodes) {
        this.asyncConfig = asyncConfig;
        this.byteCodes = byteCodes;
    }

    public void setRecTableName(String recTableName) {
        this.recTableName = recTableName;
    }

    public void setEdgeTableName(String edgeTableName) {
        this.edgeTableName = edgeTableName;
    }

    public void setExtraTableName(String extraTableName) {
        this.extraTableName = extraTableName;
    }

    public String getRecTableName() {
        return recTableName;
    }


    public String getExtraTableName() {
        return extraTableName;
    }

    public String getEdgeTableName() {
        return edgeTableName;
    }

    public LinkedHashMap<String, byte[]> getByteCodes() {
        return byteCodes;
    }

    public AsyncConfig getAsyncConfig() {
        return asyncConfig;
    }
}
