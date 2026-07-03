package arc.modules;

import arc.ApplicationListener;
import arc.Core;
import arc.Input;
import arc.struct.ObjectMap;
import arc.struct.Seq;

public abstract class ModuleCore implements ApplicationListener {
    protected ObjectMap<Class<? extends Module>, Module> modules = new ObjectMap<>();
    protected Seq<Module> modulearray = new Seq<>();

    abstract public void initModules();
    public void preInit(){}
    public void postInit(){}
    public void updateInput(){ }

    protected <N extends Module> void module(N t){
        modules.put(t.getClass(), t);
        modulearray.add(t);
        t.preInit();
    }

    @Override
    public void resize(int width, int height){
        for(Module module : modulearray){
            module.resize(width, height);
        }
    }

    @Override
    public final void init(){
        initModules();
        preInit();
        for(Module module : modulearray){
            module.init();
        }
        postInit();
    }

    @Override
    public void update(){
        for(Module module : modulearray){
            module.update();
        }
        updateInput();
    }

    @Override
    public void pause(){
        for(Module module : modulearray) module.pause();
    }

    @Override
    public void resume(){
        for(Module module : modulearray) module.resume();
    }

    @Override
    public void dispose(){
        for(Module module : modulearray) module.dispose();
    }
}