package pk2;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.Optional;

public class TopFoldersMgmtDialog extends Dialog<TopItem[]> {

    private final TextFlow tf = new TextFlow();
    private final ArrayList<String[]> toDel = new ArrayList<>();
    private final ArrayList<TopItem> items;

    public TopFoldersMgmtDialog(ArrayList<TopItem> rf) {

        setResizable(true);
        setupTheRest();
        tf.setLineSpacing(2.5);
        getDialogPane().setMinWidth(500);
        getDialogPane().setMaxHeight(500);
        items = rf;
        showItems();
    }

    private void showItems()
    {
        for (TopItem r : items)
        {
            final Something ss = new Something()
            {
                @Override
                public void ok(String s)
                {
                    showRenameDialog(s);
                }
            };
            final Coof sb = new Coof(r, ss);
            tf.getChildren().addAll(sb, new Text("\n"));
        }
    }

    private void showRenameDialog(String s)
    {
        final ArrayList<TopItem> als = new ArrayList<>();
        for (Node n : tf.getChildren())
        {
            if (n.getUserData() == null)
            {
                continue;
            }

            final Coof r = (Coof) n;
            if (r.getRat().flag == Flags.TL_FOLDER_NEW)
            {
                als.add(r.getRat());
            }
        }

        final RenameChoiceDialog rcd = new RenameChoiceDialog(s, als);
        final Optional<String> result = rcd.showAndWait();

        if (result.isPresent())
        {
            toDel.add(new String[]{s, result.get()});
            removeRenamedItem(s);
            removeRenamedItem(result.get());
        }
    }

    private void removeRenamedItem(String s)
    {
        Node rn = null;
        for (Node n : tf.getChildren())
        {
            if (n.getUserData() == null)
            {
                continue;
            }

            final Coof r = (Coof) n;
            if (s.equals(r.getRat().path))
            {
                rn = n;
            }
        }

        if (rn != null)
        {
            final int i = tf.getChildren().indexOf(rn);
            try
            {
                tf.getChildren().remove(i);
            }
            catch (Exception ignore) {}
        }

        final ArrayList<Node> nodes = new ArrayList<>();
        for (Node n : tf.getChildren())
        {
            if (n.getUserData() == null)
            {
                continue;
            }
            nodes.add(n);
        }

        tf.getChildren().clear();
        for (Node n : nodes)
        {
            tf.getChildren().add(n);
            tf.getChildren().add(new Text("\n"));
        }
    }

    private void setupTheRest() {

        setTitle("Root folders");

        final Button btn = new Button("Reset");
        btn.setOnAction(actionEvent -> {
            toDel.clear();
            tf.getChildren().clear();
            showItems();
        });

        tf.setPadding(new Insets(5));
        final ScrollPane sp = new ScrollPane(tf);

        final VBox vb = new VBox(btn, sp);
        vb.setSpacing(15);
        vb.setAlignment(Pos.TOP_LEFT);

        getDialogPane().setContent(vb);

        getDialogPane().getButtonTypes().add(ButtonType.OK);
        setResizable(false);

        setOnCloseRequest(dialogEvent -> {
            close();
        });

        setResultConverter(buttonType -> {

            if (ButtonType.OK == buttonType)
            {
                for (String[] s : toDel)
                {
                    Global.thisBranchDB.renameTopLevelFF(s[0], s[1], Flags.TL_FOLDER);
                }

                final ArrayList<TopItem> r = new ArrayList<>();
                for (Node n : tf.getChildren())
                {
                    if (n.getUserData() == null)
                    {
                        continue;
                    }

                    final Coof cr = (Coof) n;

                    if (cr.getSelStat() == 0)
                    {
                        // ignore -
                        r.add(new TopItem(cr.getRat().id, cr.getRat().path, Flags.TL_FOLDER_IGNORE));
                    }
                    else if (cr.getSelStat() == 1)
                    {
                        // selected
                        final Flags f;
                        if (cr.getRat().flag == Flags.TL_FOLDER_NEW)
                        {
                            f = Flags.TL_FOLDER;
                        }
                        else
                        {
                            f = cr.getRat().flag;
                        }
                        r.add(new TopItem(cr.getRat().id, cr.getRat().path, f));
                    }
                    else
                    {
                        // undecided
                        r.add(new TopItem(cr.getRat().id, cr.getRat().path, Flags.TL_NULL));
                    }
                }
                return r.toArray(new TopItem[0]);
            }
            else
            {
                return null;
            }
        });
    }

    public abstract static class Something
    {
        public abstract void ok(String s);
    }
}
