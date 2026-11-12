package pk2;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CommitDialog extends Dialog<String[]> {

    private final Label lblCommitId = new Label();
    private final TextField tfAuthor = new TextField();
    private final TextField tfLabel = new TextField();
    private final TextArea taComment = new TextArea();
    private final String cid = push.fubar();

    public CommitDialog() {

        setupTextFields();
        setupTheRest();
        taComment.setWrapText(true);

        Platform.runLater(() -> taComment.requestFocus());
    }

    private void setupTheRest() {

        setTitle("Commit");

        tfAuthor.setText(Global.getCompName());

        getDialogPane().getButtonTypes().add(ButtonType.OK);
        setOnCloseRequest(dialogEvent -> {
            close();
        });

        lblCommitId.setText("Commit " + cid);

        final HBox hb = new HBox(new Label("Author"), tfAuthor, new Label("Label"), tfLabel);
        hb.setSpacing(5);
        hb.setAlignment(Pos.CENTER_LEFT);

        final VBox vb1 = new VBox(new Label("Comment"), taComment);
        vb1.setSpacing(5);

        final VBox vb = new VBox(lblCommitId, hb, vb1);
        vb.setSpacing(10);
        vb.setPadding(new Insets(10));

        setResultConverter(buttonType -> {

            if (ButtonType.OK == buttonType)
            {
                return new String[] {cid, tfAuthor.getText(), tfLabel.getText(), taComment.getText()};
            }
            else
            {
                return new String[0];
            }
        });

        getDialogPane().setContent(vb);
    }

    private void setupTextFields() {
        tfAuthor.setPrefColumnCount(15);
        tfLabel.setPrefColumnCount(15);
        tfAuthor.setPromptText("Author");
        taComment.setPromptText("Comments");
    }
}
