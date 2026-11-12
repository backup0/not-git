package pk2;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestDiff extends VBox
{
    Button btn = new Button("Go");
    DiffeyBox db = new DiffeyBox();
    public TestDiff()
    {
        setPadding(new Insets(5));

        final Path f0 = Paths.get("D:\\Disposable\\test-f0.txt");
        final Path f1 = Paths.get("D:\\Disposable\\test-f1.txt");
        final Path f2 = Paths.get("D:\\Disposable\\test-f2.txt");



        btn.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {
                try
                {
                    InputStream ins0 = Files.newInputStream(f0);
                    InputStream ins1 = Files.newInputStream(f1);
                    InputStream ins2 = Files.newInputStream(f2);

                    db.showDiff(ins0, ins1, ins2);
                }
                catch (Exception e) {return;}
            }
        });

        getChildren().addAll(btn, db);
    }
}
