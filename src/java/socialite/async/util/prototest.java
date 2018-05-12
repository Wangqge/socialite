//package socialite.async.util;
//
//
//import java.util.Arrays;
//
///**
// * Created by zhangzh on 2017/2/20.
// */
//public class prototest {
//
//    public void test() {
//
//        SerializeToolHP student = new SerializeToolHP();
//        student.setName("lance");
//        student.setAge(28);
//        student.setStudentNo("2011070122");
//        student.setSchoolName("BJUT");
//
//        byte[] serializerResult = prototool.serializer(student);
//
//        System.out.println("serializer result:" + Arrays.toString(serializerResult));
//
//        SerializeToolHP deSerializerResult = prototool.deserializer(serializerResult,SerializeToolHP.class);
//
//        System.out.println("deSerializerResult:" + deSerializerResult.toString());
//    }
//
//}
//
