package pk2;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class SomeOpener
{

    public static void openStuff(Entry e)
    {
        final Stage primaryStage = new Stage();
        final Scene scene = new Scene(new DiffBox(e), 500, 500);
        scene.getRoot().setStyle("-fx-font-family: 'monospaced'; -fx-font-size: 12.5px;");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
