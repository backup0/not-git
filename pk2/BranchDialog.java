package pk2;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.nio.file.Files;
import java.nio.file.Path;

public class BranchDialog  extends Dialog<Path>
{

    private final ListView<String> lv = new ListView<>();
    public String makeCurrent = null;
    public String open = null;

    public BranchDialog()
    {
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        setOnCloseRequest(dialogEvent -> {
            close();
        });

        loadStuff();

        final var btnMakeCurrent = new Button("Make current");
        btnMakeCurrent.setOnAction(actionEvent -> {

            var x = lv.getSelectionModel().getSelectedItem();
            if (x != null)
            {
                makeCurrent = x;
            }
            close();
        });

        final var btnOpen = new Button("Open");
        btnOpen.setOnAction(actionEvent -> {

            var x = lv.getSelectionModel().getSelectedItem();
            if (x != null)
            {
                open = x;
            }
            close();
        });

        final var btnCreate = new Button("Create a branch");
        btnCreate.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {
                createBranch();
                //>> ---< do it here.
            }
        });

        final var fp = new FlowPane();
        fp.setHgap(5);
        if (lv.getItems().isEmpty())
        {
            fp.getChildren().add(btnCreate);
        }
        else
        {
            fp.getChildren().addAll(btnOpen, btnMakeCurrent);
        }

        final var vb = new VBox(fp, lv);
        vb.setPadding(new Insets(10));
        vb.setSpacing(5);

        getDialogPane().setContent(vb);
    }

    private void loadStuff()
    {
        try (var ds = Files.newDirectoryStream(Global.repo))
        {
            for (var p : ds)
            {
                if (Files.isDirectory(p ))
                {
                    continue;
                }

                if (!p.getFileName().toString().startsWith("ng_"))
                {
                    continue;
                }

                if (!p.getFileName().toString().endsWith("mv.db"))
                {
                    continue;
                }

                lv.getItems().add(p.getFileName().toString().replace(".mv.db",""));

            }
        }
        catch (Exception e)
        {

        }
    }

    private void createBranch()
    {
        final TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Input Required");
        dialog.setHeaderText("Create a branch\nEnter name, leave blank for default");

        final var s = dialog.showAndWait();
        Main.w("++++ ", s);
    }

    public boolean isEmpty()
    {
        return lv.getItems().isEmpty();
    }
}
