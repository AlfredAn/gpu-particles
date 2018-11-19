package test;

public abstract class TestApp
{
    public TestApp()
    {
        init();
    }
    
    public abstract void init();
    public abstract void destroy();
    public abstract void update();
    public abstract void draw();
}