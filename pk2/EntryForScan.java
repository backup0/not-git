package pk2;

public class EntryForScan
{
    public final long id;
    public final String path;
    public final String hash;
    // root -- need?
    // root path -- need?
    // flag? need? -- maybe.

    public EntryForScan(long id, String path, String hash)
    {
        this.id = id;
        this.path = path;
        this.hash = hash;
    }
}
