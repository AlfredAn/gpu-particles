package test.gpuparticles;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import test.GLUtil;
import test.InputHandler;
import test.ShaderProgram;
import test.TestApp;
import test.Timing;
//implement aspect ratio switching in shader (??)
public final class TestGPUParticles extends TestApp
{
    private static final int
            P_COUNT = 4194304,
            P_TEXWIDTH,
            P_TEXHEIGHT,
            SIM_FPS = 144,
            SIM_ITERATIONS = 32;
    
    private static final float
            SIM_TIMEFACTOR = 1f,
            SIM_SLOWFACTOR = 1f;
    
    static
    {
        int w = 1, h = 1, c = P_COUNT;
        
        while (c > 1)
        {
            if (c % 2 != 0)
            {
                throw new RuntimeException("P_COUNT must be a power of two!");
            }
            w *= 2;
            c /= 2;
            
            if (c % 2 != 0)
            {
                if (c == 1)
                {
                    break;
                }
                throw new RuntimeException("P_COUNT must be a power of two!");
            }
            h *= 2;
            c /= 2;
        }
        
        P_TEXWIDTH = w;
        P_TEXHEIGHT = h;
    }
    
    private ShaderProgram shaderInit, shaderUpdate, shaderDraw;
    private int vaoQuad, vboQuad, vaoPart, vboPart;
    private ParticleFramebuffer fboSrc, fboTar;
    private FloatBuffer matrixBuffer;
    private Matrix4f proj;
    
    @Override
    public void init()
    {
        loadShaders();
        createQuad();
        createParticles();
        createFramebuffers();
        createMatrix();
        
        GLUtil.checkErrors();
    }
    
    private void loadShaders()
    {
        shaderInit = new ShaderProgram(
                "shaders/gpuparticles/particles.vert",
                "shaders/gpuparticles/init.frag",
                new String[] {"in_Position", "in_TexCoord"},
                new String[] {"u_TexSize"});
        
        shaderUpdate = new ShaderProgram(
                "shaders/gpuparticles/particles.vert",
                "shaders/gpuparticles/update.frag",
                new String[] {"in_Position", "in_TexCoord"},
                new String[] {"u_Sampler", "u_TimeStep", "u_TexSize", "u_Iterations"});
        
        shaderDraw = new ShaderProgram(
                "shaders/gpuparticles/draw.vert",
                "shaders/gpuparticles/draw.frag",
                new String[] {"in_TexCoord"},
                new String[] {"u_Sampler", "u_Matrix"});
        
        GLUtil.checkErrors();
    }
    
    private void createQuad()
    {
        FloatBuffer buf = BufferUtils.createFloatBuffer(24);
        
        buf.put(-1); //X1
        buf.put(-1); //Y1
        buf.put(0); //U
        buf.put(0); //V
        
        buf.put( 1); //X2
        buf.put( 1); //Y2
        buf.put(1); //U
        buf.put(1); //V
        
        buf.put(-1); //X3
        buf.put( 1); //Y3
        buf.put(0); //U
        buf.put(1); //V
        
        
        buf.put(-1); //X1
        buf.put(-1); //Y1
        buf.put(0); //U
        buf.put(0); //V
        
        buf.put( 1); //X2
        buf.put(-1); //Y2
        buf.put(1); //U
        buf.put(0); //V
        
        buf.put( 1); //X3
        buf.put( 1); //Y3
        buf.put(1); //U
        buf.put(1); //V
        
        buf.flip();
        
        vaoQuad = glGenVertexArrays();
        glBindVertexArray(vaoQuad);
        
        vboQuad = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboQuad);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_STATIC_DRAW);
        
        //position
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4*4, 0);
        
        //texture coords
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4*4, 2*4);
        
        glBindVertexArray(0);
        
        GLUtil.checkErrors();
    }
    
    private void createParticles()
    {
        FloatBuffer buf = BufferUtils.createFloatBuffer(P_COUNT * 2);
        
        for (int y = 0; y < P_TEXHEIGHT; y++)
        {
            for (int x = 0; x < P_TEXWIDTH; x++)
            {
                buf.put((float)x / P_TEXWIDTH);
                buf.put((float)y / P_TEXHEIGHT);
            }
        }
        
        buf.flip();
        
        vaoPart = glGenVertexArrays();
        glBindVertexArray(vaoPart);
        
        vboPart = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboPart);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_STATIC_DRAW);
        
        //particle texture coord
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2*4, 0);
        
        glBindVertexArray(0);
        
        GLUtil.checkErrors();
    }
    
    private void createFramebuffers()
    {
        fboSrc = new ParticleFramebuffer(P_TEXWIDTH, P_TEXHEIGHT);
        fboTar = new ParticleFramebuffer(P_TEXWIDTH, P_TEXHEIGHT);
        
        //init particles on framebuffer
        //fboSrc.bindTexture();
        fboTar.bind(GL_DRAW_FRAMEBUFFER);
        
        shaderInit.use();
        glUniform2f(shaderInit.getUniformLocation(0), P_TEXWIDTH, P_TEXHEIGHT);
        glBindVertexArray(vaoQuad);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
        glUseProgram(0);
        
        //glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        
        swapBuffers();
        
        GLUtil.checkErrors();
    }
    
    private void swapBuffers()
    {
        ParticleFramebuffer temp = fboSrc;
        fboSrc = fboTar;
        fboTar = temp;
    }
    
    private void createMatrix()
    {
        proj = new Matrix4f();
        matrixBuffer = BufferUtils.createFloatBuffer(16);
        
        updateMatrix();
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
        
        shaderDraw.use();
        glUniformMatrix4(shaderDraw.getUniformLocation(1), false, matrixBuffer);
        glUseProgram(0);
        
        GLUtil.checkErrors();
    }
    
    @Override
    public void destroy()
    {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        
        glUseProgram(0);
        
        glDeleteBuffers(vboQuad);
        glDeleteVertexArrays(vaoQuad);
        
        glDeleteBuffers(vboPart);
        glDeleteVertexArrays(vaoPart);
        
        fboSrc.destroy();
        fboTar.destroy();
        
        shaderInit.destroy();
        shaderUpdate.destroy();
        shaderDraw.destroy();
        
        GLUtil.checkErrors();
    }
    
    @Override
    public void update()
    {
        float tf = 1;
        if (!InputHandler.isKeyDown(InputHandler.KEY_SLOWDOWN))
        {
            tf = SIM_SLOWFACTOR;
        }
        shaderUpdate.use();
        glActiveTexture(GL_TEXTURE0);
        fboSrc.bindTexture();
        glUniform1i(shaderUpdate.getUniformLocation(0), 0);
        glUniform1f(shaderUpdate.getUniformLocation(1), tf * SIM_TIMEFACTOR * ((SIM_FPS == 0) ? (float)Timing.getDelta() : (1f / SIM_FPS)));
        glUniform2f(shaderUpdate.getUniformLocation(2), P_TEXWIDTH, P_TEXHEIGHT);
        glUniform1i(shaderUpdate.getUniformLocation(3), SIM_ITERATIONS);
        
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        fboTar.bind(GL_FRAMEBUFFER);
        
        glBindVertexArray(vaoQuad);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
        
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glUseProgram(0);
        swapBuffers();
        
        GLUtil.checkErrors();
    }
    
    @Override
    public void draw()
    {
        glClearColor(0f, 0f, 0f, 0f);
        glClear(GL_COLOR_BUFFER_BIT);
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);
        
        shaderDraw.use();
        glActiveTexture(GL_TEXTURE0);
        fboSrc.bindTexture();
        glUniform1i(shaderDraw.getUniformLocation(0), 0);
        
        glBindVertexArray(vaoPart);
        glDrawArrays(GL_POINTS, 0, P_COUNT);
        glBindVertexArray(0);
        
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
        
        glDisable(GL_BLEND);
        
        /*fboSrc.bind(GL_READ_FRAMEBUFFER);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glBlitFramebuffer(
                0, 0, P_TEXWIDTH, P_TEXHEIGHT,
                0, 0, P_TEXWIDTH, P_TEXHEIGHT,
                GL_COLOR_BUFFER_BIT, GL_NEAREST);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);*/
        
        if (SIM_FPS != 0)
        {
            Display.sync(SIM_FPS);
        }
        
        GLUtil.checkErrors();
    }
}