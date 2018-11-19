package test.gpuparticles;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL30.*;
import test.GLUtil;

final class ParticleFramebuffer
{
    public final int width, height;
    private final int tex, fbo;
    
    /*
    R: x
    G: y
    B: xv
    A: yv
    */
    
    ParticleFramebuffer(int width, int height)
    {
        if (width <= 0 || height <= 0)
        {
            throw new IllegalArgumentException("Negative framebuffer dimensions.");
        }
        
        this.width = width;
        this.height = height;
        
        tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, tex);
        
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, width, height, 0, GL_RGBA, GL_FLOAT, (ByteBuffer)null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        
        glBindTexture(GL_TEXTURE_2D, 0);
        
        fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, tex, 0);
        
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE)
        {
            throw new RuntimeException("Couldn't create framebuffer: Error " + status);
        }
        
        GLUtil.checkErrors();
    }
    
    void destroy()
    {
        glDeleteTextures(tex);
        glDeleteFramebuffers(fbo);
    }
    
    void bind(int target)
    {
        glBindFramebuffer(target, fbo);
    }
    
    void bindTexture()
    {
        glBindTexture(GL_TEXTURE_2D, tex);
    }
    
    int getFBO()
    {
        return fbo;
    }
    
    int getTexture()
    {
        return tex;
    }
}





















