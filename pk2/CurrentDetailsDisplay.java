package pk2;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import unfucker.PreScan;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

public class CurrentDetailsDisplay extends VBox
{
    private final TextFlow taCurrent = new TextFlow();
    private final TextFlow taTopLevel = new TextFlow();
    private final TextFlow taIgnored = new TextFlow();
    private final TextFlow taFileList = new TextFlow();

    private final String scf = "Showing current files;";

    final TextArea taCurrentFileNOte = new TextArea(scf);

    public CurrentDetailsDisplay(final Runnable rb)
    {
        setPadding(new Insets(10));
        setSpacing(10);

        final Button btnUpdate = new Button("Refresh");
        btnUpdate.setOnAction(actionEvent -> {
            reload();
            scan();
        });

        final Button btnCommit = new Button("Commit");
        btnCommit.setOnAction(actionEvent -> {
            final CommitDialog cds = new CommitDialog();
            final Optional<String[]> r = cds.showAndWait();

            if (r.isEmpty())
            {
                return;
            }

            if (r.get().length == 0)
            {
                return;
            }

            commit0s(r.get());
            clearFileList();
            rb.run();
        });

        final String tbText1 = "Select all";
        final String tbText2 = "Select none";

        final var tb = new Button(tbText1);
        tb.setOnMouseClicked(mouseEvent -> {
            if (tb.getText().equalsIgnoreCase(tbText1))
            {
                tb.setText(tbText2);
                selectCb(true);
            }
            else
            {
                tb.setText(tbText1);
                selectCb(false);
            }
        });

        final var tbUnignore = new Button("Unignore all");
        tbUnignore.setOnMouseClicked(mouseEvent -> {
            unIgnore();
        });

        final FlowPane fp = new FlowPane(btnUpdate, btnCommit, new Label(" ".repeat(25)), tb, tbUnignore);
        fp.setHgap(5);
        final VBox vbx = new VBox(fp, setupTextFlow(taCurrent));
        vbx.setSpacing(5);
        vbx.setPadding(new Insets(5));

        final Tab tab1 = new Tab("Changes", vbx);
        tab1.setClosable(false);

        final Tab tab2 = new Tab("Ignore", getIgnoreListBox(taIgnored));
        tab2.setClosable(false);

        final Tab tab3 = new Tab("Top level folders", getTopLevelListBox(taTopLevel));
        tab3.setClosable(false);

        final Tab tab4 = new Tab("Files", getFileDispBox());
        tab4.setClosable(false);

        final TabPane tabPane = new TabPane(tab1, tab4, tab2, tab3);

        getChildren().addAll(tabPane);

        setVgrow(tabPane, Priority.ALWAYS);

        clearFileList();
    }

    private VBox getTopLevelListBox(TextFlow tf)
    {
        final Button btn = new Button("Refresh");
        btn.setOnAction(actionEvent -> btfs());

        final VBox vb = new VBox(btn, setupTextFlow(tf));
        vb.setSpacing(5);
        vb.setPadding(new Insets(5));
        return vb;
    }

    private VBox getIgnoreListBox(TextFlow tf)
    {
        final Button btn = new Button("Refresh");
        btn.setOnAction(actionEvent -> showIgnoredStuff());

        final VBox vb = new VBox(btn, setupTextFlow(tf));
        vb.setSpacing(5);
        vb.setPadding(new Insets(5));
        return vb;
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

    private void selectCb(boolean b)
    {
        for (Node n : taCurrent.getChildren())
        {
            if (n.getUserData() == null)
            {
                continue;
            }

            try
            {
                final CheckBox cb = (CheckBox) n;
                cb.setSelected(b);
            }
            catch (Exception ignore) {}
        }
    }

    private void unIgnore()
    {
        for (Node n : taCurrent.getChildren())
        {
            try
            {
                if (n instanceof CheckBox)
                {
                    n.setDisable(false);
                    continue;
                }

                if (n instanceof Text)
                {
                    ((Text) n).setStrikethrough(false);
                }

            }
            catch (Exception ignore) {}
        }
    }

    private void scan()
    {
        taCurrent.getChildren().clear();
        taCurrent.setUserData(null);

        final ArrayList<TopItem> topLevelFolders = new ArrayList<>();
        Global.thisBranchDB.getRootItems(Flags.TL_FOLDER, topLevelFolders);

        final FScaner fsc = new FScaner(topLevelFolders.toArray(new TopItem[0]));
        final ArrayList<Entry> entries = fsc.getPoodah();

        if (!entries.isEmpty())
        {
            taCurrent.setUserData("1");
        }

        for (final Entry e : entries)
        {

            final TextButton tb = new TextButton("-x-");
            tb.setTooltip(new Tooltip("Ignore"));
            taCurrent.getChildren().add(tb);

            final Text txtSpacer = new Text("  ");
            taCurrent.getChildren().add(txtSpacer);

            final CheckBox cb = new CheckBox(" ");
            cb.setUserData(e);
            taCurrent.getChildren().add(cb);

            final Path p = Paths.get(e.topLvlFolderPath, e.path);
            final Text t = new Text("" + p);
            Formatter.format(t, e.flag);
            taCurrent.getChildren().add(t);
            taCurrent.getChildren().add(new Text("\n"));

            t.setOnMouseClicked(mouseEvent -> {
                SomeOpener.openStuff(e);
            });

            t.setOnMouseEntered(mouseEvent -> {
                t.setCursor(Cursor.HAND);
                t.setUnderline(true);
            });

            t.setOnMouseExited(mouseEvent -> {
                t.setCursor(Cursor.DEFAULT);
                t.setUnderline(false);
            });

            tb.setOnMouseClicked(mouseEvent -> {
                if (cb.isDisabled())
                {
                    cb.setDisable(false);
                    t.setStrikethrough(false);
                }
                else
                {
                    cb.setDisable(true);
                    t.setStrikethrough(true);
                }
            });
        }
    }

    private void reload()
    {
        final ArrayList<TopItem> ti = (new PreScan()).preScan();
        if (ti.isEmpty())
        {
            return;
        }

        final TopFoldersMgmtDialog rdg = new TopFoldersMgmtDialog(ti);
        final Optional<TopItem[]> ops = rdg.showAndWait();

        if (ops.isEmpty())
        {
            return;
        }

        if (ops.get().length == 0)
        {
            // todo: correct?
            return;
        }

        final TopItem[] rit = ops.get();
        for (TopItem r : rit)
        {
            if (r.flag == Flags.TL_NULL)
            {
                continue;
            }

            final String msg;
            if (r.id < 0)
            {
                msg = Global.thisBranchDB.addTLItem(r);
            }
            else
            {
                // existing stuff ---
                msg = Global.thisBranchDB.reflagTLItem(r);
            }

            if (!msg.isEmpty())
            {
                Main.w("Thread -- ", msg);
            }

        }
    }
    
    private void commit0s(String[] s)
    {
        //>> something could happen in between update and commit (this -- so; check.)
        if (taCurrent.getUserData() == null)
        {
            return;
        }

        boolean allIgnore = true;
        for (Node n : taCurrent.getChildren())
        {
            if (n.getUserData() == null)
            {
                continue;
            }

            final CheckBox cb = (CheckBox) n;
            if (cb.isDisabled())
            {
                final Entry e = (Entry) cb.getUserData();
                Global.thisBranchDB.ignore(e.path, e.topLvlFolderId);
            }

            if (!cb.isDisabled() && cb.isSelected())
            {
                allIgnore = false;
                break;
            }
        }

        if (allIgnore)
        {
            return;
        }

        // s => commit id; auth; label; commenz
        final Long cid = Global.thisBranchDB.generateCommitX(s[0], s[1], s[3], s[2]);
        if (cid == null)
        {
            // todo: show error msg?
            return;
        }

        for (Node n : taCurrent.getChildren())
        {
            if (n.getUserData() == null)
            {
                continue;
            }

            try
            {
                final CheckBox cb = (CheckBox) n;
                if (cb.isDisabled())
                {
                    continue;
                }

                final Entry e = (Entry) cb.getUserData();
                final Path p = Global.thisBranchDB.getRepoPath().resolve(e.topLvlFolderPath).resolve(e.path);

                if (!cb.isSelected())
                {
                    // no action
                    continue;
                }
                Global.thisBranchDB.storeFile2(p, e.path, cid, e.topLvlFolderId, e.flag);
            }
            catch (Exception ignore) {}
        }
    }

    private void showIgnoredStuff()
    {
        taIgnored.getChildren().clear();

        final ArrayList<String[]> ss = Global.thisBranchDB.getIgnoreFiles();

        for (String[] t : ss)
        {
            final TextButton tb = new TextButton("-x-");

            tb.setTooltip(new Tooltip("Ignore"));
            taIgnored.getChildren().add(tb);

            final Text txtSpacer = new Text("  " + t[1]);
            taIgnored.getChildren().add(txtSpacer);

            tb.setOnMouseClicked(mouseEvent -> {
                Global.thisBranchDB.removeFromIgnore(t[0]);
                txtSpacer.setStrikethrough(true);
                tb.setDisable(true);
            });
        }

        //taFileDEtails.getChildren().add(cb);

        //taFileDEtails.getChildren().addAll(pp);
    }

    private void btfs()
    {
        taTopLevel.getChildren().clear();

        final HashMap<Integer, TopItem> hms = Global.thisBranchDB.getTopLevels();

        for (TopItem t : hms.values())
        {
            final Text txt = new Text(t.path);
            Formatter.format(txt, t.flag);
            final CheckBox cb = new CheckBox();

            if (t.flag == Flags.TL_FOLDER_DELETED)
            {
                cb.setDisable(true);
            }

            if (t.flag == Flags.TL_FOLDER)
            {
                cb.setSelected(true);
            }

            cb.setOnAction(actionEvent -> {

                Global.thisBranchDB.reflagTopFolder(t);
                btfs();
            });

            taTopLevel.getChildren().addAll(cb, new Text(" ".repeat(4)), txt);
            taTopLevel.getChildren().add(new Text("\n"));
        }
    }

    private VBox getFileDispBox()
    {
        taCurrentFileNOte.setMinHeight(150);
        taCurrentFileNOte.setEditable(false);

        final var cs = "Compare with: ";
        final var btnLefresh = new Button("Refresh");
        final var btnCompare = new Button(cs + "?");

        btnCompare.setOnAction(actionEvent -> {
            final var cld = new CommitListDialog(null);
            final var x = cld.showAndWait();
            x.ifPresent(commit ->
            {
                doCompare(commit);
                showText(taCurrentFileNOte, commit);
                btnCompare.setText(cs + commit.fName);
            });
        });

        btnLefresh.setOnAction(actionEvent ->
        {
            loadFiles();
            taCurrentFileNOte.setText("");
            btnCompare.setText(cs + "?");
            taCurrentFileNOte.setText(scf);

        });

        final FlowPane fp = new FlowPane(btnLefresh, btnCompare);
        fp.setHgap(5);

        var vb = new VBox(fp, taCurrentFileNOte, setupTextFlow(taFileList));
        vb.setSpacing(5);
        vb.setPadding(new Insets(5));
        return vb;
    }

    private void loadFiles()
    {
        taFileList.getChildren().clear();

        final ArrayList<TopItem> topLevelFolders = new ArrayList<>();

        Global.thisBranchDB.getRootItems(Flags.TL_FOLDER, topLevelFolders);

        for (TopItem t : topLevelFolders)
        {
            loadFiles(t);
        }
    }

    private void loadFiles(TopItem t)
    {
        final var path = Global.thisBranchDB.getRepoPath().resolve(t.path);
        try (Stream<Path> sp = Files.walk(path);)
        {
            sp.forEach(path1 ->
            {
                if (path1.toFile().isDirectory())
                {
                    return;
                }

                final var pp = path1.subpath(Global.thisBranchDB.getRepoPath().getNameCount() + 1,
                        path1.getNameCount());

                final var txt = new Text(pp.toString());
                taFileList.getChildren().add(txt);
                taFileList.getChildren().add(new Text("\n"));

                final var e = new Entry(0, pp.toString(), Flags.FILE, "", 0, 0L, t.id, t.path);
                actionizeText(txt,e);
            });
        }
        catch (Exception ignored)
        {
        }
    }

    private void doCompare(Commit c)
    {
        taFileList.getChildren().clear();

        if (c.id < 0)
        {
            return;
        }

        final var gs = new GScanner(null, c);

        for (Entry e : gs.getResult())
        {
            final Text txt = new Text((Paths.get(e.topLvlFolderPath, e.path)).toString());
            actionizeText(txt, e);

            Formatter.format(txt, e.flag);
            taFileList.getChildren().add(txt);
            taFileList.getChildren().add(new Text("\n"));
        }
    }

    private void showText(TextArea ta, Commit c)
    {
        final var s = "Comparing == CURRENT == files with \n\n" + c.fName + "\n\n" + c.msg;
        ta.setText(s);
    }

    private void actionizeText(Text txt, Entry e)
    {

        txt.setOnMouseEntered(mouseEvent -> {
            txt.setCursor(Cursor.HAND);
            txt.setUnderline(true);
        });

        txt.setOnMouseExited(mouseEvent -> {
            txt.setCursor(Cursor.DEFAULT);
            txt.setUnderline(false);
        });

        txt.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY)
            {
                Main.w("Show popup.");
                return;
            }

            if (mouseEvent.getClickCount() > 1)
            {
                if (e != null) {SomeOpener.openStuff(e);}
            }
        });
    }
}
