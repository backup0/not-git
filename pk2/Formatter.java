package pk2;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class Formatter
{
    public static final Color f_new = Color.CORNFLOWERBLUE;
    public static final Color f_changed = Color.DARKGOLDENROD;
    public static final Color f_changed_2 = Color.DEEPSKYBLUE;
    public static final Color f_deleted = Color.CRIMSON;

    public static void format(Text t, Flags f)
    {
        if (f == Flags.FILE_NEW)
        {
            t.setFill(f_new);
            return;
        }

        if (f == Flags.FILE_DELETED)
        {
            t.setFill(f_deleted);
            return;
        }

        if (f == Flags.FILE_CHANGED)
        {
            t.setFill(f_changed);
            return;
        }

        if (f == Flags.FILE_CHANGED_2)
        {
            t.setFill(f_changed_2);
            return;
        }

        if (f == Flags.TL_FOLDER_IGNORE)
        {
            t.setFill(Color.DIMGRAY);
            return;
        }

        if (f == Flags.TL_FOLDER_DELETED)
        {
            t.setFill(f_deleted);
            return;
        }
        
        t.setFill(Color.BLACK);
    }
}
