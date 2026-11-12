package pk2;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class FScaner
{
    private final ArrayList<Entry> poodah = new ArrayList<>();

    public FScaner(TopItem... tlFolders)
    {
        for (TopItem t : tlFolders)
        {
            nf(t);
        }
    }

    private void nf(TopItem t)
    {
        final HashMap<String, EntryForScan> entriesInDB = Global.thisBranchDB.getLatestEntries(t.id);
        final ArrayList<Path> stuffInSrc = getStuffInSource(t.path, t);

        final ArrayList<EntryForScan> existingStuff = new ArrayList<>();

        for (Path ip : stuffInSrc)
        {
            final EntryForScan e = entriesInDB.remove(ip.toString());
            if (e == null)
            {
                // it's in src not in db --- it's NEW

                if (Global.extBranchDB == null)
                {
                    poodah.add(new Entry(0, ip.toString(), Flags.FILE_NEW, "", 0, 0, t.id, t.path));
                }
            }
            else
            {
                existingStuff.add(e);
            }
        }

        //Global.xyz.
        if (Global.extBranchDB != null)
        {
            somethingInDiffBranch(t, existingStuff);
        }
        else
        {
            checkForChanged(t, existingStuff);
        }
        //EntryForScan eos; eos.
        addDeletedStuff(entriesInDB, t);
    }

    private void somethingInDiffBranch(TopItem t, ArrayList<EntryForScan> e)
    {
        for (EntryForScan ef : e)
        {

            var lastestCommitForeignBranch = Global.thisBranchDB.getLatestCommit(t.id, ef.path);
            var commitsInCurrentBranch = Global.extBranchDB.getAllCommits(t.id, ef.path);
            int ii = commitsInCurrentBranch.indexOf(lastestCommitForeignBranch);

            final Path loc = Global.extBranchDB.getRepoPath().resolve(t.path).resolve(ef.path);
            // if hash is the same as in repo, then file hasn't been modified.
            final var hash = Global.getHash(loc);

            if (ii >= 0)
            {
                continue;
            }

            // if (ii == 0)
            // latest commits are the same in both branches -- we don't care
            // if (ii >= 1)
            // there are newer commits in the current branch.
            // we do nothing .. we don't care
            if (Global.thisBranchDB.fileWasChangedInSource(t.id, ef.path, hash))
            {
                poodah.add(new Entry(ef.id, ef.path,Flags.FILE_CHANGED_2, ef.hash, 0, 0, t.id, t.path));
            }
            else
            {
                poodah.add(new Entry(ef.id, ef.path,Flags.FILE_CHANGED, ef.hash, 0, 0, t.id, t.path));
            }
        }
    }

    private void addDeletedStuff(HashMap<String, EntryForScan> hm, TopItem t)
    {
        for (EntryForScan e : hm.values())
        {
            final Entry e2;
            if (Global.extBranchDB != null)
            {
                e2 = new Entry(e.id, e.path, Flags.FILE_NEW, e.hash, 0, 0, t.id, t.path);
            }
            else
            {
                e2 = new Entry(e.id, e.path, Flags.FILE_DELETED, e.hash, 0, 0, t.id, t.path);
            }
            poodah.add(e2);
        }
    }

    private ArrayList<Path> getStuffInSource(String p, TopItem t)
    {
        final ArrayList<Path> r = new ArrayList<>();
        final ArrayList<Path> q = new ArrayList<>();
        q.add(Global.thisBranchDB.getRepoPath().resolve(p));
        final int nc = Global.thisBranchDB.getRepoPath().getNameCount();
        final HashMap<String, Boolean> ignore = Global.thisBranchDB.getIgnoreFiles2(t);

        while (!q.isEmpty())
        {
            final Path p0 = q.remove(q.size() - 1);

            try (DirectoryStream<Path> ds = Files.newDirectoryStream(p0))
            {
                for (Path p2 : ds)
                {
                    if (p2.toFile().isDirectory())
                    {
                        q.add(p2);
                        continue;
                    }

                    final Path gp = p2.subpath(nc + 1, p2.getNameCount());
                    if (!ignore.containsKey(gp.toString()))
                    {
                        r.add(gp);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return r;
    }

    private void checkForChanged(TopItem p, ArrayList<EntryForScan> entries)
    {

        //>>> here is we're doing differen branch we need to checl.'
        //
        for (EntryForScan ee : entries)
        {
            //Global.xyz.getCommitsForFile() <__ what about top level? -- don't think we store full path ...'
            //Global.xyz.getFile_x();
            final Path loc = Global.thisBranchDB.getRepoPath().resolve(p.path).resolve(ee.path);
            final String hash = Global.getHash(loc);
            if (hash.isEmpty())
            {
                continue;
            }

            if (!hash.equalsIgnoreCase(ee.hash))
            {
                final Entry entry;
                entry = new Entry(0, ee.path, Flags.FILE_CHANGED, "", 0L, 0L, p.id, p.path);
                poodah.add(entry);
            }
        }
    }

    public ArrayList<Entry> getPoodah()
    {
        return poodah;
    }
}
