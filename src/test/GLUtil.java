package test;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.OpenGLException;

public class GLUtil
{
    private GLUtil() {}
    
    private static final boolean checkGLErrors = true;
    
    public static void checkErrors()
    {
        if (!checkGLErrors)
        {
            return;
        }
        
        int e = glGetError();
        
        if (e != 0)
        {
            throw new OpenGLException(e);
        }
    }
}