import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileDemo {
    public static void main(String[] args) {

        try {
//          example1("test1.txt");
            example2("test2.txt");
//          example3("test3.txt");
//          example4("test4.txt");
//          example5("test4.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void example1(String fileName) {
        try {
            // create a new RandomAccessFile with filename Example
            RandomAccessFile raf = new RandomAccessFile("/Volumes/Disk1/Udemy/Kafka/workspace/java_source_code/seek/src/main/resources/" + fileName, "rw");

            // write something in the file
            raf.writeUTF("Hello World");

            // set the file pointer at 0 position
            raf.seek(0);

            // print the string
            System.out.println("" + raf.readUTF());

            // set the file pointer at 0 position
            raf.seek(0);

            // write something in the file
            raf.writeUTF("This is an example");

            // set the file pointer at 0 position
            raf.seek(0);

            // print the string
            System.out.println("" + raf.readUTF());

            // close the stream and release resources
            raf.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void example2(String fileName) {
        try {
            // create a new RandomAccessFile with filename Example
            RandomAccessFile raf = new RandomAccessFile("/Volumes/Disk1/Udemy/Kafka/workspace/java_source_code/seek/src/main/resources/" + fileName, "rw");

            // write to txt and idx
            String str01 = "{\"name\":\"John\",\"age\":30,\"car\":null}";
            printIdx(raf, str01);
            raf.writeUTF(str01);

            String str02 = "{\"name\":\"Alice\",\"age\":25,\"car\":\"nissan\"}";
            printIdx(raf, str02);
            raf.writeUTF(str02);

            String str03 = "{\"name\":\"Tom\",\"age\":20,\"car\":\"ford\"}";
            printIdx(raf, str03);
            raf.writeUTF(str03);


            // set the file pointer at 0 position
            raf.seek(0);
            System.out.println("seek_00 -> " + raf.readUTF());

            // set the file pointer at 37 position
            raf.seek(37);
            System.out.println("seek_37 -> " + raf.readUTF());

            // set the file pointer at 79 position
            raf.seek(79);
            System.out.println("seek_79 -> " + raf.readUTF());

            // close the stream and release resources
            raf.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private static JsonParser jsonParser = new JsonParser();

    private static String extractName(String tweetJson){
        // gson library
        return jsonParser.parse(tweetJson)
                .getAsJsonObject()
                .get("name")
                .getAsString();
    }

    public static void printIdx(RandomAccessFile raf, String jsonStr) throws IOException {
        String strIdx = "{\"idx\":" + raf.getFilePointer() + ",\"name\":\"" + extractName(jsonStr) + "\"}";
        System.out.println(strIdx);
    }

    public static void example3(String fileName) {
        try {
            // create a new RandomAccessFile with filename Example
            RandomAccessFile raf = new RandomAccessFile("/Volumes/Disk1/Udemy/Kafka/workspace/java_source_code/seek/src/main/resources/" + fileName, "r");

            long position = raf.length();

            while (position > 0) {

                position -= 1;

                raf.seek(position);
                byte b = raf.readByte();

                System.out.print((char) b);
            }

            // close the stream and release resources
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void example5(String fileName) throws Exception {
        RandomAccessFile randomAccessFile = null;

        String line1 = "line\n";
        String line2 = "asdf1234\n";

        // read / write permissions
        randomAccessFile = new RandomAccessFile("/Volumes/Disk1/Udemy/Kafka/workspace/java_source_code/seek/src/main/resources/" + fileName, "rw");

        randomAccessFile.writeBytes(line1);
        randomAccessFile.writeBytes(line2);

        // Place the file pointer at the end of the first line
        randomAccessFile.seek(line1.length());

        byte[] buffer = new byte[line2.length()];
        randomAccessFile.read(buffer);
        System.out.println(new String(buffer));

        randomAccessFile.close();
    }
}