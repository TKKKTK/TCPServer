import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MatWriterTest {
    public static void main(String[] args) {

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("ServerMode.txt", true));
            out.write("wanggang" + "\n");
            out.write("ttt");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
