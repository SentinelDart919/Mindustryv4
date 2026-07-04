package io.anuke.mindustry.editor;

import arc.graphics.Gfx;
import arc.input.KeyCode;
import arc.graphics.Color;
import arc.graphics.g2d.Batch;
import arc.input.GestureDetector;
import arc.input.GestureDetector.GestureListener;
import arc.math.geom.Bresenham2;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Inputs;
import io.anuke.mindustry.editor.DrawOperation.TileOperation;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.ui.GridImage;
import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Scl;
import arc.math.geom.Geometry;
import arc.math.Mathf;

import static io.anuke.mindustry.Vars.mobile;
import static io.anuke.mindustry.Vars.ui;

public class MapView extends Element implements GestureListener{
    private MapEditor editor;
    private EditorTool tool = EditorTool.pencil;
    private OperationStack stack = new OperationStack();
    private DrawOperation op;
    private Bresenham2 br = new Bresenham2();
    private boolean updated = false;
    private float offsetx, offsety;
    private float zoom = 1f;
    private boolean grid = false;
    private GridImage image = new GridImage(0, 0);
    private Vec2 vec = new Vec2();
    private Rect rect = new Rect();
    private Vec2[][] brushPolygons = new Vec2[MapEditor.brushSizes.length][0];

    private boolean drawing;
    private int lastx, lasty;
    private int startx, starty;
    private float mousex, mousey;
    private EditorTool lastTool;

    public MapView(MapEditor editor){
        this.editor = editor;

        for(int i = 0; i < MapEditor.brushSizes.length; i++){
            float size = MapEditor.brushSizes[i];
            brushPolygons[i] = Geometry.pixelCircle(size, (index, x, y) -> new Vec2(x, y).dst2(index, index) <= index - 0.5f);
        }

        Inputs.addProcessor(new GestureDetector(20, 0.5f, 2, 0.15f, this));
        touchable = Touchable.enabled;

        addListener(new InputListener(){

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y){
                mousex = x;
                mousey = y;

                return false;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                if(pointer != 0){
                    return false;
                }

                if(!mobile && button != KeyCode.mouseLeft && button != KeyCode.mouseMiddle){
                    return true;
                }

                if(button == KeyCode.mouseMiddle){
                    lastTool = tool;
                    tool = EditorTool.zoom;
                }

                mousex = x;
                mousey = y;

                op = new DrawOperation(editor.getMap());

                updated = false;

                Point2 p = project(x, y);
                lastx = p.x;
                lasty = p.y;
                startx = p.x;
                starty = p.y;
                tool.touched(editor, p.x, p.y);

                if(tool.edit){
                    updated = true;
                    ui.editor.resetSaved();
                }

                drawing = true;
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                if(!mobile && button != KeyCode.mouseLeft && button != KeyCode.mouseMiddle){
                    return;
                }

                drawing = false;

                Point2 p = project(x, y);

                if(tool == EditorTool.line){
                    ui.editor.resetSaved();
                    DrawOperation lineOp = new DrawOperation(editor.getMap());
                    Seq<Point2> points = br.line(startx, starty, p.x, p.y);
                    for(Point2 point : points){
                        editor.draw(point.x, point.y);
                    }
                    updated = true;
                }

                if(op != null && updated){
                    if(!op.isEmpty()){
                        stack.add(op);
                    }
                    op = null;
                }

                if(lastTool != null){
                    tool = lastTool;
                    lastTool = null;
                }

            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer){
                mousex = x;
                mousey = y;

                Point2 p = project(x, y);

                if(drawing && tool.draggable){
                    ui.editor.resetSaved();
                    Seq<Point2> points = br.line(lastx, lasty, p.x, p.y);
                    for(Point2 point : points){
                        tool.touched(editor, point.x, point.y);
                    }
                    updated = true;
                }
                lastx = p.x;
                lasty = p.y;
            }
        });
    }

    public EditorTool getTool(){
        return tool;
    }

    public void setTool(EditorTool tool){
        this.tool = tool;
    }

    public void clearStack(){
        stack.clear();
    }

    public OperationStack getStack(){
        return stack;
    }

    public boolean isGrid(){
        return grid;
    }

    public void setGrid(boolean grid){
        this.grid = grid;
    }

    public void undo(){
        if(stack.canUndo()){
            stack.undo(editor);
        }
    }

    public void redo(){
        if(stack.canRedo()){
            stack.redo(editor);
        }
    }

    public void addTileOp(TileOperation t){
        op.addOperation(t);
    }

    public boolean checkForDuplicates(short x, short y){
        return op.checkDuplicate(x, y);
    }

    @Override
    public void act(float delta){
        super.act(delta);

        if(Core.scene.getKeyboardFocus() == null || !(Core.scene.getKeyboardFocus() instanceof TextField) &&
                !Inputs.keyDown(KeyCode.controlLeft)){
            float ax = Inputs.getAxis("move_x");
            float ay = Inputs.getAxis("move_y");
            offsetx -= ax * 15f / zoom;
            offsety -= ay * 15f / zoom;
        }

        if(ui.editor.hasPane()) return;

        zoom += Inputs.scroll() / 10f * zoom;
        clampZoom();
    }

    private void clampZoom(){
        zoom = Mathf.clamp(zoom, 0.2f, 12f);
    }

    private Point2 project(float x, float y){
        float ratio = 1f / ((float) editor.getMap().width() / editor.getMap().height());
        float size = Math.min(width, height);
        float sclwidth = size * zoom;
        float sclheight = size * zoom * ratio;
        x = (x - getWidth() / 2 + sclwidth / 2 - offsetx * zoom) / sclwidth * editor.getMap().width();
        y = (y - getHeight() / 2 + sclheight / 2 - offsety * zoom) / sclheight * editor.getMap().height();

        if(editor.getDrawBlock().size % 2 == 0 && tool != EditorTool.eraser){
            return new Point2((int) (x - 0.5f), (int) (y - 0.5f));
        }else{
            return new Point2((int) x, (int) y);
        }
    }

    private Vec2 unproject(int x, int y){
        float ratio = 1f / ((float) editor.getMap().width() / editor.getMap().height());
        float size = Math.min(width, height);
        float sclwidth = size * zoom;
        float sclheight = size * zoom * ratio;
        float px = ((float) x / editor.getMap().width()) * sclwidth + offsetx * zoom - sclwidth / 2 + getWidth() / 2;
        float py = ((float) (y) / editor.getMap().height()) * sclheight
                + offsety * zoom - sclheight / 2 + getHeight() / 2;
        return vec.set(px, py);
    }

    public void draw(Batch batch, float alpha){
        float ratio = 1f / ((float) editor.getMap().width() / editor.getMap().height());
        float size = Math.min(width, height);
        float sclwidth = size * zoom;
        float sclheight = size * zoom * ratio;
        float centerx = x + width / 2 + offsetx * zoom;
        float centery = y + height / 2 + offsety * zoom;

        image.setImageSize(editor.getMap().width(), editor.getMap().height());

        Gfx.beginClip(x, y, width, height);

        Draw.color(Palette.remove);
        Lines.stroke(2f);
        Lines.rect(centerx - sclwidth / 2 - 1, centery - sclheight / 2 - 1, sclwidth + 2, sclheight + 2);
        editor.renderer().draw(centerx - sclwidth / 2, centery - sclheight / 2, sclwidth, sclheight);
        Draw.reset();

        if(grid){
            Draw.color(Color.gray);
            image.setBounds(centerx - sclwidth / 2, centery - sclheight / 2, sclwidth, sclheight);
            image.draw(batch, alpha);
            Draw.color();
        }

        int index = 0;
        for(int i = 0; i < MapEditor.brushSizes.length; i++){
            if(editor.getBrushSize() == MapEditor.brushSizes[i]){
                index = i;
                break;
            }
        }

        float scaling = zoom * Math.min(width, height) / editor.getMap().width();

        Draw.color(Palette.accent);
        Lines.stroke(Scl.scl(1f * zoom));

        if(!editor.getDrawBlock().isMultiblock() || tool == EditorTool.eraser){
            if(tool == EditorTool.line && drawing){
                Vec2 v1 = unproject(startx, starty).add(x, y);
                float sx = v1.x, sy = v1.y;
                Vec2 v2 = unproject(lastx, lasty).add(x, y);

                Lines.poly(brushPolygons[index], sx, sy, scaling);
                Lines.poly(brushPolygons[index], v2.x, v2.y, scaling);
            }

            if(tool.edit && (!mobile || drawing)){
                Point2 p = project(mousex, mousey);
                Vec2 v = unproject(p.x, p.y).add(x, y);
                Lines.poly(brushPolygons[index], v.x, v.y, scaling);
            }
        }else{
            if((tool.edit || tool == EditorTool.line) && (!mobile || drawing)){
                Point2 p = project(mousex, mousey);
                Vec2 v = unproject(p.x, p.y).add(x, y);
                float offset = (editor.getDrawBlock().size % 2 == 0 ? scaling / 2f : 0f);
                Lines.square(
                        v.x + scaling / 2f + offset,
                        v.y + scaling / 2f + offset,
                        scaling * editor.getDrawBlock().size / 2f);
            }
        }

        Gfx.endClip();

        Draw.color(Palette.accent);
        Lines.stroke(Scl.scl(3f));
        Lines.rect(x, y, width, height);
        Draw.reset();
    }

    private boolean active(){
        return Core.scene.getKeyboardFocus() != null
                && Core.scene.getKeyboardFocus().isDescendantOf(ui.editor)
                && ui.editor.isShown() && tool == EditorTool.zoom &&
                Core.scene.hit(Gfx.mouseWorld().x, Gfx.mouseWorld().y, true) == this;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, KeyCode button){
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, KeyCode button){
        return false;
    }

    @Override
    public boolean longPress(float x, float y){
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, KeyCode button){
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY){
        if(!active()) return false;
        offsetx += deltaX / zoom;
        offsety -= deltaY / zoom;
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, KeyCode button){
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance){
        if(!active()) return false;
        float nzoom = distance - initialDistance;
        zoom += nzoom / 10000f / Scl.scl(1f) * zoom;
        clampZoom();
        return false;
    }

    @Override
    public boolean pinch(Vec2 initialPointer1, Vec2 initialPointer2, Vec2 pointer1, Vec2 pointer2){
        return false;
    }

    @Override
    public void pinchStop(){

    }
}

