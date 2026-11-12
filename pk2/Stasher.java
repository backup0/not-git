package pk2;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class Stasher
{
    private final Connection db;

    public Stasher(Path repoPath) throws Exception
    {
        final String ab = "jdbc:h2:" + repoPath.resolve("stash");
        db = DriverManager.getConnection(ab, "sa", "");

        final var q = "create table IF NOT EXISTS files(id bigint generated always as identity primary key, " +
                "stid varchar, path varchar, entry blob);";
        db.createStatement().execute(q);
    }

    public void stash(Path fp, Path p, String id)
    {
        final var q = "insert into files(stid, path, entry)";
        try (PreparedStatement ps = db.prepareStatement(q);
             BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(fp)))
        {
            //Files.
            ps.setString(1, id);
            ps.setString(2, p.toString());
            ps.setBlob(3, bis);
            ps.execute();
        }
        catch (Exception e)
        {

        }
    }
}
