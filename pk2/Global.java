package pk2;

import javafx.scene.image.Image;

import java.io.BufferedInputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

public class Global
{
    public static String appDataFolderName = "summathing-xyz-23";

    public static Path repo = null;
    public static String currentBranch = null;

    public static Hush thisBranchDB = null;
    public static Hush extBranchDB = null;
    public static Images imgs = null;
    public static HashMap<String, Image> images = new HashMap<>();
    //public static ArrayList<DiffView> openDialogs = new ArrayList<>();
    public static String repoFolderExclusionPrefix = "x__";
    public static Calendar calendar = Calendar.getInstance();

    public static String getStringHash(byte[] bs)
    {
        final StringBuilder sbs = new StringBuilder();
        for (byte b : bs)
        {
            final String i = Integer.toHexString(Byte.toUnsignedInt(b));
            final String i2 = "0".repeat(2 - i.length()) + i;
            sbs.append(i2);
        }
        return sbs.toString();
    }

    public static String getHash(Path p)
    {
        final DigestInputStream dis;
        try
        {
            dis = new DigestInputStream(Files.newInputStream(p), MessageDigest.getInstance("SHA-256"));
        }
        catch (Exception e)
        {
            return "";
        }

        try (BufferedInputStream bis = new BufferedInputStream(dis))
        {

            final byte[] buff = new byte[1024 * 128];
            while (true)
            {
                int i = bis.read(buff);
                if (i < 0)
                {
                    break;
                }
            }
        }
        catch (Exception e)
        {
            return "";
        }

        final byte[] md = dis.getMessageDigest().digest();
        final StringBuilder sbs = new StringBuilder();
        for (byte b : md)
        {
            final String i = Integer.toHexString(Byte.toUnsignedInt(b));
            final String i2 = "0".repeat(2 - i.length()) + i;
            sbs.append(i2);
        }
        return sbs.toString();
    }

    public static String getAppDataFolder()
    {
        return System.getenv("localappdata");
    }

    public static int getDateInInt()
    {
        calendar.setTimeInMillis(System.currentTimeMillis());
        final SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyMMdd");

        return Integer.parseInt(sdf.format(calendar.getTime()));
    }

    public static String getDate4Commmit()
    {
        calendar.setTimeInMillis(System.currentTimeMillis());
        final SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyMMddkkmm");

        return sdf.format(calendar.getTime());
    }

    public static String getCompName()
    {
        try
        {
            // Get the local host address
            final InetAddress localHost = InetAddress.getLocalHost();
            // Get the host name from the local host address
            return localHost.getHostName();
        }
        catch (Exception e)
        {
            return "auth-name-unknown";
        }
    }
}
