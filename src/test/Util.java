package test;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Util
{
    private Util() {}
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    public static String getCurrentTimeStamp()
    {
        Date now = new Date();
        return sdf.format(now);
    }
    
    private static final Class cl = new Util().getClass();
    
    public static String getAbsolutePath(String path)
    {
        return cl.getResource("/" + path).getPath();
    }
    
    public static InputStream getResourceAsStream(String path)
    {
        return cl.getResourceAsStream("/" + path);
    }
}
