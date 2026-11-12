package pk2;

import java.io.FileInputStream;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Zippo
{
    public Zippo()
    {
        Path zipFile = Paths.get("D:\\Disposable\\not-x.zip");
        try (FileSystem fs = FileSystems.newFileSystem(zipFile, null)) { // null for default environment (read-only if no 'create')

            Path p = fs.getPath("20230525_030637.jpg");
            Files.readAllBytes(p);

            /*
            Path root = fs.getPath("/");
            try (var stream = Files.walk(root)) {
                stream.forEach(System.out::println); // List all files and directories
            }
            // Read a specific entry
            Path entry = fs.getPath("/new_folder/my_file.txt");
            if (Files.exists(entry)) {
                String content = Files.readString(entry);
                System.out.println("Content: " + content);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
