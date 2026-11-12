package pk2;

import java.io.BufferedOutputStream;
import java.io.File;
import java.nio.file.*;
import java.util.*;

public class Puller
{
    private final Commit commit;
    private final String uuid;
    private final int superPathNameCount;
    public Puller(Commit c)
    {
        commit = c;
        uuid = "coft";//UUID.randomUUID().toString();
        superPathNameCount = Global.thisBranchDB.getRepoPath().getNameCount();
    }

    // todo: TEST
    public void pull(int x)
    {
        // first we need to clear the top level folders;
        // which -- that's 2 rounds,

        final ArrayList<Entry> es = Global.thisBranchDB.getNyet2(commit.id);

        if (x == 1)
        {
            clearTopLevelFolders(es);
        }
        else
        {
            fullyClearTopLevelFolder();
        }

        getFiles(es);


        /*
        final ArrayList<Entry> es = Global.thisBranchDB.getNyet2(commit.id);
        clearTopLevelFolders(es);
        getFiles(es);
         */
    }

    private void fullyClearTopLevelFolder()
    {

        try (var ds = Files.newDirectoryStream(Global.thisBranchDB.getRepoPath()))
        {
            final ArrayList<Path> paths = new ArrayList<>();

            for (Path p : ds)
            {
                if (p.toFile().isFile())
                {
                    continue;
                }

                if (!p.getFileName().toString().startsWith(Global.repoFolderExclusionPrefix))
                {
                    paths.add(p);
                }
            }

            for (Path s : paths)
            {
                wiper(s);
            }
        }
        catch (Exception e)
        {

        }
    }

    private void clearTopLevelFolders(ArrayList<Entry> es)
    {
        final HashMap<String, String> tlvs = new HashMap<>();
        // now we have unique stuff -- 'k.
        for (Entry e : es)
        {
            tlvs.putIfAbsent(e.topLvlFolderPath, "");
        }

        for (String s: tlvs.keySet())
        {
            wiper(Global.thisBranchDB.getRepoPath().resolve(s));
        }
    }

    private void wiper(Path path)
    {
        //final Path pp = Global.xyz.getRepoPath().resolve(path);
        final ArrayList<Path> q = new ArrayList<>();
        q.add(path);

        final ArrayList<Path> sps = new ArrayList<>();
        sps.add(path);

        while (!q.isEmpty())
        {
            final Path p = q.remove(0);
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(p))
            {
                for (Path p2 : ds)
                {
                    sps.add(p2);
                    if (p2.toFile().isDirectory())
                    {
                        q.add(p2);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        sps.sort(Comparator.comparingInt(Path::getNameCount));

        for (int i = sps.size() -1; i >= 0; i--)
        {
            final var p = sps.get(i);
            if (p.toFile().isFile())
            {
                stash(p);
                // p.subpath(superPathNameCount, p.getNameCount())
            }
            Main.w("delete -> ", sps.get(i), "  as ", p.subpath(superPathNameCount, p.getNameCount()));
            try
            {
                Files.deleteIfExists(sps.get(i));
            }
            catch (Exception e){ e.printStackTrace();}
        }
    }

    final HashMap<String, Entry> fuk = new HashMap<>();
    private void getFiles(ArrayList<Entry> e)
    {
        for (Entry pkk : e)
        {
            fuk.putIfAbsent(pkk.topLvlFolderPath + File.separator + pkk.path, pkk);
        }

        for (Map.Entry<String, Entry> ef : fuk.entrySet())
        {
            final var gp = Global.thisBranchDB.getRepoPath().resolve(ef.getKey());
            Main.w("--> ", gp);
            try
            {
                if (!gp.getParent().toFile().exists())
                {
                    Files.createDirectories(gp.getParent());
                }

                Files.createFile(gp);

                try (var bis = Global.thisBranchDB.getContent(ef.getValue().id);
                        var bos = new BufferedOutputStream(Files.newOutputStream(gp)))
                {
                    final byte[] bb = new byte[1204 * 16];
                    final int iiii = bis.read(bb);
                    bos.write(bb, 0, iiii);
                }
            }
            catch (Exception ex)
            {
            }
        }
    }

    private void stash(Path p)
    {
        try
        {
            //Main.w("Stash ", p.subpath(superPathNameCount, p.getNameCount()));
            // todo: stash this ..
        }
        catch (Exception e)
        {

        }
    }
}
