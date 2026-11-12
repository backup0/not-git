package pk2;

public class Entry
{
    public final long id;
    public final String path;
    public final Flags flag;
    public final String hash;
    public final long length;
    public final long commitId;
    public final int topLvlFolderId;
    public final String topLvlFolderPath;

    public Entry(long id, String path, Flags flag, String hash, long len, long cid, int tl, String tlPath)
    {
        this.id = id;
        this.path = path;
        this.flag = flag;
        this.hash = hash;
        length = len;
        commitId = cid;
        topLvlFolderId = tl;
        topLvlFolderPath = tlPath;
    }
}
