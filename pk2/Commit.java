package pk2;

public class Commit
{
    public final long id;
    public final String fName;
    public final String msg;
    public final String author;
    public final String dateTime;
    public final String label;

    public Commit(long id, String fName, String msg, String author, String dt, String label)
    {
        this.id = id;
        this.fName = fName;
        this.msg = msg;
        this.author = author;
        dateTime = dt;
        this.label = label;
    }
}
