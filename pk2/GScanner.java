package pk2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class GScanner
{
    private final ArrayList<Entry> entries = new ArrayList<>();

    public GScanner(Commit c1, Commit c2)
    {
        final ArrayList<TopItem> topLevelFolders = new ArrayList<>();

        Global.thisBranchDB.getRootItems(Flags.TL_FOLDER, topLevelFolders);

        for (TopItem t : topLevelFolders)
        {
            compare(t, c1, c2);
        }
    }

    private void compare(TopItem t, Commit c1, Commit c2)
    {

        final HashMap<String, EntryForScan> a1;
        if (c1 == null)
        {
            a1 = getCurrentFiles(t);
        }
        else
        {
            a1 = Global.thisBranchDB.getEntries(t.id, c1.id);
        }

        final HashMap<String, EntryForScan> a2;
        if (c2 == null)
        {
            a2 = getCurrentFiles(t);
        }
        else
        {
            a2 = Global.thisBranchDB.getEntries(t.id, c2.id);
        }

        for (Map.Entry<String, EntryForScan> s : a1.entrySet())
        {
            final var r = a2.remove(s.getKey());
            if (r == null)
            {
                // meaning the stuff does not exists in the 2nd commit.
                a1.put(s.getKey(), new EntryForScan(s.getValue().id, s.getValue().path, "d"));
                final Entry etr;
                etr = new Entry(s.getValue().id, s.getValue().path, Flags.FILE_NEW, "", 0,0,
                        t.id,t.path);
                entries.add(etr);
                continue;
            }

            final Flags flag;
            if (r.hash.equalsIgnoreCase(s.getValue().hash))
            {
                flag = Flags.FILE;

            }
            else
            {
                flag = Flags.FILE_CHANGED;
            }

            final Entry etr;
            etr = new Entry(s.getValue().id, s.getValue().path, flag, "", 0,0,
                    t.id,t.path);
            entries.add(etr);

        }

        for (Map.Entry<String, EntryForScan> s : a2.entrySet())
        {
            final Entry etr;
            etr = new Entry(s.getValue().id, s.getValue().path, Flags.FILE_DELETED, "", 0,0,
                    t.id,t.path);
            entries.add(etr);
        }
    }

    private HashMap<String, EntryForScan> getCurrentFiles(TopItem t)
    {
        final var rslt = new HashMap<String, EntryForScan>();

        final var path = Global.thisBranchDB.getRepoPath().resolve(t.path);
        try (Stream<Path> sp = Files.walk(path);)
        {
            sp.forEach(path1 ->
            {
                if (path1.toFile().isDirectory())
                {
                    return;
                }

                final var pp = path1.subpath(Global.thisBranchDB.getRepoPath().getNameCount() + 1,
                        path1.getNameCount());
                final var hash = Global.getHash(path1);
                final var efs = new EntryForScan(0, pp.toString(), hash);

                rslt.putIfAbsent(pp.toString(), efs);
            });
        }
        catch (Exception ignored)
        {
        }
        return rslt;
    }

    public ArrayList<Entry> getResult()
    {
        return entries;
    }
}
