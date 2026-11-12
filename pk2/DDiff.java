package pk2;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class DDiff
{
    public static final int lineStat_new = 0;
    public static final int lineStat_changed = 1;
    public static final int lineStat_deleted = 2;

    private static final int ic_goNext = -836;

    private final ArrayList<DDiff.Line> contA = new ArrayList<>();
    private final ArrayList<DDiff.Line> contB = new ArrayList<>();
    private final ArrayList<DDiff.Line> deletedItems = new ArrayList<>();

    int blockCounter = 0;
    int headMatchedLocation = 0;

    public DDiff(Path f1, Path f2)
    {
        loadAandB(f1, f2);
        diff();
    }

    private void diff()
    {
        // make sure we never get stuck in a never ending loop
        int breaker = 0;
        int superMax = contA.size() + 10;

        final int size = contA.size();
        int i = 0;
        while (i < size)
        {
            int nextLine = doLine(i, contA.get(i));
            if (nextLine == -1)
            {
                i++;
            }
            else
            {
                i = nextLine;
            }

            if (breaker++ > superMax)
            {
                break;
            }
        }
        slipDeletedCrap();
    }

    private void loadAandB(Path f1, Path f2)
    {
        try (BufferedReader br = Files.newBufferedReader(f1))
        {
            String s;
            while ((s = br.readLine()) != null)
            {
                contA.add(new DDiff.Line(s));
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
                contB.add(new DDiff.Line(s));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private int doLine(int i, DDiff.Line line)
    {
        final ArrayList<DDiff.ContBlock> als = new ArrayList<>();
        als.add(new DDiff.ContBlock() );
        findHeadMatches(als, line);
        // no 'anchor'
        if (als.isEmpty())
        {
            // no match -- so this line is missing from the other file. 'k.
            // must have been deleted; 100% - because there is no match.
            line.ic = blockCounter;
            deletedItems.add(line);
            line.lineStat = lineStat_deleted;
            //Main.w("Deleted => ", a.txt, " >>", a.poop);
            return -1;
        }

        // find continous block
        int i2 = i+1;
        for (; i2 < contA.size(); i2++)
        {
            final DDiff.Line st = contA.get(i2);
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
            return i2;
        }

        final DDiff.ContBlock block = als.get(0);
        if (block.count == 1)
        {
            contB.get(block.loc).lineStat = lineStat_changed;
            contB.get(block.loc).ic = ++blockCounter;
            headMatchedLocation = block.loc + 1;
        }
        else
        {
            for (int b = 0; b < block.count; b++)
            {
                contB.get(block.loc + b).lineStat = lineStat_changed;
                contB.get(block.loc + b).ic = ++blockCounter;
                headMatchedLocation = block.loc + b + 1;
            }
        }
        return i2;
    }

    private void slipDeletedCrap()
    {
        while (!deletedItems.isEmpty())
        {
            putInBList(deletedItems.remove(0));
        }
    }

    private void putInBList(DDiff.Line s)
    {
        if (s.ic == 0)
        {
            for (int u = 0; u< contB.size(); u++)
            {
                final DDiff.Line xb = contB.get(u);
                if (xb.ic != ic_goNext)
                {
                    contB.add(u, s);
                    break;
                }
            }
            s.ic = ic_goNext;
            return;
        }

        int i=0;
        for (; i< contB.size(); i++)
        {
            final DDiff.Line xb = contB.get(i);
            if (xb.ic == s.ic)
            {
                break;
            }
        }

        boolean noAdd = true;
        ++i;
        for (; i< contB.size(); i++)
        {
            final DDiff.Line xb = contB.get(i);
            if (xb.ic != ic_goNext)
            {
                contB.add(i, s);
                noAdd = false;
                break;
            }
        }

        if (noAdd)
        {
            contB.add(s);
        }
        s.ic = ic_goNext;
    }
    
    private void findHeadMatches(ArrayList<DDiff.ContBlock> als, DDiff.Line x)
    {
        for (int i = headMatchedLocation; i < contB.size(); i++)
        {
            DDiff.Line b = contB.get(i);
            if (x.text.equals(b.text))
            {
                DDiff.ContBlock xy = new DDiff.ContBlock();
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

    private void removeLowerValues(ArrayList<DDiff.ContBlock> a)
    {
        final DDiff.ContBlock[] heads = a.toArray(new DDiff.ContBlock[0]);
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
            final DDiff.ContBlock xy = heads[i];
            if (xy.count >= maxVal)
            {
                a.add(xy);
            }
        }
    }

    private void findMatches(ArrayList<DDiff.ContBlock> block, DDiff.Line line)
    {
        final DDiff.ContBlock[] heads = block.toArray(new DDiff.ContBlock[0]);
        block.clear();
        block.add(heads[0]);

        int changeCount = 0;
        for (int i = 1; i < heads.length; i++)
        {
            final DDiff.ContBlock h = heads[i];
            int locToCheck = h.loc + h.count; // need to check the 'next' item.
            block.add(h);

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
            final DDiff.Line part2 = contB.get(locToCheck);
            if (line.text.equals(part2.text))
            {
                h.count++;
            }
            else
            {
                // mark as done.
                block.get(0).flag = 1;
            }
        }

        if (changeCount == 0)
        {
            block.get(0).flag = 1;
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
