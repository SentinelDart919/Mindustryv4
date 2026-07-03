package arc.modules;

public abstract class Module{
    public void update(){}
    public void init(){}
    public void preInit(){}
    public void pause(){}
    public void resume(){}
    public void dispose(){}
    public void resize(int width, int height){ resize(); }
    public void resize(){}
}