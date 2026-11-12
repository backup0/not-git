package pk2;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class CommitDetailsDisplay extends VBox
{
    private final TextArea taDetails = new TextArea();
    private final TextFlow tfFileDetails = new TextFlow();
    private Commit currentCommit = null;
    private HashMap<Integer, Boolean> hms = new HashMap<>();
    private final Button btnShow = new Button();
    private final String stringShowAll = "Show all files";
    private final String stringShowSome = "Show modded only";

    private final String sCompare = "Compare with: ";
    private final Button btnCompareWith = new Button(sCompare + "?");

    private final Button btnPull = new Button("Pull");

    public CommitDetailsDisplay()
    {
        setPadding(new Insets(10));
        setSpacing(10);
        //setPrefSize(15000, 15000);
        taDetails.setEditable(false);

        btnPull.setOnAction(actionEvent -> {

            if (currentCommit == null)
            {
                return;
            }
            pull();
        });

        btnShow.setText(stringShowAll);
        btnShow.setOnAction(actionEvent -> {
            if (btnShow.getUserData() == null)
            {
                btnShow.setUserData(true);
                btnShow.setText(stringShowSome);
                load(Global.thisBranchDB.getNyet2(currentCommit.id), true);
            }
            else
            {
                btnShow.setUserData(null);
                btnShow.setText(stringShowAll);
                if (currentCommit == null)
                {
                    return;
                }
                update(currentCommit);
            }
        });

        btnCompareWith.setOnAction(actionEvent -> {


            final var commit = showCommitListDialog();
            if (commit == null)
            {
                return;
            }

            if (commit.id == -2)
            {
                return;
            }

            showMsg(commit);
            btnCompareWith.setText(sCompare + commit.fName);
            btnCompareWith.setTooltip(new Tooltip(commit.msg));

            btnPull.setDisable(true);
            btnShow.setDisable(true);

            if (commit.id == -1)
            {
                showSheit(null);
            }
            else
            {
                showSheit(commit);
            }

        });

        final var btnCancelCompare = new Button("x");
        btnCancelCompare.setOnAction(actionEvent ->
        {
            update(currentCommit);
        });

        final FlowPane fp;
        fp = new FlowPane(btnPull, btnShow, new Label(" ".repeat(5)), btnCompareWith, btnCancelCompare);
        fp.setHgap(5);
        fp.setVgap(5);
        fp.setPadding(new Insets(5));

        final ScrollPane spn = new ScrollPane(tfFileDetails);
        spn.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        spn.setFitToHeight(true);
        spn.setFitToWidth(true);

        getChildren().addAll(taDetails, fp, spn);

        setVgrow(spn, Priority.ALWAYS);

        tfFileDetails.setLineSpacing(5);
        tfFileDetails.setPadding(new Insets(10));
    }

    private void showSheit(Commit c)
    {
        tfFileDetails.getChildren().clear();
        hms.clear();

        final var gs = new GScanner(currentCommit, c);
        load(gs.getResult(), false);
    }


    private void showMsg(Commit c)
    {
        final var s = "=== comparing ===\n\n" + currentCommit.fName + "; " + currentCommit.msg +
                "\n\n=== with ===\n\n" + c.fName + "; " + c.msg;
        taDetails.setText(s);
    }

    private Commit showCommitListDialog()
    {
        if (currentCommit == null)
        {
            return null;
        }

        final var cld = new CommitListDialog(currentCommit);
        final var x = cld.showAndWait();

        return x.orElse(null);
    }

    public void update(Commit c)
    {
        btnCompareWith.setText(sCompare + "?");

        btnPull.setDisable(false);
        btnShow.setDisable(false);

        btnShow.setText(stringShowAll);
        btnShow.setUserData(null);
        taDetails.clear();
        tfFileDetails.getChildren().clear();
        hms.clear();

        currentCommit = c;

        final String s = c.fName + "\n" + c.label + "\n" + c.dateTime + "\n" + c.author + "\n\n" + c.msg;
        taDetails.setText(s);

        final ArrayList<Entry> entries = Global.thisBranchDB.getNyet(c.id);
        load(entries, false);
    }

    private void load(ArrayList<Entry> entries, boolean ov)
    {
        for (Entry e : entries)
        {
            if (hms.containsKey(e.path.hashCode()))
            {
                continue;
            }

            hms.put(e.path.hashCode(), true);

            final Text txt = new Text((Paths.get(e.topLvlFolderPath, e.path)).toString());
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
                    //Main.w("Openz -- show file")
                    SomeOpener.openStuff(e);
                }
            });
            
            if (ov)
            {
                Formatter.format(txt, Flags.FILE);
            }
            else
            {
                Formatter.format(txt, e.flag);
            }

            tfFileDetails.getChildren().add(txt);
            tfFileDetails.getChildren().add(new Text("\n"));
        }
    }

    private void pull()
    {
        final String o1 = "1: Default"; // restore from repo; just what's in the repo
        final String o2 = "2: Clean repo folder"; // clean up repo folder; and restore

        final ChoiceDialog<String> dc = new ChoiceDialog<>(o1, o2);
        dc.showAndWait();
        final var ops = dc.getSelectedItem();

        if (ops.contains("1:"))
        {
            (new Puller(currentCommit)).pull(1);
        }
        else
        {
            (new Puller(currentCommit)).pull(2);
        }
    }
}
