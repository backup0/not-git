package pk2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Diffy200
{
    public static final int lineStat_new = 0;
    public static final int lineStat_changed = 1;
    public static final int lineStat_deleted = 2;

    private final ArrayList<Line> contA = new ArrayList<>();
    private final LinkedList<Line> contB = new LinkedList<>();
    private final LinkedList<Line> deletedItems = new LinkedList<>();

    public Diffy200(InputStream ins1, InputStream ins2)
    {
        loadAandB(ins1, ins2);
    }

    private void loadAandB(InputStream ins1, InputStream ins2)
    {

        if (ins1 == null || ins2 == null)
        {
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(ins1)))
        {
            String s;
            while ((s = br.readLine()) != null)
            {
                Line ai = new Line(s);
                contA.add(ai);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(ins2)))
        {
            String s;
            while ((s = br.readLine()) != null)
            {
                contB.add(new Line(s));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void tryDiff()
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
    }

    int blockCounter = 0;
    private int try0(int i, Line a)
    {
        final ArrayList<ContBlock> als = new ArrayList<>();
        als.add(new ContBlock());
        findHeadMatches(als, a);
        // no 'anchor'

        if (als.isEmpty())
        {
            // no match -- so this line is missing from the other file. 'k.
            // must have been deleted; 100% - because there is no match.
            a.ic = blockCounter;
            deletedItems.add(a);
            a.lineStat = lineStat_deleted;
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

        final ContBlock uu = als.get(0);
        for (int y = 0; y < uu.count; y++)
        {
            contB.get(uu.loc + y).lineStat = lineStat_changed;
            contB.get(uu.loc + y).ic = ++blockCounter;
            hm = uu.loc + y;// + 1;
                                //^^^^ << -- why did I add 1 before? Investigate there must be a reason
                ///.. but 1 is wrong.
        }

        hm++;

        return io;
    }

    private void slipDeletedCrap()
    {
        while (!deletedItems.isEmpty())
        {
            Line x = deletedItems.remove(0);
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

        int changeCount = 0;
        for (int i = 1; i < a.size(); i++)
        {
            final ContBlock cb = a.get(i);
            final int rIndex = cb.loc + cb.count;

            if (rIndex >= contB.size())
            {
                break;
            }

            final Line l = contB.get(rIndex);

            if (x.text.equals(l.text))
            {
                cb.count++;
                changeCount++;
            }
        }

        if (changeCount == 0)
        {
            a.get(0).flag = 1;
        }
    }

    public List<Line> getResult()
    {
        return contB;
    }

    public static class Line
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
