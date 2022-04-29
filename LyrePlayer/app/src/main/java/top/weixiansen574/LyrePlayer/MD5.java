package top.weixiansen574.LyrePlayer;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
    public static String getMD5Three(byte[] data) {
        BigInteger bi = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data, 0, data.length);
            byte[] b = md.digest();
            bi = new BigInteger(1, b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String md5 = bi.toString(16);
        if (md5.length() < 32){
            for (int i = 32 - md5.length(); i > 0; i--) {
                md5 = "0" + md5;
            }
        }
        return md5;
    }
}
