package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram
{
    private final int vert, frag, prog;
    private final int[] uniform;
    
    public ShaderProgram(String vertPath, String fragPath, String[] attribs, String[] uniforms)
    {
        if (attribs == null)
        {
            attribs = new String[0];
        }
        
        if (uniforms == null)
        {
            uniforms = new String[0];
        }
        
        vert = loadShader(vertPath, GL_VERTEX_SHADER);
        frag = loadShader(fragPath, GL_FRAGMENT_SHADER);
        
        prog = glCreateProgram();
        glAttachShader(prog, vert);
        glAttachShader(prog, frag);
        
        for (int i = 0; i < attribs.length; i++)
        {
            glBindAttribLocation(prog, i, attribs[i]);
        }
        
        glLinkProgram(prog);
        glValidateProgram(prog);
        
        int status = glGetProgrami(prog, GL_LINK_STATUS);
        
        if (status != GL_TRUE)
        {
            String log = glGetProgramInfoLog(prog, 1024);
            Test.forceExit("Error linking shader \"" + vertPath + "\" or \"" + fragPath + "\":\n" + log);
        }
        
        uniform = new int[uniforms.length];
        for (int i = 0; i < uniforms.length; i++)
        {
            uniform[i] = glGetUniformLocation(prog, uniforms[i]);
        }
        
        //Test.checkGLErrors("ShaderProgram()");
    }
    
    public void destroy()
    {
        glUseProgram(0);
        glDetachShader(prog, vert);
        glDetachShader(prog, frag);
        
        glDeleteShader(vert);
        glDeleteShader(frag);
        glDeleteProgram(prog);
        
        //Test.checkGLErrors("ShaderProgram.destroy()");
    }
    
    public void use()
    {
        glUseProgram(prog);
    }
    
    public int getUniformLocation(int i)
    {
        return uniform[i];
    }
    
    private static int loadShader(String filename, int type)
    {
        StringBuilder shaderSource = new StringBuilder();
        int shaderId = 0;
        BufferedReader reader = null;
        
        try
        {
            reader = new BufferedReader(new InputStreamReader(Util.getResourceAsStream(filename)));
            String line;
            
            while ((line = reader.readLine()) != null)
            {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        }
        catch (IOException e)
        {
            Test.forceExit("Error reading shader file: " + filename, e);
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    Test.forceExit("Error reading shader file: " + filename, e);
                }
            }
        }
        
        shaderId = glCreateShader(type);
        glShaderSource(shaderId, shaderSource);
        glCompileShader(shaderId);
        
        int status = glGetShaderi(shaderId, GL_COMPILE_STATUS);
        
        if (status != GL_TRUE)
        {
            String log = glGetShaderInfoLog(shaderId, 1024);
            Test.forceExit("Error compiling shader \"" + filename + "\":\n" + log);
        }
        
        //WorldOfCaves.checkGLErrors("Shader.loadShader(" + filename + ")");
        
        return shaderId;
    }
}





