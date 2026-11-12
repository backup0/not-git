package pk2;

import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JMDiffBox extends VBox
{
    public JMDiffBox()
    {
        setSpacing(15);
        setPadding(new Insets(10));
        //setPrefSize(10_000, 10_000);
    }

    public void load(String p, int rootId, long commitId, long commitId2)
    {
        
        getChildren().clear();

        final TextArea ta = new TextArea();
        ta.setPrefSize(10_000, 10_000);
        ta.setWrapText(true);
        ta.setEditable(false);
        ta.setFont(Font.font("Monospaced", 14));

        final Tab tabDiff = new Tab("Diff");
        tabDiff.setClosable(false);

        final DiffeyBox dbx = new DiffeyBox();
        tabDiff.setContent(dbx);

        final Tab tabThisFile = new Tab("This file", ta);
        tabThisFile.setClosable(false);

        final TabPane tabPane = new TabPane(tabDiff, tabThisFile);

        getChildren().add(tabPane);

        try (var bif1 = Global.thisBranchDB.getFileCont(commitId, p, rootId);
             var bif2 = Global.thisBranchDB.getFileCont(commitId2, p, rootId))
        {
            final ByteArrayInputStream bis = new ByteArrayInputStream(bif2.readAllBytes());
            dbx.showDiff(bif1, bis);
            bis.reset();
            ta.setText(new String(bis.readAllBytes(), StandardCharsets.UTF_8));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void load(Path p)
    {
        getChildren().clear();

        final TextArea ta = new TextArea();
        ta.setPrefSize(10_000, 10_000);
        ta.setWrapText(true);
        ta.setEditable(false);
        ta.setFont(Font.font("Monospaced", 14));

        getChildren().add(ta);

        try (var bis = new BufferedInputStream(Files.newInputStream(p)))
        {
            byte[] x = bis.readAllBytes();
            final String s = new String(x, StandardCharsets.UTF_8);
            ta.setText(s);
        }
        catch (Exception e)
        {

        }
    }

    public void load(String p, int rootId, long commitId)
    {
        getChildren().clear();

        final TextArea ta = new TextArea();
        ta.setPrefSize(10_000, 10_000);
        ta.setWrapText(true);
        ta.setEditable(false);
        ta.setFont(Font.font("Monospaced", 14));

        getChildren().add(ta);

        try (var bis = new BufferedInputStream(Global.thisBranchDB.getFileCont(commitId, p, rootId)))
        {
            byte[] x = bis.readAllBytes();
            final String s = new String(x, StandardCharsets.UTF_8);
            ta.setText(s);
        }
        catch (Exception e)
        {

        }
    }

    public void loadCurrent(Path p, int rootId, String p2, long cid)
    {
        // current - current file.
        // so, we diff with 'anchor' ..
        getChildren().clear();

        byte[] b;
        try
        {
            b = Files.readAllBytes(p);
        }
        catch (Exception e)
        {
            b = new byte[0];
            e.printStackTrace();
        }

        final TextArea ta = new TextArea(new String(b, StandardCharsets.UTF_8));
        ta.setPrefSize(10_000, 10_000);
        ta.setWrapText(true);
        ta.setEditable(false);
        ta.setFont(Font.font("Monospaced", 14));

        final Tab tabDiff = new Tab("Diff");
        tabDiff.setClosable(false);

        final DiffeyBox dbx = new DiffeyBox();
        tabDiff.setContent(dbx);

        final Tab tabThisFile = new Tab("This file", ta);
        tabThisFile.setClosable(false);

        final TabPane tabPane = new TabPane(tabDiff, tabThisFile);

        getChildren().add(tabPane);

        try (var bif1 = Global.thisBranchDB.getFileCont(cid, p2, rootId); var fis = new FileInputStream(p.toString()))
        {
            dbx.showDiff(bif1, fis);
        }
        catch (Exception e)
        {

        }
    }

    public void loadMissing(String path)
    {
        // shit is missing ... eleted, not here nemore - 'k
        getChildren().clear();

        final TextArea ta = new TextArea(path + " was deleted ..");
        ta.setPrefSize(10_000, 10_000);
        ta.setWrapText(true);
        ta.setEditable(false);
        ta.setFont(Font.font("Monospaced", 14));

        getChildren().add(ta);
    }
}
