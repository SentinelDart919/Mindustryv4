package arc.scene;

import arc.modules.Module;
import arc.scene.ui.layout.Table;
import arc.Core;

public class SceneModule extends Module{
    public Group root = new Group(){
        @Override
        public void draw(){}
        @Override
        public void act(float delta){
            for(Element child : getChildren()){
                child.act(delta);
            }
        }
    };
    public Table stage = new Table();

    public void act(){
        root.act(Core.graphics.getDeltaTime());
    }

    protected void loadSkin(){
    }
    
    @Override
    public void init(){
        stage.fillParent = true;
        root.addChild(stage);
    }
    
    @Override
    public void update(){
        root.act(Core.graphics.getDeltaTime());
    }
    
    @Override
    public void resize(int width, int height){
        stage.setSize(width, height);
    }
}
