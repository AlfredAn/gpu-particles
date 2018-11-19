package test;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

public final class InputHandler
{
    private InputHandler() {}
    
    public static final int
            KEY_EXIT = 0, KEY_FULLSCREEN = 1, KEY_SLOWDOWN = 2;
    private static final int KEY_AMOUNT = 3, KEY_ALTS = 1, MOUSE_BUTTONS = 2;
    
    private static int[][] key;
    private static boolean[] keyPressed, keyReleased, mousePrev;
    private static boolean mouseLock;
    private static double mouseX, mouseY, mouseDX, mouseDY, mouseXPrev, mouseYPrev;
    
    private static double mouseSensitivity = 1./3;
    
    public static void init()
    {
        Test.log("Setting up keybindings...");
        
        Mouse.setClipMouseCoordinatesToWindow(false);
        
        key = new int[KEY_AMOUNT][KEY_ALTS];
        
        for (int i = 0; i < KEY_AMOUNT; i++)
        {
            for (int j = 0; j < KEY_ALTS; j++)
            {
                key[i][j] = -1;
            }
        }
        
        mousePrev = new boolean[MOUSE_BUTTONS];
        
        for (int i = 0; i < MOUSE_BUTTONS; i++)
        {
            mousePrev[i] = Mouse.isButtonDown(i);
        }
        
        /////set controls (left mouse button = -1000, right mouse button = -1001)
        key[KEY_EXIT][0] = Keyboard.KEY_ESCAPE;
        key[KEY_FULLSCREEN][0] = Keyboard.KEY_F11;
        key[KEY_SLOWDOWN][0] = Keyboard.KEY_SPACE;
        /////
        
        keyPressed = new boolean[KEY_AMOUNT];
        keyReleased = new boolean[KEY_AMOUNT];
        
        for (int i = 0; i < KEY_AMOUNT; i++)
        {
            keyPressed[i] = false;
            keyReleased[i] = false;
        }
        
        mouseLock = true;
        mouseX = Mouse.getX();
        mouseY = Mouse.getY();
        mouseDX = 0;
        mouseDY = 0;
        
        resetMousePos();
        Mouse.setGrabbed(true);
    }
    
    public static void update()
    {
        for (int i = 0; i < KEY_AMOUNT; i++)
        {
            keyPressed[i] = false;
            keyReleased[i] = false;
        }
        
        while (Keyboard.next())
        {
            int k = getKeyId(Keyboard.getEventKey());
            
            if (k != -1)
            {
                if (Keyboard.getEventKeyState())
                {
                    keyPressed[k] = true;
                }
                else
                {
                    keyReleased[k] = true;
                }
            }
        }
        
        for (int i = 0; i < KEY_AMOUNT; i++)
        {
            for (int j = 0; j < KEY_ALTS; j++)
            {
                if (key[i][j] <= -1000)
                {
                    int b = mb(key[i][j]);
                    boolean prev = mousePrev[b];
                    boolean down = Mouse.isButtonDown(b);
                    
                    if (!prev && down)
                    {
                        keyPressed[i] = true;
                    }
                    else if (prev && !down)
                    {
                        keyReleased[i] = true;
                    }
                }
            }
        }
        
        for (int i = 0; i < MOUSE_BUTTONS; i++)
        {
            mousePrev[i] = Mouse.isButtonDown(i);
        }
        
        mouseXPrev = mouseX;
        mouseYPrev = mouseY;
        mouseX = Mouse.getX();
        mouseY = Mouse.getY();
        mouseDX = mouseX - mouseXPrev;
        mouseDY = mouseY - mouseYPrev;
        
        if (mouseLock)
        {
            resetMousePos();
        }
    }
    
    private static int mb(int button)
    {
        if (button <= -1000)
        {
            return (-1000 - button);
        }
        else if (button >= 0)
        {
            return button;
        }
        
        throw new IllegalArgumentException("Invalid key id: " + button);
    }
    
    private static void resetMousePos()
    {
        DisplayMode dm = Display.getDisplayMode();
        double cx = (double)dm.getWidth()/2;
        double cy = (double)dm.getHeight()/2;
        
        Mouse.setCursorPosition((int)cx, (int)cy);
        mouseX = (int)cx;
        mouseY = (int)cy;
    }
    
    public static void setMouseLock(boolean lock)
    {
        if (mouseLock != lock)
        {
            if (lock)
            {
                resetMousePos();
            }
            
            Mouse.setGrabbed(lock);
            mouseLock = lock;
        }
    }
    
    public static boolean getMouseLock()
    {
        return mouseLock;
    }
    
    public static void setMouseSensitivity(double sensitivity)
    {
        mouseSensitivity = sensitivity;
    }
    
    public static double getMouseSensitivity()
    {
        return mouseSensitivity;
    }
    
    public static boolean isKeyDown(int id)
    {
        for (int i = 0; i < KEY_ALTS; i++)
        {
            if (key[id][i] != -1)
            {
                if (key[id][i] >= 0 && Keyboard.isKeyDown(key[id][i]))
                {
                    return true;
                }
                else if (key[id][i] <= -1000 && Mouse.isButtonDown(mb(key[id][i])))
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public static boolean wasKeyPressed(int id)
    {
        return keyPressed[id];
    }
    
    public static boolean wasKeyReleased(int id)
    {
        return keyReleased[id];
    }
    
    private static int getKeyId(int keyCode)
    {
        for (int i = 0; i < KEY_AMOUNT; i++)
        {
            for (int j = 0; j < KEY_ALTS; j++)
            {
                if (key[i][j] == keyCode)
                {
                    return i;
                }
            }
        }
        
        return -1;
    }
    
    public static boolean isKey(int keyCode, int id)
    {
        for (int i = 0; i < KEY_ALTS; i++)
        {
            if (key[id][i] == keyCode)
            {
                return true;
            }
        }
        
        return false;
    }
    
    public static double getMouseX()
    {
        return mouseX;
    }
    
    public static double getMouseY()
    {
        return mouseY;
    }
    
    public static double getMouseDX()
    {
        return mouseDX * mouseSensitivity;
    }
    
    public static double getMouseDY()
    {
        return mouseDY * mouseSensitivity;
    }
    
    public static double getMouseRawDX()
    {
        return mouseDX;
    }
    
    public static double getMouseRawDY()
    {
        return mouseDY;
    }
}