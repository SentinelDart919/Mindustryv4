package arc.input;

import arc.scene.event.InputListener;

public class InputAdapter extends InputListener implements InputProcessor{
    @Override
    public boolean keyDown(KeyCode keycode){
        return false;
    }

    @Override
    public boolean keyUp(KeyCode keycode){
        return false;
    }

    @Override
    public boolean keyTyped(char character){
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, KeyCode button){
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, KeyCode button){
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer){
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY){
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY){
        return false;
    }
}
