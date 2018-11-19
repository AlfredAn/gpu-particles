package test;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import static org.lwjgl.opengl.GL11.*;

public final class DisplayHandler
{
    private DisplayHandler() {}
    
    public static void init()
    {
        Test.log("Creating display...");
        
        PixelFormat pixelFormat = new PixelFormat(32, 0, 24, 0, 0);
        
        ContextAttribs contextAttribs = new ContextAttribs(3, 3)
                .withForwardCompatible(true)
                .withProfileCore(true);
        
        DisplayMode displayMode = getDefaultDisplayMode();
        
        try
        {
            Display.setDisplayMode(displayMode);
            Display.create(pixelFormat, contextAttribs);
        }
        catch (LWJGLException e)
        {
            Test.forceExit("Error creating display.", e);
        }
        
        Display.setResizable(false);
        glViewport(0, 0, displayMode.getWidth(), displayMode.getHeight());
    }
    
    public static void toggleFullscreen()
    {
        setFullscreen(!Display.isFullscreen());
    }
    
    public static void setFullscreen(boolean fullscreen)
    {
        Test.log("Fullscreen set to " + fullscreen);
        
        if (fullscreen != Display.isFullscreen())
        {
            try
            {
                if (fullscreen)
                {
                    Display.setDisplayModeAndFullscreen(Display.getDesktopDisplayMode());
                }
                else
                {
                    Display.setDisplayMode(getDefaultDisplayMode());
                }
            }
            catch (LWJGLException e)
            {
                Test.forceExit("Error toggling fullscreen mode.", e);
            }
        }
        
        DisplayMode displayMode = Display.getDisplayMode();
        glViewport(0, 0, displayMode.getWidth(), displayMode.getHeight());
    }
    
    public static int getDefaultWindowWidth()
    {
        int dw = Display.getDesktopDisplayMode().getWidth();
        
        return Math.min(Math.max(dw / 2, 1360), dw * 3/4);
    }
    
    public static int getDefaultWindowHeight()
    {
        int dh = Display.getDesktopDisplayMode().getHeight();
        
        return Math.min(Math.max(dh / 2, 768), dh * 3/4);
    }
    
    public static DisplayMode getDefaultDisplayMode()
    {
        return new DisplayMode(getDefaultWindowWidth(), getDefaultWindowHeight());
    }
}
