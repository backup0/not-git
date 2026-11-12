package pk2;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;


public class Coof extends HBox
{

    private final TopItem rat;
    private final CheckBox cdb = new CheckBox();

    public Coof(TopItem r, TopFoldersMgmtDialog.Something ss)
    {
        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        setUserData(1);
        rat = r;

        final Text cbText = new Text(r.path);

        if ((r.flag.name().contains("FOLDER")))
        {
            cdb.setGraphic(new ImageView(Global.imgs.getImage(ImgCodes.TREE_FOLDER)));
        }

        final String[] tbText = new String[]{"[ignore]", "[don't ignore]"};

        final TextButton tb = new TextButton(tbText[0]);
        tb.setOnMouseClicked(mouseEvent -> {

            if (cbText.isStrikethrough())
            {
                cbText.setStrikethrough(false);
                tb.setText(tbText[0]);
                cdb.setDisable(false);
            }
            else
            {
                cbText.setStrikethrough(true);
                tb.setText(tbText[1]);
                cdb.setDisable(true);
            }
        });

        final TextButton tb2 = new TextButton("[rename]");
        tb2.setOnMouseClicked(mouseEvent -> ss.ok(r.path));

        cdb.setUserData(0);
        cdb.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {

            }
        });

        getChildren().addAll(cdb, cbText, tb);
        if (r.flag == Flags.TL_FOLDER_DELETED)
        {
            getChildren().add(tb2);
        }
    }

    public TopItem getRat()
    {
        return rat;
    }

    public int getSelStat()
    {
        if (cdb.isDisabled())
        {
            return 0;
        }

        return cdb.isSelected() ? 1 : 2;
    }
}
