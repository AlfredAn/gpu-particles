package test;

import java.util.ArrayList;
import org.lwjgl.opengl.Display;

public final class Timing
{
    private Timing() {}
    
    private static long lastTime;
    private static ArrayList<Long> frames;
    private static boolean first;
    
    private static double delta;
    private static long deltaNano;
    
    public static void init()
    {
        Test.log("Starting timer...");
        
        lastTime = System.nanoTime();
        first = true;
        
        frames = new ArrayList<>(60);
    }
    
    public static void update()
    {
        long time = System.nanoTime();
        deltaNano = (time - lastTime);
        delta = (double)deltaNano / 1000000000;
        lastTime = time;
        
        frames.add(time);
        
        for (int i = 0; i < frames.size(); i++)
        {
            if (time - (long)frames.get(i) >= 1000000000)
            {
                frames.remove(i);
                i--;
            }
            else
            {
                break;
            }
        }
        
        if (first)
        {
            delta = Math.min(delta, 1./Display.getDesktopDisplayMode().getFrequency());
            first = false;
        }
        
        delta = Math.min(delta, .2);
    }
    
    public static double getDelta()
    {
        return delta;
    }
    
    public static long getDeltaNano()
    {
        return deltaNano;
    }
    
    public static int getFPS()
    {
        return frames.size();
    }
    
    private static int printTime = 0;
    
    public static void printFPS()
    {
        printFPS(0);
    }
    public static void printFPS(double interval)
    {
        if (interval <= 0)
        {
            pfps();
            return;
        }
        
        if (printTime <= 0)
        {
            pfps();
            printTime += (int)(interval * 1000000000);
        }
        
        printTime -= getDeltaNano();
    }
    
    private static void pfps()
    {
        Test.log("Fps: " + getFPS());
    }
}


















