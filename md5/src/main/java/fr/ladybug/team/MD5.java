package fr.ladybug.team;

import java.security.MessageDigest;

public class MD5 {
    public static MD5Evaluator getEvaluator() {
        return new SingleThreadMD5();
    }

    public static MD5Evaluator getEvaluatorMultiThread() {
        return new MultiThreadMD5();
    }

    public static String getHexStringFromMD5(MessageDigest md5) {
        var byteDigest = md5.digest();
        var hexStringBuilder = new StringBuilder();
        for (byte b : byteDigest) {
            if ((0xff & b) < 0x10) {
                hexStringBuilder.append("0").append(Integer.toHexString((0xFF & b)));
            } else {
                hexStringBuilder.append(Integer.toHexString(0xFF & b));
            }
        }
        return hexStringBuilder.toString();
    }
}
