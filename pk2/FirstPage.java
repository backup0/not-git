package pk2;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FirstPage extends BorderPane
{

    public FirstPage()
    {
        //setCenter(lvs);
        final Button btnOpen = new Button("Open repo");
        btnOpen.setOnAction(actionEvent -> {
            o();
        });

        final Button btnCreate = new Button("Create repo");
        btnCreate.setOnAction(actionEvent -> {
            c();
        });

        final HBox hb = new HBox(btnOpen, btnCreate);

        final ListView<String> hist = new ListView<>();
        hist.getItems().addAll("One", "Dos", "Trois");

        hist.setOnKeyReleased(new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent keyEvent)
            {
                if (keyEvent.getCode() == KeyCode.ENTER)
                {
                    try
                    {
                        final Path p = Paths.get(hist.getSelectionModel().getSelectedItem());
                        oo(p);
                    }
                    catch (Exception ignore) {}
                }
            }
        });

        hist.setOnMouseClicked(mouseEvent -> {

            if (mouseEvent.getClickCount() > 1)
            {
                Main.w("Dobly click ... @ ", hist.getSelectionModel().getSelectedItem());
                try
                {
                    final Path p = Paths.get(hist.getSelectionModel().getSelectedItem());
                    oo(p);
                }
                catch (Exception ignore) {}
            }
        });

        setTop(hb);
        setCenter(hist);
    }

    private void o()
    {
        final DirectoryChooser dc = new DirectoryChooser();
        final File f = dc.showDialog(null);
        if (f != null)
        {
            oo(f.toPath());
        }
    }

    private void c()
    {
        final DirectoryChooser dc = new DirectoryChooser();
        final File f = dc.showDialog(null);
    }

    private void oo(Path p)
    {
        Main.w("Open --> ", p);
    }
}
