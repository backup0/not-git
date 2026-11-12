package pk2;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import org.h2.value.ExtTypeInfoRow;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class CPS extends HBox
{
    private final TextFlow tf = new TextFlow();
    private final CommitDetailsDisplay cMdd = new CommitDetailsDisplay();
    private final CurrentDetailsDisplay cRdd2;
    private final CurrentDetailsDiffBranch cRdd;
    private ArrayList<Commit> commitList = null;
    private final ListView<String> capos = new ListView<>();

    //TreeV tv = new TreeV();
    public CPS()
    {
        //setCenter(new CommitDetailsDisplay());
        //setTop(hbBtns);

        cMdd.setPrefSize(10_000, 10_000);

        capos.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1)
            {
                if (t1.intValue() < 0)
                {
                    return;
                }

                if (getChildren().isEmpty())
                {
                    return;
                }

                getChildren().removeAll(cRdd2, cRdd, cMdd);

                if (t1.intValue() == 0)
                {
                    // show current interface
                    if (Global.extBranchDB == null)
                    {
                        getChildren().add(cRdd2);
                    }
                    else
                    {
                        getChildren().add(cRdd);
                    }
                }
                else
                {
                    // show not current if.
                    getChildren().add(cMdd);
                    if (commitList == null)
                    {
                        return;
                    }
                    final Commit c = commitList.get(t1.intValue() - 1);
                    cMdd.update(c);
                }
            }
        });

        //capos.getItems().addAll(getCommits());
        capos.setMinWidth(350);
        populateCapos();

        cRdd2 = new CurrentDetailsDisplay(() -> populateCapos());
        cRdd = new CurrentDetailsDiffBranch(() -> populateCapos());
        cRdd.setPrefSize(10_000, 10_000);

        getChildren().addAll(getSidePanel(), capos);
        capos.getSelectionModel().selectFirst();

        /*
        Text g = new Text("shem ham po poo RASH");
        g.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent mouseEvent)
            {
                Main.w(((Text) mouseEvent.getSource()).getText());
                ContextMenu cm = new ContextMenu(new MenuItem("Fk2..."), new MenuItem("Pt2"));
                cm.show(g, Side.BOTTOM, 0, 0);
                if (mouseEvent.getClickCount() > 1)
                {
                    Main.w("DOBLE click!");
                }
            }
        });*/

        //CommitDetailsDisplay cdd = new CommitDetailsDisplay();
        //CurrentDetailsDisplay cdd = new CurrentDetailsDisplay();
        //cdd.setPrefSize(10_000, 10_000);
    }

    private void populateCapos()
    {
        capos.getItems().clear();
        capos.getItems().add("CURRENT");

        commitList = Global.thisBranchDB.getCommits();
        for (Commit c : commitList)
        {
            if (c.msg.isEmpty())
            {
                capos.getItems().add(c.fName);
            }
            else
            {
                capos.getItems().add(c.msg + " (" + c.fName + ")");
            }
        }
    }

    private VBox getSidePanel()
    {
        final Button btnBranch = new Button("b");
        btnBranch.setOnAction(actionEvent -> {

            final var dg = new BranchDialog();
            dg.showAndWait();

            var openP = dg.open;
            var makeCurrentP = dg.makeCurrent;

            if (openP == null && makeCurrentP == null)
            {
                return;
            }

            try
            {
                if (openP == null)
                {
                    if (makeCurrentP.equalsIgnoreCase(Global.currentBranch))
                    {
                        return;
                    }

                    Global.thisBranchDB = new Hush(Global.repo, makeCurrentP);
                    Global.extBranchDB = null;
                }
                else
                {
                    if (openP.equalsIgnoreCase(Global.currentBranch))
                    {
                        return;
                    }

                    Global.thisBranchDB = new Hush(Global.repo, openP);
                    Global.extBranchDB = new Hush(Global.repo, Global.currentBranch);
                }

                populateCapos();
                capos.getSelectionModel().selectFirst();
            }
            catch (Exception e)
            {}
        });

        final Button btnClose = new Button("x");

        final VBox vb = new VBox(btnBranch, new Label(), btnClose);
        vb.setPadding(new Insets(5));
        return vb;
    }

    private void commit()
    {
        CommitDialog chibal2 = new CommitDialog();
        chibal2.initOwner(javafx.stage.Stage.getWindows().get(0));
        chibal2.showAndWait();

        final HBox hb0 = new HBox(new ProgressBar(-1), new Label("Committing ..."));
        hb0.setSpacing(5);
        hb0.setAlignment(Pos.CENTER_LEFT);
        Popup pewp = new Popup();
        pewp.getContent().add(hb0);
        pewp.show(javafx.stage.Stage.getWindows().get(0));
    }

    private void doCommit()
    {
        // generate commit it.
        for (Node n : tf.getChildren())
        {
            if (!(n instanceof CheckBox))
            {
                continue;
            }

            final CheckBox cbs = (CheckBox) n;
            final Entry e = (Entry) cbs.getUserData();

            final Path p = Global.thisBranchDB.getRepoPath().resolve(e.path);
        }
        commit();
    }

    private void shewStuff()
    {
        tf.getChildren().clear();

        final FScaner fs = new FScaner(new TopItem(0,"D:\\Disposable\\not_git\\src", Flags.TL_FOLDER));
        final ArrayList<Entry> als = fs.getPoodah();

        for (Entry e : als)
        {
            final CheckBox cbs = new CheckBox(e.path);
            cbs.setUserData(e);

            if (e.flag == Flags.FILE_NEW)
            {
                cbs.setTextFill(Formatter.f_new);
            }

            if (e.flag == Flags.FILE_DELETED)
            {
                cbs.setTextFill(Formatter.f_deleted);
            }

            if (e.flag == Flags.FILE_CHANGED)
            {
                cbs.setTextFill(Formatter.f_changed);
            }

            tf.getChildren().add(cbs);
            if (e.flag == Flags.FILE_NEW)
            {
                tf.getChildren().add(new Text("  "));
                final TextButton tb = new TextButton("[View]");
                tb.setOnMouseClicked(mouseEvent -> {
                    SomeOpener.openStuff(e);
                });
                tf.getChildren().add(tb);
            }
            tf.getChildren().add(new Text("\n"));
        }
    }

    private void doScan()
    {
        /*
        final ArrayList<RootItem> rootItem = Global.xyz.getRootItems();
        final HashMap<String, RootItem> rootFolders = new HashMap<>();
        final HashMap<String, RootItem> rootFiles = new HashMap<>();

        while (!rootItem.isEmpty())
        {
            final RootItem r = rootItem.remove(rootItem.size() -1);
            if (r.flag == Flags.root_folder)
            {
                rootFolders.put(r.path, r);
                continue;
            }

            if()

        }

        final ArrayList<RootItem> newFolders = new ArrayList<>();

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Global.xyz.getRepoPath()))
        {
            for (Path p : ds)
            {
                if (skip(p))
                {
                    continue;
                }

                //final int flag = p.toFile().isDirectory() ?

                final RootItem rf = rootFolders.get(p.getFileName().toString());
                if (rf == null)
                {
                    newFolders.add(new RootItem(0, p.getFileName().toString(), Flags.root_folder_new));
                }
                // whatever left in there - has been deleted, in hash map.
            }
        }
        catch (Exception ignore) {}

        final RootFolderMgmtDialog rdg = new RootFolderMgmtDialog(newFolders);
        rdg.showAndWait(); */
    }

}
