//package socialite.async.util;
//
//import com.esotericsoftware.kryo.Kryo;
//import com.esotericsoftware.kryo.io.ByteBufferInputStream;
//import com.esotericsoftware.kryo.io.ByteBufferOutputStream;
//import com.esotericsoftware.kryo.io.Input;
//import com.esotericsoftware.kryo.io.Output;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.nio.ByteBuffer;
//
//public class SerializeTool {
//    private static final Log L = LogFactory.getLog(SerializeTool.class);
//    private Kryo kryo;
//    private int initSize;
//
//    private SerializeTool() {
//    }
//
//    public static void main(String[] args) {
//        Double obj = new Double(12);
//        SerializeTool serial = new SerializeTool.Builder().build();
//        ByteBuffer byteBuffer = serial.toByteBuffer(8, obj);
//        byteBuffer.flip();
//        obj = serial.fromByteBuffer(byteBuffer, obj.getClass());
//        System.out.println(obj);
////        SerializeTool serializeTool = new SerializeTool.Builder().setInitSize(128 * 1024 * 1024).build();
////        Test test = new Test();
////       // test.set();
////       // StopWatch stopWatch = new StopWatch();
////      //  stopWatch.start();
////        byte[] data = new byte[0];
////        for (int i = 0; i < 10; i++) {
////            data = serializeTool.toBytes(test);
////            test = serializeTool.fromBytes(data, Test.class);
////            System.out.println(data.length);
////            ByteBuffer byteBuffers = serializeTool.toByteBuffer(256,test);
////            System.out.println(byteBuffer.position());
////            test = serializeTool.fromByteBuffer(byteBuffer, Test.class);
////        }
////       // stopWatch.stop();
////        System.out.println(test.s.getS()+"size " + data.length / 1024 / 1024);
////       // System.out.println(stopWatch.getTime() / 10);
////       // System.out.println(test.data.get(0));
//////        ByteBuffer byteBuffer = ;
//
//        //  System.out.println(serializeTool.fromByteBuffer(serializeTool.toByteBuffer(test), Test.class).i);
//    }
//
//    public ByteBuffer toByteBuffer(int buffSize, Object object) {
//        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffSize);
//        ByteBufferOutputStream byteBufferOutputStream = new ByteBufferOutputStream(byteBuffer);
//        Output output = new Output(byteBufferOutputStream);
//        kryo.writeObject(output, object);
//        output.close();
//        return byteBuffer;
//    }
//
//    public <T> T fromByteBuffer(ByteBuffer byteBuffer, Class<T> klass) {
//        ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream(byteBuffer);
//        Input input = new Input(byteBufferInputStream);
//        T object = kryo.readObject(input, klass);
//
//        input.close();
//        return object;
//    }
//
//    public byte[] toBytes(int buffSize, Object object) {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(buffSize);
//        Output output = new Output(byteArrayOutputStream);
//        kryo.writeObject(output, object);
//        output.close();
//        return byteArrayOutputStream.toByteArray();
//    }
//
//
//    public byte[] toBytes(Object object) {
//        return toBytes(initSize, object);
//    }
//
//
//    public <T> T fromBytes(byte[] data, Class<T> klass) {
//        Input input = new Input(new ByteArrayInputStream(data));
//        T object = kryo.readObject(input, klass);
//        input.close();
//        return object;
//    }
//
//    public Object fromBytesToObject(byte[] data, Class<?> klass) {
//        Input input = new Input(new ByteArrayInputStream(data));
//        Object object = kryo.readObject(input, klass);
//        input.close();
//        return object;
//    }
//
//    public static class Builder {
//        Kryo kryo;
//        int initSize = 32 * 1024 * 1024;
//
//        public Builder() {
//            kryo = new Kryo();
//        }
//
//        /**
//         * 注册被序列、反序列化的类，序列化和反序列化需要相同的注册顺序
//         *
//         * @param klass
//         * @return
//         */
//        public Builder registry(Class<?> klass) {
//            kryo.register(klass);
//            kryo.setRegistrationRequired(true);
//            return this;
//        }
//
//        public Builder registry(Class<?> klass, int id) {
//            kryo.register(klass, id);
//            kryo.setRegistrationRequired(true);
//            return this;
//        }
//
//        public Builder setSerializeTransient(boolean enable) {
//            kryo.getFieldSerializerConfig().setSerializeTransient(enable);
//          //  kryo.getFieldSerializerConfig().
//            return this;
//        }
//
//        public Builder setIgnoreSyntheticFields(boolean ignore) {
//            kryo.getFieldSerializerConfig().setIgnoreSyntheticFields(ignore);
//            return this;
//        }
//
//        public Builder setInitSize(int initSize) {
//            this.initSize = initSize;
//            return this;
//        }
//
//        public SerializeTool build() {
//            SerializeTool serializeTool = new SerializeTool();
//            serializeTool.kryo = kryo;
//            serializeTool.initSize = initSize;
//            return serializeTool;
//        }
//    }
//}
