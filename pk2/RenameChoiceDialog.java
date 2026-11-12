package pk2;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;

public class RenameChoiceDialog extends Dialog<String> {

    private final TextFlow tf = new TextFlow();
    private final Label lblRenameTo = new Label();
    private final String ogName;

    public RenameChoiceDialog(String ogName, ArrayList<TopItem> rf) {
        setResizable(true);
        dfs(rf);
        setupTheRest();
        tf.setLineSpacing(2.5);
        getDialogPane().setMinWidth(500);
        getDialogPane().setMaxHeight(500);
        this.ogName = ogName;
    }

    private void dfs(ArrayList<TopItem> rf)
    {
        final ToggleGroup tg = new ToggleGroup();
        for (TopItem r : rf)
        {
            final RadioButton rb = new RadioButton(r.path);
            rb.selectedProperty().addListener(new ChangeListener<Boolean>()
            {
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1)
                {
                    Main.w(aBoolean, " --> ", t1);
                    lblRenameTo.setText("Rename " + ogName + " [TO] " + rb.getText());
                }
            });

            rb.setToggleGroup(tg);
            tf.getChildren().addAll(rb, new Text("\n"));
        }
            }

    private void setupTheRest() {

        setTitle("Rename root folder");
        setResizable(false);

        final ScrollPane sp = new ScrollPane(tf);
        final Label lblNote = new Label("Warning: The entry in the repo will be renamed.");
        final VBox vb = new VBox(sp, lblNote, lblRenameTo);

        getDialogPane().setContent(vb);

        getDialogPane().getButtonTypes().add(ButtonType.OK);
        setOnCloseRequest(dialogEvent -> {
            close();
        });

        setResultConverter(buttonType -> {
            if (ButtonType.OK == buttonType)
            {
                return getResult0();
            }
            else
            {
                return null;
            }
        });
    }

    private String getResult0()
    {
        final StringBuilder s = new StringBuilder();
        for (Node n : tf.getChildren())
        {
            if (n instanceof RadioButton)
            {
                RadioButton rb = (RadioButton) n;
                if (rb.isSelected())
                {
                    s.append(rb.getText());
                    break;
                }
            }
        }
        return s.toString();
    }
}
