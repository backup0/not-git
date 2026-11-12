package pk2;

import java.io.BufferedReader;
import java.nio.file.*;
import java.util.ArrayList;

public class Repostory
{
    private static final Path fileP = Paths.get("D:\\Disposable\\prev_open");

    public static void check()
    {
        if (!Files.exists(fileP))
        {
            try
            {
                Files.createFile(fileP);
            }
            catch (Exception ignore)
            {
            }
        }
    }

    public static void add(String p)
    {
        try
        {
            Files.write(fileP, (p + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (Exception ignore)
        {}
    }

    public static ArrayList<String> read()
    {
        final var al = new ArrayList<String>();

        try (BufferedReader br = Files.newBufferedReader(fileP))
        {
            while (true)
            {
                final var f = br.readLine();
                if (f == null)
                {
                    break;
                }
                al.add(f);
            }
        }
        catch (Exception ignore){}

        return al;
    }
}
