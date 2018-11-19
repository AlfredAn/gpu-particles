package test;

import java.io.File;
import org.lwjgl.opengl.Display;
import test.gpuparticles.TestGPUParticles;

public final class Test
{
    private static final boolean isDist = true;
    
    private static boolean exit = false;
    
    private static TestApp test;
    
    private Test() {}
    
    public static void main(String[] args)
    {
        startup();
        mainLoop();
        shutdown();
    }
    
    private static void startup()
    {
        log("Starting up...");
        
        initLibraries();
        DisplayHandler.init();
        DisplayHandler.setFullscreen(true);
        InputHandler.init();
        Timing.init();
        
        test = new TestGPUParticles();
        
        log("Startup successful!");
    }
    
    private static void mainLoop()
    {
        log("Starting main loop...");
        Timing.update();
        
        while (!exit)
        {
            update();
            draw();
            
            Display.update();
        }
    }
    
    private static void update()
    {
        Timing.update();
        //Timing.printFPS(1);
        InputHandler.update();
        
        if (InputHandler.isKeyDown(InputHandler.KEY_FULLSCREEN))
        {
            DisplayHandler.toggleFullscreen();
        }
        
        if (Display.isCloseRequested() || InputHandler.isKeyDown(InputHandler.KEY_EXIT))
        {
            exit();
        }
        
        if (test != null)
        {
            test.update();
        }
    }
    
    private static void draw()
    {
        if (test != null)
        {
            test.draw();
        }
    }
    
    public static void exit()
    {
        exit = true;
    }
    
    private static void shutdown()
    {
        log("Shutting down...");
        
        log("Destroying app...");
        test.destroy();
        
        log("Destroying display...");
        Display.destroy();
        
        log("Shutdown successful!");
    }
    
    public static void forceExit()
    {
        forceExit(null, null);
    }
    public static void forceExit(Throwable cause)
    {
        forceExit(null, cause);
    }
    public static void forceExit(String msg)
    {
        forceExit(msg, null);
    }
    public static void forceExit(String msg, Throwable cause)
    {
        if (msg != null)
        {
            logError(msg, cause);
        }
        else if (cause != null)
        {
            logError("Unknown error.", cause);
        }
        
        try
        {
            shutdown();
        }
        catch (Throwable t)
        {
            logError("Error while shutting down.", t);
        }
    }
    
    public static void log(String msg)
    {
        System.out.println(Util.getCurrentTimeStamp() + ": " + msg);
    }
    
    public static void logError(String msg)
    {
        logError(msg, null);
    }
    public static void logError(String msg, Throwable cause)
    {
        System.err.println(Util.getCurrentTimeStamp() + ": " + msg);
        
        if (cause != null)
        {
            cause.printStackTrace();
        }
    }
    
    private static void initLibraries()
    {
        if (isDist)
        {
            log("Setting LWJGL library path...");
            
            String os = System.getProperty("os.name");
            String natives = "";
            
            if (os.contains("Windows"))
            {
                natives = "windows";
            }
            else if (os.contains("Mac"))
            {
                natives = "macosx";
            }
            else if (os.contains("Linux"))
            {
                natives = "linux";
            }
            else if (os.contains("Solaris"))
            {
                natives = "solaris";
            }
            else
            {
                logError("Error: OS not recognized.");
                System.exit(-1);
            }
            
            System.setProperty("org.lwjgl.librarypath", new File("native/" + natives).getAbsolutePath());
        }
    }
}
