package pk2;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AutoVersion
{

    public String init()
    {
        final SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yy.D.");

        return sdf.format(Calendar.getInstance().getTime()) + "0";
    }

    public String update(String s)
    {
        try
        {
            final String[] s2 = s.split("\\.");

            final String sod = s2[0] + "0".repeat(3 - s2[1].length()) + s2[1];
            final int oldDate = Integer.parseInt(sod);

            final Calendar cdr = Calendar.getInstance();
            final SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("yyDDD");
            int currentDate = Integer.parseInt(sdf.format(cdr.getTime()));

            sdf.applyPattern("yy.D.");

            if (oldDate < currentDate)
            {
                return sdf.format(cdr.getTime()) + "0";
            }
            else
            {
                return s2[0] + "." + s2[1] + "." + (Integer.parseInt(s2[2]) + 1);
            }
        }
        catch (Exception e)
        {
            return "";
        }
    }
}
