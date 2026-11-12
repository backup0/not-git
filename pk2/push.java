package pk2;

import java.util.Random;

public class push
{
    private static final Random urand = new Random();

    public static String fubar()
    {
        final String gd = Global.getDate4Commmit();
        final String s1 = getRandomString(5);
        final String s2 = getRandomString(5);
        final String s3 = getRandomString(5);
        //return getRandomString(3) + "-" +getRandomString(5) + "-" +  urand.nextInt(Integer.MAX_VALUE);
        return s1 + "-" + s2 + "-" + s3 + "-" + gd;
    }

    public static String getRandomString(int len) {

        final char[] uchars = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
        final StringBuilder sb = new StringBuilder();
        int i = 0;
        int n;

        while (i < len) {

            n = urand.nextInt(uchars.length);
            sb.append(uchars[n]);
            i++;
        }
        return sb.toString();
    }
}
