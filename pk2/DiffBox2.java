package pk2;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.nio.file.Path;
import java.util.ArrayList;

public class DiffBox2 extends VBox
{
    private final Entry anchorEntry;
    private final ArrayList<Commit> commitsForThisFile;
    private final Label lblTop = new Label("?");
    private final ListView<String> lv = new ListView<>();
    private final JMDiffBox jmdiff = new JMDiffBox();

    public DiffBox2(Entry e)
    {
        anchorEntry = e;
        commitsForThisFile = Global.thisBranchDB.getCommitsForFile(anchorEntry.path, e.topLvlFolderId);
        goDoSetup();
        shit(anchorEntry.commitId);
    }

    private void goDoSetup()
    {
        if (anchorEntry.commitId == 0)
        {
            lblTop.setText("** " + anchorEntry.path);
        }

        final HBox hb = new HBox(lblTop);
        hb.setPadding(new Insets(10));
        hb.setSpacing(10);
        hb.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(hb, getCont());
    }

    private HBox getCont()
    {
        lv.setPrefHeight(10_000);
        lv.setMinWidth(250);

        lv.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
                //>> incorrect @ CURRENT; when we scan workspace.
            if (t1.intValue() == 0)
            // this is the current file ..
            {
                if (anchorEntry.commitId == 0)
                {
                    final var p = Global.thisBranchDB.getRepoPath().resolve(anchorEntry.topLvlFolderPath).
                            resolve(anchorEntry.path);
                    if (p.toFile().exists())
                    {
                        jmdiff.load(p);
                    }
                    else
                    {
                        jmdiff.loadMissing(anchorEntry.path);
                    }
                }
                else
                {
                    selectCurrentFile();
                }
                return;
            }

            // compare ..
            final int indx = t1.intValue() - 1;
            if (indx < 0)
            {
                return;
            }

            final Commit cmt = commitsForThisFile.get(indx);

            if (anchorEntry.commitId == cmt.id)
            // same file is selected
            {
                // anchor is selected
                if (anchorEntry.flag == Flags.FILE_DELETED)
                {
                    jmdiff.loadMissing(anchorEntry.path);
                }
                else
                {
                    jmdiff.load(anchorEntry.path, anchorEntry.topLvlFolderId, anchorEntry.commitId);
                }
                return;
            }

            if (anchorEntry.flag == Flags.FILE_DELETED)
            {
                jmdiff.load(anchorEntry.path,anchorEntry.topLvlFolderId, cmt.id);
                return;
            }

            // now we select other commits -- we wanna diff with anchor
            if (Global.thisBranchDB.getEntryFlag(anchorEntry, cmt.id) == Flags.FILE_DELETED)
            {
                jmdiff.loadMissing(anchorEntry.path);
                return;
            }

            jmdiff.load(anchorEntry.path, anchorEntry.topLvlFolderId, anchorEntry.commitId, cmt.id);
            Main.w(cmt.fName);
        });

        lv.getSelectionModel().select(0);

        final HBox hb = new HBox(lv, jmdiff);
        return hb;
    }

    private void selectCurrentFile()
    {
        // select current file to compare
        // anchor --> xx;

        // check if file exists ... if it doesn't show sexy ledee.
       // Global.xyz.getFileCont(entry.commitId, entry.path);
        final Path fp = Global.thisBranchDB.getRepoPath().resolve(anchorEntry.topLvlFolderPath).
                resolve(anchorEntry.path);

        if (fp.toFile().exists())
        {
            if (anchorEntry.flag == Flags.FILE_DELETED)
            {
                // anchor deleted -- so how can we compare? We can't.
                jmdiff.loadMissing(anchorEntry.path);
            }
            else
            {
                // compare ...
                jmdiff.loadCurrent(fp, anchorEntry.topLvlFolderId, anchorEntry.path, anchorEntry.commitId);
            }
        }
        else
        {
            // current file is missing -- we can't; ok,
            jmdiff.loadMissing(anchorEntry.path);
        }
    }

    private void loadStuff()
    {
        final Popup pp = new Popup();
        final CommitsForFile cff = new CommitsForFile(anchorEntry);
        pp.getContent().add(cff);
        pp.setAutoHide(true);
        //pp.show(javafx.stage.Stage.getWindows().get(0));
        pp.show(getScene().getWindow());
        pp.setOnAutoHide(new EventHandler<Event>()
        {
            @Override
            public void handle(Event event)
            {
                Main.w("--> ", cff.getSelected());
            }
        });
    }

    public void shit(long commitId)
    {
        lv.getItems().clear();

        lv.getItems().add("CURRENT");

        int currentLoc = 0;
        for (Commit c : commitsForThisFile)
        {
            String l;

            if (commitId > 0)
            {
                if (commitId > c.id)
                {
                    l = "↓ ";
                }
                else if (commitId < c.id)
                {
                    l = "↑ ";
                }
                else
                {
                    l = "→ ";
                    lblTop.setText(anchorEntry.path + "  (" + c.fName + ")");
                    lblTop.setTooltip(new Tooltip(c.msg));
                    currentLoc = lv.getItems().size();
                }
            }
            else
            {
                l = "+ ";
            }

            if ("".equalsIgnoreCase(c.msg))
            {
                l = l + c.fName;
            }
            else
            {
                l = l + c.msg;
            }
            lv.getItems().add(l);
        }

        lv.getSelectionModel().select(currentLoc);
    }
}
