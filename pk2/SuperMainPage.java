package pk2;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SuperMainPage extends StackPane
{

    private final ListView<String> lv = new ListView<>();

    public SuperMainPage()
    {
        setAlignment(Pos.CENTER);
        setPrefSize(50_000, 50_000);
        setPadding(new Insets(20));

        final var btn = new Button("Open/Create repo");
        btn.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {
                openFolder();
            }
        });

        lv.getItems().addAll(Repostory.read());

        lv.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() > 1)
            {
                final var s = lv.getSelectionModel().getSelectedItem();
                if (s == null) return;

                openRepo(s);
            }
        });

        final var vb = new VBox(btn, lv);
        vb.setSpacing(10);

        final var tp = new TitledPane("Zer0", vb);
        tp.setCollapsible(false);

        getChildren().add(tp);
    }

    private void openFolder()
    {

        final var dc = new DirectoryChooser();
        final File f = dc.showDialog(getScene().getWindow());
        if (f != null)
        {
            openRepo(f.toString());
        }
    }

    private void openRepo(String s)
    {
        try
        {
            final Path repo = Paths.get(s);
            if (Files.exists(repo))
            {
                Global.repo = repo;
                selectBranch();
                if (Global.thisBranchDB != null)
                {
                    getScene().setRoot(new CPS());
                }
            }
            else
            {
                throw new Exception("Folder no exists.");
            }

            if (!lv.getItems().contains(s))
            {
                Repostory.add(s);
            }
        }
        catch (Exception e)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("(!)");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void selectBranch()
    {
        try
        {
            var theFile = Global.repo.resolve("current_branch");

            if (!Files.exists(theFile))
            {
                if (createRepo())
                {
                    Files.createFile(theFile);
                }
                else
                {
                    return;
                }
            }

            Global.extBranchDB = null;

            final var fileName = new String(Files.readAllBytes(theFile));
            final var path = Global.repo.resolve(fileName + ".mv.db");
            if (Files.exists(path))
            {
                Global.thisBranchDB = new Hush(Global.repo, fileName);
                Global.currentBranch = fileName;
                return;
            }

            final var bd = new BranchDialog();
            if (!bd.isEmpty())
            {
                bd.showAndWait();
            }

            if (bd.open == null && bd.makeCurrent == null)
            {
                // todo: wtf?
                // show something
            }

            if (bd.open == null)
            {
                // make current ..
                Global.thisBranchDB = new Hush(Global.repo, bd.makeCurrent);
                Global.currentBranch = bd.makeCurrent;
                Files.write(theFile, bd.makeCurrent.getBytes());
            }
            else
            {
                // just open
                Global.thisBranchDB = new Hush(Global.repo, bd.open);
                Global.currentBranch = bd.open;
            }
        }
        catch (Exception e)
        {
            // show error or something.
        }
    }

    private boolean createRepo()
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("(i)");
        alert.setContentText("Not a valid repo; make a repo here?");

        final var rslt = alert.showAndWait();
        return rslt.filter(buttonType -> !buttonType.getButtonData().isCancelButton()).isPresent();
    }
}
