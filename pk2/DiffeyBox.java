package pk2;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class DiffeyBox extends ScrollPane
{
    private final TextFlow tf = new TextFlow();

    public DiffeyBox()
    {
        setContent(tf);
        setFitToHeight(true);
        setFitToWidth(true);
        tf.setStyle("-fx-background-color: white");
        tf.setPadding(new Insets(5));
        setPrefSize(15_000, 15_000);
    }

    public void showDiff(InputStream ins1, InputStream ins2, InputStream ins3)
    {
        tf.getChildren().clear();

        try
        {
            byte[] b = ins1.readAllBytes();

            StringBuilder sbs1 = new StringBuilder();
            Diffy200 d = new Diffy200(new ByteArrayInputStream(b), ins2);
            d.tryDiff();
            List<Diffy200.Line> g = d.getResult();
            for (Diffy200.Line l : g)
            {
                if (l.lineStat == Diffy200.lineStat_deleted)
                {
                    continue;
                }

                sbs1.append(l.text).append("\n");
            }
            Main.w(sbs1);
            ByteArrayInputStream bis1 = new ByteArrayInputStream(sbs1.toString().getBytes());

            StringBuilder sbs2 = new StringBuilder();
            Diffy200 e = new Diffy200(new ByteArrayInputStream(b), ins3);
            e.tryDiff();
            List<Diffy200.Line> g2 = e.getResult();
            for (Diffy200.Line l : g2)
            {
                if (l.lineStat == Diffy200.lineStat_deleted)
                {
                    continue;
                }

                sbs2.append(l.text).append("\n");
            }
            Main.w(sbs2);
            ByteArrayInputStream bis2 = new ByteArrayInputStream(sbs2.toString().getBytes());


            Diffy200 def2 = new Diffy200(bis1, bis2);
            def2.tryDiff();
            List<Diffy200.Line> g3 = def2.getResult();

            for (Diffy200.Line dl : g3)
            {
                Text tt = new Text(dl.text + "\n");
                tt.setFont(Font.font("monospaced", 14));
                tf.getChildren().add(tt);

                if (dl.lineStat == Diffy200.lineStat_new)
                {
                    tt.setFill(Color.CORNFLOWERBLUE);
                }

                if (dl.lineStat == Diffy200.lineStat_deleted)
                {
                    tt.setFill(Color.RED);
                }
            }
        }
        catch (Exception ignore) {}


    }

    public void showDiff(InputStream ins1, InputStream ins2)
    {
        tf.getChildren().clear();

        try
        {
            StringBuilder sbs = new StringBuilder();

            Diffy200 d = new Diffy200(ins1, ins2);
            d.tryDiff();
            List<Diffy200.Line> g = d.getResult();

            for (Diffy200.Line dl : g)
            {
                Text tt = new Text(dl.text + "\n");
                tt.setFont(Font.font("monospaced", 14));
                tf.getChildren().add(tt);

                if (dl.lineStat == Diffy200.lineStat_new)
                {
                    tt.setFill(Color.CORNFLOWERBLUE);
                }

                if (dl.lineStat == Diffy200.lineStat_deleted)
                {
                    tt.setFill(Color.RED);
                }
            }

            for (Diffy200.Line dl : g)
            {
                if (dl.lineStat == Diffy200.lineStat_deleted)
                {
                    continue;
                }
                sbs.append(dl.text).append("\n");
            }

            Main.w(sbs);
        }
        catch (Exception ignore) {}
    }
}
