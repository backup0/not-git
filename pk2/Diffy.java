package pk2;

import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Diffy extends HBox
{
    public static final int lineStat_new = 0;
    public static final int lineStat_changed = 1;
    public static final int lineStat_deleted = 2;

    private final TextFlow tfA = new TextFlow();
    private final TextFlow tfB = new TextFlow();
    private final TextFlow tfAB = new TextFlow();

    private final Path f1 = Paths.get("D:\\Disposable\\test-f1.txt");
    private final Path f2 = Paths.get("D:\\Disposable\\test-f2.txt");

    private final ArrayList<Line> contA = new ArrayList<>();
    private final ArrayList<Line> contB = new ArrayList<>();
    private final ArrayList<Line> deletedItems = new ArrayList<>();

    public Diffy()
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

        loadAandB();

        try (FileInputStream f01 = new FileInputStream(f1.toString());
             FileInputStream f02 = new FileInputStream(f2.toString()))
        {

            Diffy200 d = new Diffy200(f01, f02);
            d.tryDiff();
            List<Diffy200.Line> g = d.getResult();

            for (Diffy200.Line dl : g)
            {
                Text tt = new Text(dl.text + "\n");
                tfAB.getChildren().add(tt);

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
        catch (Exception e)
        {
            e.printStackTrace();
        }

        /*
        tfA.getChildren().clear();
        tfB.getChildren().clear();
        tfAB.getChildren().clear();

        contA.clear();
        contB.clear();

        blockCounter = 0;
        hm = 0;
        deletedItems.clear();

        loadAandB();
        tryDiff();*/
    }

    private void printOut(Line x)
    {
        //for (XX x : alB)
        {
            final Text ft = new Text(x.text + "\n");
            ft.setFont(Font.font("Consolas"));

            if (x.lineStat == 0)
            {
                ft.setFill(Color.CADETBLUE);
            }
            else if (x.lineStat == 45)
            {
                ft.setFill(Color.BLACK);
            }
            else if (x.lineStat == 66)
            {
                ft.setFill(Color.ORANGERED);
            }
            else if (x.lineStat == 700)
            {
                ft.setFill(Color.BLACK);
            }
            else if (x.lineStat == 750)
            {
                ft.setFill(Color.DARKORANGE);
            }
            else if (x.lineStat == 800)
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
            String s;
            while ((s = br.readLine()) != null)
            {
                Text tt = new Text(s + "\n");
                tt.setFont(Font.font("Consolas"));
                tfA.getChildren().add(tt);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try (BufferedReader br = Files.newBufferedReader(f2))
        {
            String s;
            while ((s = br.readLine()) != null)
            {
                Text tt = new Text(s +"\n");
                tt.setFont(Font.font("Consolas"));
                tfB.getChildren().add(tt);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void tryDiff()
    {
        // make sure we never get stuck in a never ending loop
        int breaker = 0;
        int superMax = contA.size() + 10;

        final int size = contA.size();
        int o = 0;
        while (o < size)
        {
            int p = try0(o, contA.get(o));
            if (p == -1)
            {
                o++;
            }
            else
            {
                o = p;
            }

            if (breaker++ > superMax)
            {
                break;
            }
        }
        slipDeletedCrap();
        show();
    }

    //int oob_last = 0;

    int blockCounter = 0;
    private int try0(int i, Line a)
    {
        final ArrayList<ContBlock> als = new ArrayList<>();
        als.add(new ContBlock() );
        findHeadMatches(als, a);
        // no 'anchor'
        Main.w("fhm -> ", a.text);
        if (a.text.equals("4"))
        {
            int x = 90;
        }
        if (als.isEmpty())
        {
            // no match -- so this line is missing from the other file. 'k.
            // must have been deleted; 100% - because there is no match.
            a.ic = blockCounter;
            deletedItems.add(a);
            a.lineStat = lineStat_deleted;
            //Main.w("Deleted => ", a.txt, " >>", a.poop);
            return -1;
        }

        int io = i+1;
        for (; io < contA.size(); io++)
        {
            final Line st = contA.get(io);
            findMatches(als, st);
            if (als.get(0).flag == 1)
            {
                break;
            }
        }

        removeLowerValues(als);

        // take only the top part ..
        if (als.isEmpty())
        {
            return io;
        }

        ContBlock uu = als.get(0);
        if (uu.count == 1)
        {
            //a.poop = blockCounter;
            //dels.add(a);
            //a.st = 800;
            contB.get(uu.loc).lineStat = lineStat_changed;
            contB.get(uu.loc).ic = ++blockCounter;
            hm = uu.loc + 1;
        }
        else
        {
            for (int y = 0; y < uu.count; y++)
            {
                contB.get(uu.loc + y).lineStat = lineStat_changed;
                contB.get(uu.loc + y).ic = ++blockCounter;
                hm = uu.loc + y + 1;
            }
        }
        return io;
    }

    private void slipDeletedCrap()
    {
        while (!deletedItems.isEmpty())
        {
            Line x = deletedItems.remove(0);
            Main.w("delZ -> ", x.text, " ", x.ic);
            putInShite(x);
        }
    }

    private void putInShite(Line s)
    {
        if (s.ic == 0)
        {
            for (int u = 0; u< contB.size(); u++)
            {
                Line xb = contB.get(u);
                if (xb.ic != 666)
                {
                    contB.add(u, s);
                    break;
                }
            }
            s.ic = 666;
            return;
        }

        int i=0;
        for (; i< contB.size(); i++)
        {
            Line xb = contB.get(i);
            if (xb.ic == s.ic)
            {
                break;
            }
        }

        boolean na = true;
        ++i;
        for (; i< contB.size(); i++)
        {
            final Line xb = contB.get(i);
            if (xb.ic != 666)
            {
                contB.add(i, s);
                na = false;
                break;
            }
        }

        if (na)
        {
            contB.add(s);
        }
        s.ic = 666;
    }

    int hm = 0;
    private void findHeadMatches(ArrayList<ContBlock> als, Line x)
    {
        for (int i = hm; i < contB.size(); i++)
        {
            Line b = contB.get(i);
            if (x.text.equals(b.text))
            {
                ContBlock xy = new ContBlock();
                xy.loc = i;
                xy.count++;
                als.add(xy);
            }
        }

        if (als.size() == 1)
        {
            als.clear();
        }
    }

    private void removeLowerValues(ArrayList<ContBlock> a)
    {
        final ContBlock[] heads = a.toArray(new ContBlock[0]);
        a.clear();

        // now keep only the largest value(s).
        // first find out the largest count
        int maxVal = 0;
        for (int i = 1; i < heads.length; i++)
        {
            maxVal = Integer.max(maxVal, heads[i].count);
        }

        // now add back into the list
        for (int i = 1; i < heads.length; i++)
        {
            final ContBlock xy = heads[i];
            if (xy.count >= maxVal)
            {
                a.add(xy);
            }
        }
    }

    private void findMatches(ArrayList<ContBlock> a, Line x)
    {
        final ContBlock[] heads = a.toArray(new ContBlock[0]);
        a.clear();
        a.add(heads[0]);

        int changeCount = 0;

        for (int i = 1; i < heads.length; i++)
        {
            final ContBlock h = heads[i];
            int locToCheck = h.loc + h.count; // need to check the 'next' item.
            a.add(h);

            if (locToCheck >= contB.size())
            {
                // out of range ..
                continue;
            }

            if (h.flag != 0)
            {
                continue;
            }

            changeCount++;
            final Line part2 = contB.get(locToCheck);
            if (x.text.equals(part2.text))
            {
                h.count++;
            }
            else
            {
                // mark as done.
                a.get(0).flag = 1;
            }
        }

        if (changeCount == 0)
        {
            a.get(0).flag = 1;
        }
    }

    private void show()
    {
        for (Line c : contB)
        {
            printOut(c);
        }
    }

    private static class Line
    {
        public int lineStat = 0;
        public final String text;
        public int ic = -1;

        public Line(String t)
        {
            text = t;
        }
    }

    private static class ContBlock
    {
        int flag = 0;
        int loc = -1;
        int count = 0;
    }
}
