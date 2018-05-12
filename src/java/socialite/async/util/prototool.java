package socialite.async.util;



import io.protostuff.*;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import socialite.util.ByteBufferOutputStream;

/**
 * Created by zhangzh on 2017/2/20.
 */
public class prototool {
    public prototool() {
    }

    public static <T> byte[] serializer(T o) {
        Schema<T>schema =(Schema<T>) RuntimeSchema.getSchema(o.getClass());

        //return ProtobufIOUtil.toByteArray();
        return ProtostuffIOUtil.toByteArray(o,schema,LinkedBuffer.allocate(256));
       // return ProtostuffIOUtil.toByteArray(o,schema,LinkedBuffer.allocate(256));
    }

    public static <T> T deserializer(byte[] bytes, Class<T> clazz) {

        T obj = null;
        try {
            obj = clazz.newInstance();
            Schema schema = RuntimeSchema.getSchema(obj.getClass());
            ProtostuffIOUtil.mergeFrom(bytes,obj,schema);
           // ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return obj;
    }
}

