package pk2;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class CommitsForFile extends VBox
{

    final ListView<String> lv = new ListView<>();
    public CommitsForFile(Entry e)
    {
        //setStyle("-fx-background-color: -fx-base");
        setPadding(new Insets(10));
        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        final Button btnReset = new Button("Clear selection");
        btnReset.setOnAction(actionEvent -> {
            lv.getSelectionModel().clearSelection();
        });

        getChildren().addAll(btnReset, lv);

        final ArrayList<Commit> commits = Global.thisBranchDB.getCommitsForFile(e.path, e.topLvlFolderId);
        for (Commit c : commits)
        {
            lv.getItems().add(c.msg); //<<--- if no msg, show commit id; etc.
        }

        lv.getSelectionModel().clearSelection();
    }

    public String getSelected()
    {
        return lv.getSelectionModel().getSelectedItem();
    }
}
