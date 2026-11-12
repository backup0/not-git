package pk2;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;

import java.util.ArrayList;

public class CommitListDialog extends Dialog<Commit>
{

    private ArrayList<Commit> commitList = null;
    private final ListView<String> lv = new ListView<>();

    public CommitListDialog(Commit c)
    {
        getDialogPane().setContent(lv);
        populate(c);
        lv.setMinWidth(350);
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        setOnCloseRequest(dialogEvent -> {
            close();
        });

        setResultConverter(buttonType -> {

            if (ButtonType.OK == buttonType)
            {
                final int i = lv.getSelectionModel().getSelectedIndex();
                try
                {
                    if (i == 0)
                    {
                        if (c == null)
                        {
                            return commitList.get(i);
                        }
                        else
                        {
                            return new Commit(-1, "CURRENT", "Current files in source",
                                    "", "", "");
                        }
                    }
                    else
                    {
                        return commitList.get(i);
                    }
                }
                catch (Exception e)
                {
                    return new Commit(-2, "", "", "", "", "");
                }
            }
            else
            {
                return null;
            }
        });
    }
    
    private void populate(Commit c1)
    {
        if (c1 != null)
        {
            lv.getItems().add("CURRENT");
        }

        commitList = Global.thisBranchDB.getCommits();
        for (Commit c : commitList)
        {
            if (c1 != null && (c.id == c1.id))
            {
                continue;
            }

            if (c.msg.isEmpty())
            {
                lv.getItems().add(c.fName);
            }
            else
            {
                lv.getItems().add(c.msg);
            }
        }
    }
}
