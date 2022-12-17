package top.weixiansen574.LyrePlayer.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RootUtils {
    private Process process;
    private DataOutputStream os;
    private DataInputStream is;
    public RootUtils() throws IOException {
        process = Runtime.getRuntime().exec("su");
        os = new DataOutputStream(process.getOutputStream());

    };
    public void exec(String command){
        try {
            os.write(command.getBytes(StandardCharsets.UTF_8));
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
