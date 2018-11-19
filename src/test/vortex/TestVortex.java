package test.vortex;

import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import test.InputHandler;
import test.ShaderProgram;
import test.TestApp;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import test.GLUtil;

public class TestVortex extends TestApp
{
    private static final int VERTEX_COUNT = 1024*16*16;
    private static final double TIME_FACTOR = 16*64*16;
    
    private static final int VERTEX_STRIDE = 1*4;
    
    private ShaderProgram shader;
    
    private Matrix4f proj;
    private FloatBuffer vertexBuffer, matrixBuffer;
    
    private long startTime;
    private double currentTime;
    
    private int vao, vbo;
    
    @Override
    public void init()
    {
        InputHandler.setMouseLock(false);
        
        startTime = System.nanoTime();
        currentTime = 0;
        
        proj = new Matrix4f();
        
        vertexBuffer = BufferUtils.createFloatBuffer(VERTEX_COUNT * VERTEX_STRIDE);
        matrixBuffer = BufferUtils.createFloatBuffer(16);
        
        loadShaders();
        createVAO();
        updateMatrix();
        
        glEnable(GL_BLEND);
        //glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBlendFunc(GL_ONE, GL_ONE);
        
        GLUtil.checkErrors();
    }
    
    private void loadShaders()
    {
        shader = new ShaderProgram(
                "shaders/vortex/vortex.vert",
                "shaders/vortex/vortex.frag",
                new String[] {"in_Radius"},
                new String[] {"u_Matrix", "u_Time"});
        
        GLUtil.checkErrors();
    }
    
    private void createVAO()
    {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        
        updateBuffer();
        
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 1, GL_FLOAT, false, VERTEX_STRIDE, 0);
        
        /*glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, VERTEX_STRIDE, VERTEX_POS_OFFSET);
        
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, VERTEX_STRIDE, VERTEX_COLOR_OFFSET);*/
        
        glBindVertexArray(0);
        
        GLUtil.checkErrors();
    }
    
    private void updateBuffer()
    {
        vertexBuffer.clear();
        
        for (int i = 0; i < VERTEX_COUNT; i++)
        {
            vertexBuffer.put((float)(((double)(VERTEX_COUNT - i - 1)) / VERTEX_COUNT));
        }
        
        vertexBuffer.flip();
        
        GLUtil.checkErrors();
    }
    
    private void updateMatrix()
    {
        DisplayMode dm = Display.getDisplayMode();
        double w = dm.getWidth();
        double h = dm.getHeight();
        float xScale = (float)(h / w);
        
        proj.setIdentity();
        proj.scale(new Vector3f(xScale, 1, 1));
        
        matrixBuffer.clear();
        proj.store(matrixBuffer);
        matrixBuffer.flip();
        
        shader.use();
        glUniformMatrix4(shader.getUniformLocation(0), false, matrixBuffer);
        glUseProgram(0);
        
        GLUtil.checkErrors();
    }
    
    @Override
    public void destroy()
    {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vbo);
        
        glBindVertexArray(0);
        glDeleteVertexArrays(vao);
        
        glUseProgram(0);
        shader.destroy();
        
        GLUtil.checkErrors();
    }
    
    @Override
    public void update()
    {
        long time = System.nanoTime();
        currentTime = ((double)(time - startTime)) / 1e9;
        //currentTime *= TIME_FACTOR * currentTime;
        
        currentTime = TIME_FACTOR * Math.pow(16, currentTime / 10);
        
        //updateBuffer();
        //updateMatrix();
        
        //update uniforms
        shader.use();
        glUniform1f(shader.getUniformLocation(1), (float)currentTime);
        glUseProgram(0);
        
        //glBindBuffer(GL_ARRAY_BUFFER, vbo);
        //glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
        //glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    @Override
    public void draw()
    {
        glClearColor(0f, 0f, 0f, 0f);
        glClear(GL_COLOR_BUFFER_BIT);
        
        shader.use();
        
        glBindVertexArray(vao);
        glDrawArrays(GL_LINE_STRIP, 0, VERTEX_COUNT);
        glBindVertexArray(0);
        
        glUseProgram(0);
        
        GLUtil.checkErrors();
    }
}
