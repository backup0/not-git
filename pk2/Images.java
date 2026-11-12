package pk2;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public class Images
{
    private final HashMap<ImgCodes, Image> imgMap = new HashMap<>();

    public Images()
    {
        loadIcons();
    }

    private void loadIcons()
    {
        final URL url = getClass().getResource("folder16x16.png");
        if (url == null)
        {
            return;
        }

        try (InputStream ins = url.openStream())
        {
            imgMap.put(ImgCodes.TREE_FOLDER, new Image(ins));
        }
        catch (Exception ignored)
        {
        }
    }

    public Image getImage(ImgCodes ic)
    {
        return imgMap.get(ic);
    }

}
