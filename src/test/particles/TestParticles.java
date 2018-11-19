package test.particles;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import test.TestApp;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import test.InputHandler;
import test.ShaderProgram;
import test.Test;
import test.Timing;

public class TestParticles extends TestApp
{
    private static final int P_AMOUNT = 50000;
    
    private static final int elementCount = 6, elementSize = 4,
            stride = elementCount * elementSize;
    
    private FloatBuffer vertexBuffer;
    //private IntBuffer indexBuffer;
    private FloatBuffer matrixBuffer;
    
    private double[] partX, partY, partXV, partYV;
    
    private double time = 0;
    
    private int vao, vbo, vboi;
    private ShaderProgram particleShader;
    
    private Matrix4f proj, view, MVP;
    
    @Override
    public void init()
    {
        InputHandler.setMouseLock(false);
        
        partX = new double[P_AMOUNT];
        partY = new double[P_AMOUNT];
        partXV = new double[P_AMOUNT];
        partYV = new double[P_AMOUNT];
        
        for (int i = 0; i < P_AMOUNT; i++)
        {
            //double dir = Math.random() * Math.PI * 2;
            //double dis = Math.random() * 0.05 + 0.45;
            double dir = Math.PI * 2 * Math.random();
            double dis = Math.random() * 0.5 + 0.5;
            
            partX[i] = dis * Math.cos(dir);
            partY[i] = dis * Math.sin(dir);
            
            //dir = Math.random() * Math.PI * 2;
            //dis = Math.random();
            dir += Math.PI / 2;//Math.PI * (0.375 + Math.random() * 0.25);
            //dis = 0.75 + Math.random() * 0.5;
            dis = 0;
            
            partXV[i] = dis * Math.cos(dir);
            partYV[i] = dis * Math.sin(dir);
        }
        
        loadShaders();
        createVAO();
        
        proj = new Matrix4f();
        view = new Matrix4f();
        MVP = new Matrix4f();
        
        DisplayMode dm = Display.getDisplayMode();
        double w = dm.getWidth();
        double h = dm.getHeight();
        float xScale = (float)(h / w);
        
        proj.scale(new Vector3f(xScale, 1, 1));
        
        matrixBuffer = BufferUtils.createFloatBuffer(16);
        proj.store(matrixBuffer);
        matrixBuffer.flip();
        
        particleShader.use();
        glUniformMatrix4(particleShader.getUniformLocation(0), false, matrixBuffer);
        glUseProgram(0);
    }
    
    private void loadShaders()
    {
        particleShader = new ShaderProgram(
                "shaders/particles/particles.vert",
                "shaders/particles/particles.frag",
                new String[] {"in_Position", "in_Color"},
                new String[] {"u_Matrix"});
    }
    
    private void createVAO()
    {
        Test.log("Creating VAO...");
        
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        
        createBuffers();
        
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);
        
        //vboi = glGenBuffers();
        //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboi);
        //glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STREAM_DRAW);
        
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, stride, 2*4);
        
        //glBindBuffer(GL_ARRAY_BUFFER, 0);
        //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    
    private void createBuffers()
    {
        vertexBuffer = BufferUtils.createFloatBuffer(P_AMOUNT * elementCount);
        //indexBuffer = BufferUtils.createIntBuffer(P_AMOUNT);
        
        updateBuffers();
    }
    
    private void updateBuffers()
    {
        vertexBuffer.clear();
        //indexBuffer.clear();
        
        for (int i = 0; i < P_AMOUNT; i++)
        {
            vertexBuffer.put((float)partX[i]);
            vertexBuffer.put((float)partY[i]);
            
            float isp = (float)Math.min(0.05 / (partXV[i] * partXV[i] + partYV[i] * partYV[i]), 1);
            float sp = (float)Math.min(partXV[i] * partXV[i] + partYV[i] * partYV[i], 1);
            
            float red = isp;
            float green = (isp + sp) * (isp * sp) * 10;
            float blue = sp;
            float alpha = 1;
            
            vertexBuffer.put(red);
            vertexBuffer.put(green);
            vertexBuffer.put(blue);
            vertexBuffer.put(alpha);
        }
        vertexBuffer.flip();
        
        /*for (int i = 0; i < P_AMOUNT; i++)
        {
            indexBuffer.put(i);
        }
        indexBuffer.flip();*/
    }
    
    @Override
    public void destroy()
    {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vbo);
        
        //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        //glDeleteBuffers(vboi);
        
        glBindVertexArray(0);
        glDeleteVertexArrays(vao);
        
        glUseProgram(0);
        particleShader.destroy();
    }
    
    @Override
    public void update()
    {
        double cx = 0, cy = 0;
        
        DisplayMode dm = Display.getDisplayMode();
        double w = dm.getWidth();
        double h = dm.getHeight();
        double xScale = h / w;
        double gx = (InputHandler.getMouseX() / w * 2 - 1) / xScale;
        double gy = InputHandler.getMouseY() / h * 2 - 1;
        
        time += Timing.getDelta();
        applyGravity(0, 0, 0.1);
        //applyGravity(gx, gy, -0.01);
        
        //double timeScale = InputHandler.isKeyDown(InputHandler.KEY_SLOWDOWN) ? 0.01 : 1;
        
        double minX = -1./xScale, maxX = 1./xScale;
        double minY = -1, maxY = 1;
        
        for (int i = 0; i < P_AMOUNT; i++)
        {
            partYV[i] -= 0.1 * Timing.getDelta();
            
            partX[i] += partXV[i] * Timing.getDelta();
            partY[i] += partYV[i] * Timing.getDelta();
            
            /*while (partX[i] <= minX || partX[i] >= maxX || partY[i] <= minY || partY[i] >= maxY)
            {
                if (partX[i] <= minX)
                {
                    partX[i] = 2 * minX - partX[i];
                    partXV[i] = -partXV[i];
                }
                else if (partX[i] >= maxX)
                {
                    partX[i] = 2 * maxX - partX[i];
                    partXV[i] = -partXV[i];
                }

                if (partY[i] <= minY)
                {
                    partY[i] = 2 * minY - partY[i];
                    partYV[i] = -partYV[i];
                }
                else if (partY[i] >= maxY)
                {
                    partY[i] = 2 * maxY - partY[i];
                    partYV[i] = -partYV[i];
                }
            }*/
            
            cx += partX[i];
            cy += partY[i];
        }
        
        cx /= P_AMOUNT;
        cy /= P_AMOUNT;
        
        cx = 0;
        cy = 0;
        
        view.setIdentity();
        view.translate(new Vector3f((float)-cx, (float)-cy, 0));
        
        Matrix4f.mul(proj, view, MVP);
        matrixBuffer.clear();
        MVP.store(matrixBuffer);
        matrixBuffer.flip();
        
        particleShader.use();
        glUniformMatrix4(particleShader.getUniformLocation(0), false, matrixBuffer);
        glUseProgram(0);
        
        updateBuffers();
        
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    private void applyGravity(double gx, double gy, double g)
    {
        for (int i = 0; i < P_AMOUNT; i++)
        {
            double dx = partX[i] - gx;
            double dy = partY[i] - gy;
            double distSqr = dx * dx + dy * dy;
            double acc = -g / Math.max(distSqr, 1./1024) * Timing.getDelta();
            double acd = Math.atan2(dy, dx);
            
            partXV[i] += acc * Math.cos(acd);
            partYV[i] += acc * Math.sin(acd);
        }
    }
    
    @Override
    public void draw()
    {
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);
        
        particleShader.use();
        
        glBindVertexArray(vao);
        glDrawArrays(GL_POINTS, 0, P_AMOUNT);
        
        glUseProgram(0);
    }
}
