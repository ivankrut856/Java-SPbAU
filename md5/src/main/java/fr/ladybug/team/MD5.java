package fr.ladybug.team;

import java.security.MessageDigest;

/**
 * MD5 utilities class
 */
public class MD5 {
    /**
     * Creates MD5 single thread evaluator
     * @return MD5Evaluator which is single thread implemented
     */
    public static MD5Evaluator getEvaluator() {
        return new SingleThreadMD5();
    }

    /**
     * Creates MD5 multi thread evaluator
     * @return MD5Evaluator which is multi thread implemented
     */
    public static MD5Evaluator getEvaluatorMultiThread() {
        return new MultiThreadMD5();
    }

    /**
     * Transforms digest from byte array to hex string
     * @param md5 the digest to transform
     * @return the result hex string
     */
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
