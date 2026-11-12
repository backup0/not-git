package pk2;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.net.URL;

public class CBE extends CheckBoxTreeItem<String>
{
    private final String val;
    private int flag;

    public CBE(String val, int flag)
    {
        super();
        this.flag = flag;
        this.val = val;
        draw();
    }

    public void draw()
    {
        final Label lbl = new Label(" " + val);
        lbl.setGraphicTextGap(5);

        /*
        if (flag == FuFu.folder || flag == FuFu.folder_new || flag == FuFu.folder_deleted)
        {
            lbl.setGraphic(new ImageView(Global.imgs.getImage(ImgCodes.TREE_FOLDER)));
            setExpanded(true);
        }

        switch (flag)
        {
            case FuFu.folder_deleted:
            case FuFu.file_deleted:
                lbl.setTextFill(Color.ORANGERED);
                break;
            case FuFu.file_new:
                lbl.setTextFill(Color.CORNFLOWERBLUE);
                break;
            case FuFu.file_changed:
                lbl.setTextFill(Color.BLACK);
                break;
            default:
                lbl.setTextFill(Color.DIMGRAY);
                break;
        }
        setGraphic(lbl);
        //
        */
    }

    public String getVal()
    {
        return val;
    }

    public boolean isFolder()
    {
        return false;//(flag == FuFu.folder || flag == FuFu.folder_new || flag == FuFu.folder_deleted);
    }
}
