package pk2;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Hush
{
    // sub_.mv.db
    // sub_branch_name.mv.db
    // sub_branch_name_2.mv.db
    // etc.
    // database:
    // main table: files
    // path, data(the content), deleted, hash, commit id,
    // 2nd table: commits
    // id(auto inc), long-id (string - why bother?), msg, commiter(why?), time(?-wN)
    private final Path repoPath;
    private final Connection db;

    public Hush(Path repoPath, String repoName) throws Exception
    {
        this.repoPath = repoPath;
        
        if (repoName == null || repoName.isEmpty())
        {
            repoName = "ng_sub";
        }

        final String ab = "jdbc:h2:" + repoPath.resolve(repoName);
        db = DriverManager.getConnection(ab, "sa", "");
        createTables();
    }

    public Path getRepoPath()
    {
        return repoPath;
    }

    private void createTables() throws Exception
    {
        final String qtls = "create table IF NOT EXISTS tls(id int generated always as identity primary key, " +
                "path varchar, flag varchar, dt int)";
        db.createStatement().execute(qtls);

        final String qFiles;
        qFiles = "create table IF NOT EXISTS files(id bigint generated always as identity primary key, path varchar, " +
                    "flag varchar, entry blob, hash varchar, len bigint, commit_id bigint, root int);";
        db.createStatement().execute(qFiles);

        final String qCommits;
        qCommits = "create table IF NOT EXISTS commits(id bigint generated always as identity primary key, f_name varchar, " +
                "msg varchar, author varchar, date_time varchar, lbl varchar);";
        db.createStatement().execute(qCommits);

        final String qIgnore;
        qIgnore = "create table IF NOT EXISTS ignore(id bigint generated always as identity primary key, path varchar, " +
                "root int);";
        db.createStatement().execute(qIgnore);
    }

    public void storeFile(Path file, String path, long cid, int tlId, Flags flag)
    {
        Main.w(file, path, cid, tlId, flag);
    }

    public void storeFile2(Path file, String path, long cid, int tlId, Flags flag)
    {
        
        if (flag == Flags.FILE_DELETED)
        {
            final String q;
            q = "insert into files(path, flag, hash, len, commit_id, root) values(?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = db.prepareStatement(q))
            {
                ps.setString(1, path);
                ps.setString(2, flag.name());
                ps.setString(3, "");
                ps.setLong(4, 69L);
                ps.setLong(5, cid);
                ps.setInt(6, tlId);
                ps.execute();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        try
        {
            BufferedInputStream bis = null;
            DigestInputStream dis = null;
            PreparedStatement psCreate = null;
            PreparedStatement psUpdateHash = null;

            try
            {
                dis = new DigestInputStream(Files.newInputStream(file), MessageDigest.getInstance("SHA-256"));
                bis = new BufferedInputStream(dis);

                final String q;
                q = "insert into files(path, flag, entry, hash, len, commit_id, root) values(?, ?, ?, ?, ?, ?, ?)";
                psCreate = db.prepareStatement(q, Statement.RETURN_GENERATED_KEYS);
                psCreate.setString(1, path);
                psCreate.setString(2, flag.name());
                psCreate.setBlob(3, bis);
                psCreate.setString(4, "");
                psCreate.setLong(5, 69L);
                psCreate.setLong(6, cid);
                psCreate.setInt(7, tlId);
                psCreate.execute();

                final String hash = Global.getStringHash(dis.getMessageDigest().digest());

                final ResultSet rs1 = psCreate.getGeneratedKeys();
                final long fid;
                if (rs1.first())
                {
                    fid = rs1.getLong(1);
                }
                else
                {
                    throw new RuntimeException();
                }

                psUpdateHash = db.prepareStatement("update files set hash = ? where id =?");
                psUpdateHash.setString(1, hash);
                psUpdateHash.setLong(2, fid);
                psUpdateHash.execute();
            }
            finally
            {
                try
                {
                    bis.close();
                }
                catch (Exception ignore) {}

                try
                {
                    dis.close();
                }
                catch (Exception ignore) {}

                try
                {
                    psCreate.close();
                }
                catch (Exception ignore) {}

                try
                {
                    psUpdateHash.close();
                }
                catch (Exception ignore) {}
            }
        }
        catch (Exception e)
        {

        }
    }

    public Long generateCommitX(String cid, String auth, String msg, String lbl)
    {
        //
        final String q = "insert into commits(f_name, msg, author, date_time, lbl) values(?, ?, ?, ?, ?)";
        try (PreparedStatement ps = db.prepareStatement(q, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setString(1, cid);
            ps.setString(2, msg);
            ps.setString(3, auth);
            ps.setString(4, Global.getDate4Commmit());
            ps.setString(5, lbl);
            ps.execute();

            final ResultSet rs =  ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            else
            {
                return null;
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public HashMap<String, EntryForScan> getLatestEntries(int root)
    {
        final String q = "select max(id) from commits";
        try (ResultSet rs = db.createStatement().executeQuery(q))
        {
            if (rs.first())
            {
                return getEntries(root, rs.getLong(1));
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public Flags getEntryFlag(Entry e, long commitId)
    {
        final String q = "select flag from files where root = ? and path = ? and commit_id = ? limit 1";
        try (PreparedStatement ps = db.prepareStatement(q))
        {
            ps.setInt(1, e.topLvlFolderId);
            ps.setString(2, e.path);
            ps.setLong(3, commitId);

            final var rs = ps.executeQuery();
            if (rs.first())
            {
                return Flags.valueOf(rs.getString(1));
            }
            else
            {
                return null;
            }
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public HashMap<String, EntryForScan> getEntries(int root, long commitId)
    {
        final HashMap<String, EntryForScan> hm = new HashMap<>();
        final String q1 = "select id, path, hash, flag from files where root = ? and commit_id <= ? order by path, commit_id desc";
        try (PreparedStatement ps = db.prepareStatement(q1))
        {
            ps.setInt(1, root);
            ps.setLong(2, commitId);

            final ResultSet rs = ps.executeQuery();
            String currenShite = "";
            while (rs.next())
            {
                final String path = rs.getString(2);
                if (currenShite.equals(path))
                {
                    continue;
                }

                currenShite = path;
                // this is the top most item -- so, it's the lates; we want the latest only -- OK?
                final String flag = rs.getString(4);
                if (flag.equalsIgnoreCase(Flags.FILE_DELETED.name()))
                {
                    // we don't care about deleted
                    continue;
                }


                final EntryForScan e = new EntryForScan(rs.getLong(1), rs.getString(2),
                        rs.getString(3));
                hm.put(e.path, e);
            }
        }
        catch (Exception ignore)
        {
        }
        return hm;
    }

    public String addTLItem(TopItem t)
    {

        final String s = "insert into tls(path, flag, dt) values(?, ?, ?)";
        try (PreparedStatement ps = db.prepareStatement(s))
        {
            ps.setString(1, t.path );
            ps.setString(2, t.flag.name());
            ps.setInt(3, Global.getDateInInt());
            ps.execute();
            return "";
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
    }

    public String reflagTLItem(TopItem t)
    {
        final String s = "update tls set flag = ? where id = ?";
        try (PreparedStatement ps = db.prepareStatement(s))
        {
            ps.setString(1, t.flag.name());
            ps.setInt(2, t.id);
            ps.execute();
            return "";
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
    }

    public void renameTopLevelFF(String oldName, String newName, Flags f)
    {
        final String q = "update tls set path = ? where path = ? and flag = ?";
        try (PreparedStatement ps = db.prepareStatement(q))
        {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.setString(3, f.name());
            ps.execute();
        }
        catch (Exception ex)
        {}
    }

    //public void
    public void getRootItems(Flags flag, ArrayList<TopItem> als)
    {

        try (ResultSet rs = db.createStatement().executeQuery("select * from tls"))
        {
            while (rs.next())
            {
                if (flag != Flags.valueOf(rs.getString("flag")))
                {
                    continue;
                }

                final TopItem rf;
                rf = new TopItem(rs.getInt(1), rs.getString(2), Flags.valueOf(rs.getString(3)));
                als.add(rf);
            }
        }
        catch (Exception ignore)
        {
        }
    }

    public ArrayList<Commit> getCommitsForFile(String file, int tlId)
    {
        final ArrayList<Commit> rslt = new ArrayList<>();
        final String sq1;
        sq1 = "select * from commits where id in (select commit_id from files where path = ? and root = ?) " +
                "order by id desc;";

        try (PreparedStatement ps = db.prepareStatement(sq1))
        {
            ps.setString(1,file);
            ps.setInt(2, tlId);
            final ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                final Commit c = new Commit(rs.getLong(1), rs.getString(2),
                        rs.getString(3), rs.getString(4), rs.getString(5),
                        rs.getString(6));
                rslt.add(c);
            }
        }
        catch (Exception e) {}

        return rslt;
    }

    public ArrayList<Entry> getNyet2(long id)
    {
        final ArrayList<Long> commitIds = new ArrayList<>();

        try (PreparedStatement ps = db.prepareStatement("select id from commits where id <= ? order by id desc"))
        {
            ps.setLong(1, id);
            final ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                commitIds.add(rs.getLong(1));
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        final ArrayList<Entry> efs = new ArrayList<>();
        final HashMap<String, Entry> es = new HashMap<>();

        if (commitIds.isEmpty())
        {
            return efs;
        }

        for (long i : commitIds)
        {
            final ArrayList<Entry> poo = getNyet(i);
            if (poo.isEmpty())
            {
                continue;
            }

            for (Entry e : poo)
            {
                es.putIfAbsent(e.path, e);
            }
        }

        for (Entry e : es.values())
        {
            if (e.flag != Flags.FILE_DELETED)
            {
                efs.add(e);
            }
        }
        return efs;
    }

    public HashMap<Integer, TopItem> getTopLevels()
    {
        final HashMap<Integer, TopItem> hms = new HashMap<>();
        try (ResultSet rs = db.createStatement().executeQuery("select * from tls"))
        {
            while (rs.next())
            {
                final TopItem rf;
                rf = new TopItem(rs.getInt(1), rs.getString(2), Flags.valueOf(rs.getString(3)));
                hms.put(rs.getInt(1), rf);
            }
        }
        catch (Exception ignore) {}

        return hms;
    }

    public ArrayList<Entry> getNyet(long id)
    {

        final HashMap<Integer, TopItem> hms = getTopLevels();

        ArrayList<Entry> entries = new ArrayList<>();
        String es = "select id, path, flag, hash, root from files where commit_id = ?"; // todo: no need order by?
        try (PreparedStatement ps = db.prepareStatement(es))
        {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                Entry e;
                e = new Entry(rs.getLong(1), rs.getString(2),
                        Flags.valueOf(rs.getString(3)), rs.getString(4), 0l,
                        id, rs.getInt(5), hms.get(rs.getInt(5)).path);
                entries.add(e);
            }
        }
        catch (Exception ignore)
        {ignore.printStackTrace();}
        return entries;
    }

    public ArrayList<Commit> getCommits()
    {
        final ArrayList<Commit> as = new ArrayList<>();
        try (ResultSet rs = db.createStatement().executeQuery("select * from commits order by id desc"))
        {
            while (rs.next())
            {
                final Commit c = new Commit(rs.getLong(1), rs.getString(2),
                        rs.getString(3), rs.getString(4), rs.getString(5),
                        rs.getString(6));
                as.add(c);
            }
        }
        catch (Exception e)
        {
        }
        return as;
    }

    public void ignore(String path, int tlId)
    {

        final String s = "insert into ignore(path, root) values(?, ?)";
        try (PreparedStatement ps = db.prepareStatement(s))
        {
            ps.setString(1, path);
            ps.setInt(2, tlId);
            ps.execute();
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    public ArrayList<String[]> getIgnoreFiles()
    {

        final HashMap<Integer, TopItem> tlvs = getTopLevels();
        final ArrayList<String[]> rslt = new ArrayList<>();

        try (ResultSet rs = db.createStatement().executeQuery("select * from ignore"))
        {
            while (rs.next())
            {
                final Path p = Paths.get(tlvs.get(rs.getInt(3)).path, rs.getString(2));
                final String[] s3 = new String[]{rs.getInt(1) + "", p.toString()};
                rslt.add(s3);
            }
        }
        catch (Exception ignore) {ignore.printStackTrace();}

        return rslt;
    }

    public HashMap<String, Boolean> getIgnoreFiles2(TopItem ti)
    {
        final  HashMap<String, Boolean> rslt = new HashMap<>();

        try (PreparedStatement ps = db.prepareStatement("select path from ignore where root = ?"))
        {
            ps.setInt(1, ti.id);
            final ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                rslt.put(rs.getString(1), true);
            }
        }
        catch (Exception ignore) {ignore.printStackTrace();}

        return rslt;
    }

    public void removeFromIgnore(String id)
    {
        try (PreparedStatement ps = db.prepareStatement("delete from ignore where id = ?"))
        {
            ps.setInt(1, Integer.parseInt(id));
            ps.execute();
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    public void reflagTopFolder(TopItem ti)
    {
        final Flags newFlag;
        if (ti.flag == Flags.TL_FOLDER)
        {
            newFlag = Flags.TL_FOLDER_IGNORE;
        }
        else if (ti.flag == Flags.TL_FOLDER_IGNORE)
        {
            newFlag = Flags.TL_FOLDER;
        }
        else
        {
            return;
        }

        try (PreparedStatement ps = db.prepareStatement("update tls set flag =? where id =?"))
        {
            ps.setString(1, newFlag.name());
            ps.setInt(2, ti.id);
            ps.execute();
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    public InputStream getFileCont(long commitId, String p, int tlId)
    {
        /*
        ble IF NOT EXISTS files(id bigint generated always as identity primary key, path varchar, " +
                    "flag varchar, entry blob, hash varchar, len bigint, commit_id bigint, root int);";
         */

        final String s = "select flag, entry from files where path =? and root =? and commit_id = ? limit 1";
        try (PreparedStatement ps = db.prepareStatement(s))
        {
            ps.setInt(2, tlId);
            ps.setLong(3, commitId);
            ps.setString(1, p);
            final ResultSet rs = ps.executeQuery();
            if (rs.first())
            {
                return rs.getBlob(2).getBinaryStream();
            }
        }
        catch (Exception ignore) {}
        return InputStream.nullInputStream();
    }

    public Entry getNyet(long id, String path, int rootId, long cid)
    {

        final HashMap<Integer, TopItem> hms = getTopLevels();

        ArrayList<Entry> entries = new ArrayList<>();
        String es = "select id, path, flag, hash, root from files where path = ? and root = ? and commit_id = ?";
        try (PreparedStatement ps = db.prepareStatement(es))
        {
            ps.setString(1, path);
            ps.setInt(2, rootId);
            ps.setLong(3, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                Entry e;
                e = new Entry(rs.getLong(1), rs.getString(2),
                        Flags.valueOf(rs.getString(3)), rs.getString(4), 0l,
                        id, rs.getInt(5), hms.get(rs.getInt(5)).path);
                entries.add(e);
            }
        }
        catch (Exception ignore)
        {ignore.printStackTrace();}
        return null;
    }

    public String getLatestCommit(int tlid, String path)
    {

        final String s = "select id, commit_id from files where path = ? and root = ? order by id desc";
        try (var ps = db.prepareStatement(s))
        {
            ps.setString(1, path);
            ps.setInt(2, tlid);
            final var rs = ps.executeQuery();
            if (rs.first())
            {
                 return getCommitFriendlyName(rs.getLong(2));
            }
            else
            {
                return "";
            }
        }
        catch (Exception e)
        {
            return "";
        }
    }

    public ArrayList<String> getAllCommits(int tlid, String path)
    {

        final var rslt = new ArrayList<String>();

        final String s = "select id, commit_id from files where path = ? and root = ? order by id desc;";
        try (var ps = db.prepareStatement(s))
        {
            ps.setString(1, path);
            ps.setInt(2, tlid);
            final var rs = ps.executeQuery();
            while (rs.next())
            {
                final var ss = getCommitFriendlyName(rs.getLong(2));
                if (!ss.isEmpty())
                {
                    rslt.add(ss);
                }
            }
        }
        catch (Exception ignore){}

        return rslt;
    }

    public String getCommitFriendlyName(long id)
    {
        try (var ps = db.prepareStatement("select f_name from commits where id =? limit 1"))
        {
            ps.setLong(1, id);
            final var rs = ps.executeQuery();
            if (rs.first())
            {
                return rs.getString(1);
            }
            else
            {
                return "";
            }
        }
        catch (Exception e)
        {
            return "";
        }
    }

    public Boolean fileWasChangedInSource(int tl, String path, String hash)
    {

        // now -- pull the file's hash
        final String qs = "select id from files where root=? and hash=? and path=? limit 1";
        try (var ps = db.prepareStatement(qs))
        {
            ps.setLong(1, tl);
            ps.setString(2, hash);
            ps.setString(3, path);

            return !(ps.executeQuery()).first();

        }
        catch (Exception e)
        {
            return null;
        }
    }

    public void close()
    {
        try
        {
            db.close();
        }
        catch (Exception ignore) {}
    }

    public BufferedInputStream getContent(long id)
    {
        try (PreparedStatement ps = db.prepareStatement("select entry from files where id = ? limit 1"))
        {
            ps.setLong(1, id);
            final ResultSet rs = ps.executeQuery();
            if (rs.first())
            {
                return new BufferedInputStream(rs.getBinaryStream(1));
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
