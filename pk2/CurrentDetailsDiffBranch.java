package pk2;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import unfucker.PreScan;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class CurrentDetailsDiffBranch extends VBox
{
    final TextFlow taCurrent = new TextFlow();

    public CurrentDetailsDiffBranch(final Runnable rb)
    {
        setPadding(new Insets(10));
        setSpacing(10);

        taCurrent.setLineSpacing(1.5);

        final Button btnUpdate = new Button("Refresh");
        btnUpdate.setOnAction(actionEvent -> {
            Main.w(Global.thisBranchDB, " <--> ", Global.extBranchDB);
            eff();
        });

        final FlowPane fp = new FlowPane(btnUpdate, new Label("  ↓ get latest version →← merge changes"));
        fp.setPadding(new Insets(10));
        fp.setHgap(5);


        getChildren().addAll(fp, setupTextFlow(taCurrent));

        //clearFileList();
        // show-stuff
        //showStuff();
    }

    private void eff()
    {
        final ArrayList<TopItem> topLevelFolders = new ArrayList<>();
        //Global.thisBranchDB.getRootItems(Flags.TL_FOLDER, topLevelFolders);
        Global.extBranchDB.getRootItems(Flags.TL_FOLDER, topLevelFolders);

        final FScaner fsc = new FScaner(topLevelFolders.toArray(new TopItem[0]));
        showStuff(fsc.getPoodah());
    }

    private void showStuff(ArrayList<Entry> entries)
    {
        taCurrent.getChildren().clear();

        for (final Entry e : entries)
        {
            final var t = new Text(e.path + " " + e.flag);
            final var btnCurrent = new Button("↓");
            final var btnMerge = new Button("→←");

            btnMerge.setDisable(true);

            btnCurrent.setOnAction(actionEvent -> {
                btnMerge.setDisable(true);
                btnCurrent.setDisable(true);
                t.setStrikethrough(true);
                //Main.w("Putin -> ", /*Global.extBranchDB.getRepoPath(). e.topLvlFolderPath, e.path));
                efies(e.id);
            });

            btnMerge.setOnAction(actionEvent -> {
                btnMerge.setDisable(true);
                btnCurrent.setDisable(true);
                t.setStrikethrough(true);
            });

            if (e.flag == Flags.FILE_CHANGED)
            {
                btnMerge.setDisable(false);
            }

            Formatter.format(t, e.flag);

            t.setOnMouseClicked(mouseEvent -> {
                SomeOpener.openStuff(e);
            });

            taCurrent.getChildren().addAll(btnCurrent, new Text("  "), btnMerge,
                    new Text(" ".repeat(5)), t, new Text("\n"));
        }

        // ok, we only care about changed/new/deleted (if files are deleted in the other branch --
        // do we keep here -- I don't know.)
    }

    private void efies(long id)
    {
        try (BufferedInputStream bis = Global.thisBranchDB.getContent(id))
        {
            Main.w(new String(bis.readAllBytes(), StandardCharsets.UTF_8));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private ScrollPane setupTextFlow(TextFlow tf)
    {
        final ScrollPane spn = new ScrollPane(tf);
        spn.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        spn.setFitToHeight(true);
        spn.setFitToWidth(true);
        tf.setPrefSize(10_000, 10_000);
        tf.setLineSpacing(5);
        tf.setPadding(new Insets(10));

        return spn;
    }

    private void clearFileList()
    {
        taCurrent.getChildren().clear();
        final Text ffff = new Text("ain't got nothin' here --- lorz, update first");
        taCurrent.getChildren().add(ffff);
    }
}
