package socialite.async.util;
import com.intellij.util.containers.ConcurrentHashMap;
import socialite.async.atomic.*;


import io.protostuff.*;

import org.apache.commons.lang3.time.StopWatch;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.Delegate;
import io.protostuff.runtime.RuntimeEnv;
import io.protostuff.runtime.RuntimeSchema;
import socialite.async.atomic.MyAtomicDouble;
import socialite.util.ByteBufferOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import java.sql.Timestamp;

/**
 * Created by zhangzh on 2017/2/20.
 */
public class SerializeTool {
    private static final Log L = LogFactory.getLog(SerializeTool.class);
    private Schema schema=null;
    private int initsize;
    private SerializeMid serializeMidrec;
    private SerializeMid serializeMidsed;


//prot

    private final static Delegate<MyAtomicDouble>  TIMESTAMP_DELEGATE = new AtomicDelegate();

    private final static DefaultIdStrategy                      idStrategy         = ((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY);

    private final static ConcurrentHashMap<Class<?>, Schema<?>> cachedSchema       = new ConcurrentHashMap<>();

    static {
        idStrategy.registerDelegate(TIMESTAMP_DELEGATE);
    }


    public static <T> Schema<T> getSchema(Class<T> klass) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(klass);
        if (schema == null) {
            System.out.println("****************************8"+klass.getName());
            schema = RuntimeSchema.createFrom(klass, idStrategy);
            cachedSchema.put(klass, schema);
        }
        return schema;
    }



    public SerializeTool() {
        serializeMidsed=new SerializeMid();
        serializeMidrec=new SerializeMid();
    }
    public ByteBuffer toByteBuffer(int buffsize,Object object){
        //serializeMid.setObject(object);
        Schema schemas = getSchema(SerializeMid.class);
        //serializeMidsed.setObject(object);
        byte tmp[]=toBytes(buffsize,object);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffsize);

        byteBuffer.put(tmp,0,tmp.length);



        return byteBuffer;

    }
    public ByteBuffer toByteBuffer(Object object){
                return  toByteBuffer(initsize,object);

    }

    public <T> T fromByteBuffer(ByteBuffer byteBuffer, Class<T> klass) {

       // byteBuffer.flip();
        byte []  tmp=new byte[byteBuffer.remaining()];
        byteBuffer.get(tmp,0,tmp.length);

        return fromBytes(tmp,klass);
    }
    public byte[] toBytes(int buffsize,Object object){


        serializeMidsed.setObject(object);
        Schema schemas = getSchema(serializeMidsed.getClass());

        return ProtostuffIOUtil.toByteArray(serializeMidsed,schemas,LinkedBuffer.allocate(buffsize));

    }
    public byte[] toBytes(Object object) {

        return toBytes(initsize, object);
    }

    public <T> T fromBytes(byte[] data, Class<T> klass){
       // T obj = null;

            Schema schemas = getSchema(SerializeMid.class);
            serializeMidrec=(SerializeMid) schemas.newMessage();
            ProtostuffIOUtil.mergeFrom(data,serializeMidrec,schemas);
        return (T)(serializeMidrec.getObject());

    }

    public Object fromBytesToObject(byte[] data, Class<?> klass) {
        Object obj=null;
        try {
            obj = klass.newInstance();
            Schema schemas = RuntimeSchema.getSchema(klass);
            ProtostuffIOUtil.mergeFrom(data,obj,schemas);
            // ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static class Builder{
        private Schema schema;
        private int initsize=32*1024*1024;
        public Builder(){

        }
        public Builder registry(Class<?> klass){
            schema=RuntimeSchema.getSchema(klass);
            return this;
        }
        public SerializeTool.Builder setInitSize(int initSize) {
            this.initsize = initSize;
            return this;
        }

        public Builder setSerializeTransient(boolean enable) {

            //  kryo.getFieldSerializerConfig().
            return this;
        }
        public SerializeTool build(){
            SerializeTool serializeToolHP = new SerializeTool();
            serializeToolHP.schema = schema;
            serializeToolHP.initsize = initsize;
            return serializeToolHP;
        }


    }


    public static void main(String[] args) {


        SerializeTool serializeToolHP = new SerializeTool.Builder()
                .setSerializeTransient(true) //!!!!!!!!!!AtomicDouble's value field is transient
                .build();

        SerializeTool serializeTool = new SerializeTool.Builder().setInitSize(256).build();
        sertest test = new sertest();
        Test  k=new Test();
        Test  n=null;
        test.setD(3);
        test.setS(3.5);
        sertest test1=null;// = new sertest();
        System.out.println(test.getD()+" "+test.getS());
        byte[] data = new byte[10];
        byte[] data1 = new byte[10];
            data = serializeTool.toBytes(k);
           n = serializeTool.fromBytes(data,Test.class);
            System.out.println(n.s.getS());
            //System.out.println(resobj[0].getD()+" "+resobj[0].getS());



            for (int j=0;j<data.length;j++){
                System.out.print(data[j]);
            }
            Timestamp s=null;
            //System.out.println((new Timestamp(0)).);
        System.out.println();
            sertest test2=null;
            test.setD(3);
            System.out.println(test.getD()+" "+test.getS());
            ByteBuffer yteBuffer = serializeTool.toByteBuffer(256,test);

            //data1=serializeTool.toBytes(256,test);
//        data1=yteBuffer.array();
        for (int j=0;j<data1.length;j++){
            System.out.print(data1[j]);
        }
        System.out.println();

          //  yteBuffer.flip();
            //test2 = serializeTool.fromBytes(data1, sertest.class);

       // System.out.println(serializeTool.fromByteBuffer(serializeTool.toByteBuffer(test), sertest.class));
    }


}

