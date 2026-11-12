package pk2;

import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Diffy2 extends HBox
{
    private final TextFlow tfA = new TextFlow();
    private final TextFlow tfB = new TextFlow();
    private final TextFlow tfAB = new TextFlow();

    private final Path f1 = Paths.get("D:\\Disposable\\test-f1.txt");
    private final Path f2 = Paths.get("D:\\Disposable\\test-f2.txt");

    private final ArrayList<XX> alA = new ArrayList<>();
    private final ArrayList<XX> alB = new ArrayList<>();


    public Diffy2()
    {
        setSpacing(10);
        Button btnReset = new Button("leLoad");
        btnReset.setOnAction(actionEvent -> shite());

        ScrollPane[] sp = new ScrollPane[]{new ScrollPane(), new ScrollPane(), new ScrollPane()};
        sp[0].setContent(tfA);
        sp[1].setContent(tfB);
        sp[2].setContent(tfAB);

        sp[0].setMinWidth(250);
        sp[1].setMinWidth(250);
        sp[2].setMinWidth(250);

        getChildren().addAll(btnReset, sp[0], sp[1], sp[2]);

    }

    private void shite()
    {
        tfA.getChildren().clear();
        tfB.getChildren().clear();
        tfAB.getChildren().clear();

        alA.clear();
        alB.clear();

        loadAandB();
        tryDiff();
        printOut();
    }

    private void printOut()
    {
        for (XX x : alB)
        {
            final Text ft = new Text(x.txt + "\n");
            ft.setFont(Font.font("Consolas"));

            if (x.st == 0)
            {
                ft.setFill(Color.CADETBLUE);
            }
            else if (x.st == 45)
            {
                ft.setFill(Color.BLACK);
            }
            else if (x.st == 66)
            {
                ft.setFill(Color.ORANGERED);
            }
            tfAB.getChildren().add(ft);
        }
    }

    private void loadAandB()
    {
        try (BufferedReader br = Files.newBufferedReader(f1))
        {
            int ctr = 0;
            String s;
            while ((s = br.readLine()) != null)
            {
                XX ai = new XX(s);
                ai.y = ctr++;
                alA.add(ai);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        for (XX es : alA)
        {
            Text tt = new Text(es.txt + "\n");
            tt.setFont(Font.font("Consolas"));
            tfA.getChildren().add(tt);
        }


        try (BufferedReader br = Files.newBufferedReader(f2))
        {
            String s;
            while ((s = br.readLine()) != null)
            {
                alB.add(new XX(s));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        for (XX es : alB)
        {
            Text tt = new Text(es.txt +"\n");
            tt.setFont(Font.font("Consolas"));
            tfB.getChildren().add(tt);
        }

    }

    private void tryDiff()
    {
        final ArrayList<Integer> deletedItems = new ArrayList<>();

        int offset = 0;
        for (int i = 0; i < alA.size(); i++)
        {
            XX x = alA.get(i);
            int io = findMatchingInB(offset, x);
            if (io >= 0)
            {
                // found matching; so line exists in both files
                x.st = 1;
                offset = io + 1;
            }
            else
            {
                deletedItems.add(i);
            }
        }
/*
        int off = 0;
        for (Integer i : deletedItems)
        {
            XX x2 = alA.get(i);
            x2.st = 66;
            off = slipDeletedCrap(off, x2);
        }*/
    }

    private int slipDeletedCrap(int offset, XX x)
    {
        Main.w("del -> ", x.txt, " ", x.y, " ", x.st);
        int rv = -1;
        for (int i = offset; i < alB.size(); i++)
        {
            rv = i;
            XX oik = alB.get(i);
            if (oik.y < 0)
            {
                continue;
            }

            if (oik.y == x.y - 1)
            {Main.w("Found --- some shite @ ", oik.y);
                alB.add(i+1, x);
                break;
            }
        }
        return rv;
    }

    private int findMatchingInB(int start, XX xFromA)
    {
        int r = -1;
        for (int i = start; i < alB.size(); i++)
        {
            XX x = alB.get(i);
            if (x.txt.compareTo(xFromA.txt) == 0)
            {
                x.st = 45;
                x.y = xFromA.y;
                r = i;
                break;
            }
        }
        return r;
    }


    private static class XX
    {
        public int y = -1;
        public int st = 0;
        public final String txt;

        public XX(String t)
        {
            txt = t;
        }
    }
}
