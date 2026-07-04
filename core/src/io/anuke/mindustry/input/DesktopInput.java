package io.anuke.mindustry.input;

import arc.Core;
import arc.Input;
import arc.graphics.CapStyle;
import arc.input.Input.Buttons;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.struct.IntSet;
import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.mindustry.core.GameState.State;
import io.anuke.mindustry.entities.Player;
import io.anuke.mindustry.entities.Unit;
import io.anuke.mindustry.entities.Units;
import io.anuke.mindustry.entities.traits.TargetTrait;
import io.anuke.mindustry.entities.units.BaseUnit;
import io.anuke.mindustry.entities.units.GroundUnit;
import io.anuke.mindustry.entities.units.UnitOrderType;
import io.anuke.mindustry.entities.units.types.Drone;
import io.anuke.mindustry.game.Schematic;
import io.anuke.mindustry.gen.Call;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.input.PlaceUtils.NormalizeDrawResult;
import io.anuke.mindustry.input.PlaceUtils.NormalizeResult;
import io.anuke.mindustry.net.Net;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import arc.input.KeyBinds;
import arc.Settings;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;

import static io.anuke.mindustry.Vars.*;
import static io.anuke.mindustry.input.CursorType.*;
import static io.anuke.mindustry.input.PlaceMode.*;

public class DesktopInput extends InputHandler{
    private final String section;
    //controller info
    private float controlx, controly;
    private boolean controlling;
    /**Current cursor type.*/
    private CursorType cursorType = normal;

    /**Animation scale for line.*/
    private float selectScale;
    private final IntSet selectedUnits = new IntSet();
    private boolean selectingUnits;
    private final Vec2 unitSelectStart = new Vec2();
    private final Vec2 unitSelectEnd = new Vec2();
    private boolean leftWasDown, rightWasDown;
    private UnitOrderType activeOrderType = UnitOrderType.move;

    public DesktopInput(Player player){
        super(player);
        this.section = "player_" + (player.playerIndex + 1);
    }

    /**Draws a placement icon for a specific block.*/
    void drawPlace(int x, int y, Block block, int rotation){
        if(block == null) return;
        if(validPlace(x, y, block, rotation)){
            Draw.color();

            TextureRegion[] regions = block.getBlockIcon();

            for(TextureRegion region : regions){
                Draw.rect(region, x * tilesize + block.offset(), y * tilesize + block.offset(),
                        region.width * selectScale, region.height * selectScale, block.rotate ? rotation * 90 : 0);
            }
        }else{
            Draw.color(Palette.removeBack);
            Lines.square(x * tilesize + block.offset(), y * tilesize + block.offset() - 1, block.size * tilesize / 2f);
            Draw.color(Palette.remove);
            Lines.square(x * tilesize + block.offset(), y * tilesize + block.offset(), block.size * tilesize / 2f);
        }
    }

    @Override
    public boolean isDrawing(){
        return mode != none || recipe != null;
    }

    @Override
    public void drawOutlined(){
        int cursorX = tileX(Core.input.getX());
        int cursorY = tileY(Core.input.getY());

        //draw selection(s)
        if(mode == placing && recipe != null){
            NormalizeResult result = PlaceUtils.normalizeArea(selectX, selectY, cursorX, cursorY, rotation, true, maxLength);

            for(int i = 0; i <= result.getLength(); i += recipe.result.size){
                int x = selectX + i * Mathf.sign(cursorX - selectX) * Mathf.bool(result.isX());
                int y = selectY + i * Mathf.sign(cursorY - selectY) * Mathf.bool(!result.isX());

                if(i + recipe.result.size > result.getLength() && recipe.result.rotate){
                    Draw.color(!validPlace(x, y, recipe.result, result.rotation) ? Palette.remove : Palette.placeRotate);
                    Draw.grect("place-arrow", x * tilesize + recipe.result.offset(),
                            y * tilesize + recipe.result.offset(), result.rotation * 90 - 90);
                }

                drawPlace(x, y, recipe.result, result.rotation);
            }

            Draw.reset();
        }else if(mode == breaking){
            NormalizeDrawResult result = PlaceUtils.normalizeDrawArea(Blocks.air, selectX, selectY, cursorX, cursorY, false, maxLength, 1f);
            NormalizeResult dresult = PlaceUtils.normalizeArea(selectX, selectY, cursorX, cursorY, rotation, false, maxLength);

            for(int x = dresult.x; x <= dresult.x2; x++){
                for(int y = dresult.y; y <= dresult.y2; y++){
                    Tile tile = world.tile(x, y);
                    if(tile == null || !validBreak(tile.x, tile.y)) continue;
                    tile = tile.target();

                    Draw.color(Palette.removeBack);
                    Lines.square(tile.drawx(), tile.drawy(), tile.block().size * tilesize / 2f);
                    Draw.color(Palette.remove);
                    Lines.square(tile.drawx(), tile.drawy(), tile.block().size * tilesize / 2f);
                }
            }

            Draw.color(Palette.removeBack);
            Lines.rect(result.x, result.y, result.x2 - result.x, result.y2 - result.y);
            Draw.color(Palette.remove);
            Lines.rect(result.x, result.y, result.x2 - result.x, result.y2 - result.y);
        }else if(mode == copying){
            int minx = Math.min(selectX, cursorX);
            int miny = Math.min(selectY, cursorY);
            int maxx = Math.max(selectX, cursorX);
            int maxy = Math.max(selectY, cursorY);

            Draw.color(Palette.accent);
            Lines.stroke(2f);
            Lines.rect(minx * tilesize, miny * tilesize, (maxx - minx + 1) * tilesize, (maxy - miny + 1) * tilesize);
            Draw.reset();
        }else if(mode == PlaceMode.schematic && schematic != null){
            for(Schematic.Stile tile : schematic.tiles){
                int ox = cursorX + tile.x + (tile.block.size - 1) / 2;
                int oy = cursorY + tile.y + (tile.block.size - 1) / 2;
                drawPlace(ox, oy, tile.block, tile.rotation);
            }
        }else if(isPlacing()){
            if(recipe.result.rotate){
                Draw.color(!validPlace(cursorX, cursorY, recipe.result, rotation) ? Palette.remove : Palette.placeRotate);
                Draw.grect("place-arrow", cursorX * tilesize + recipe.result.offset(),
                        cursorY * tilesize + recipe.result.offset(), rotation * 90 - 90);
            }
            drawPlace(cursorX, cursorY, recipe.result, rotation);
            recipe.result.drawPlace(cursorX, cursorY, rotation, validPlace(cursorX, cursorY, recipe.result, rotation));
        }

        Draw.reset();
    }

    @Override
    public void update(){
        if(Net.active() && Inputs.keyTap("player_list")){
            ui.listfrag.toggle();
        }

        if(Inputs.keyTap(section, "map")){
            ui.mapfrag.toggle();
        }

        int cursorX = tileX(Core.input.getX());
        int cursorY = tileY(Core.input.getY());

        if(ui.chatfrag.chatOpen() || ui.mapfrag.isOpen()) return;

        if(Inputs.keyTap(section, "schematic_select")){
            ui.schematics.show();
        }

        if(Inputs.keyTap(section, "copy")){
            recipe = null;
            mode = copying;
            schematic = null;
            cursorX = tileX(Core.input.getX());
            cursorY = tileY(Core.input.getY());
            selectX = cursorX;
            selectY = cursorY;
        }

        if(mode == PlaceMode.schematic && schematic != null){
            if(Inputs.keyTap(section, "schematic_flip_x")){
                schematic.flipX();
            }
            if(Inputs.keyTap(section, "schematic_flip_y")){
                schematic.flipY();
            }
        }

        if(Inputs.keyRelease(section, "select")){
            player.isShooting = false;
        }

        if(state.is(State.menu) || ui.hasDialog()) return;

        boolean controller = KeyBinds.getSection(section).device.type == Inputs.DeviceType.controller;

        //zoom and rotate things
        if(Inputs.getAxisActive("zoom") && (Inputs.keyDown(section, "zoom_hold") || controller)){
            renderer.scaleCamera((int) Inputs.getAxisTapped(section, "zoom"));
        }

        renderer.minimap.zoomBy(-(int) Inputs.getAxisTapped(section, "zoom_minimap"));

        if(player.isDead()) return;

        pollInput();

        //deselect if not placing
        if(!isPlacing() && mode == placing){
            mode = none;
        }

        if(player.isShooting && !canShoot()){
            player.isShooting = false;
        }

        if(isPlacing()){
            cursorType = hand;
            selectScale = Mathf.lerpDelta(selectScale, 1f, 0.2f);
        }else{
            selectScale = 0f;
        }

        int axis = (int) Inputs.getAxisTapped(section, "rotate");
        if(axis != 0){
            if(mode == PlaceMode.schematic && schematic != null){
                if(axis > 0){
                    schematic.rotate();
                }else{
                    for(int i = 0; i < 3; i++) schematic.rotate();
                }
            }else{
                rotation = Mathf.mod(rotation + axis, 4);
            }
        }

        Tile cursor = tileAt(Core.input.getX(), Core.input.getY());

        if(player.isDead()){
            cursorType = normal;
        }else if(cursor != null){
            cursor = cursor.target();

            cursorType = cursor.block().getCursor(cursor);

            if(isPlacing()){
                cursorType = hand;
            }

            if(!isPlacing() && canMine(cursor)){
                cursorType = drill;
            }

            if(canTapPlayer(Gfx.mouseWorld().x, Gfx.mouseWorld().y)){
                cursorType = unload;
            }
        }

        if(!ui.hasMouse()){
            cursorType.set();
        }

        cursorType = normal;
    }

    void pollInput(){
        Tile selected = tileAt(Core.input.getX(), Core.input.getY());
        int cursorX = tileX(Core.input.getX());
        int cursorY = tileY(Core.input.getY());
        Vec2 mouseWorld = Gfx.mouseWorld();
        boolean leftDown = Core.input.isButtonPressed(Buttons.LEFT);
        boolean rightDown = Core.input.isButtonPressed(Buttons.RIGHT);
        boolean leftJustPressed = leftDown && !leftWasDown;
        boolean rightJustPressed = rightDown && !rightWasDown;

        if(Inputs.keyTap(section, "deselect") && !rightJustPressed){
            player.setMineTile(null);
            selectedUnits.clear();
        }

        boolean shift = Core.input.isKeyPressed(Keys.SHIFT_LEFT) || Core.input.isKeyPressed(Keys.SHIFT_RIGHT);
        boolean rtsModifier = Core.input.isKeyPressed(Keys.ALT_LEFT) || Core.input.isKeyPressed(Keys.ALT_RIGHT)
            || Core.input.isKeyPressed(Keys.CONTROL_LEFT) || Core.input.isKeyPressed(Keys.CONTROL_RIGHT);
        boolean unitCommandMode = hasSelectedUnits();
        if((rtsModifier || unitCommandMode) && !ui.hasMouse() && leftJustPressed){
            selectingUnits = true;
            unitSelectStart.set(mouseWorld);
            unitSelectEnd.set(mouseWorld);
        }

        if(selectingUnits){
            unitSelectEnd.set(mouseWorld);
            if(!leftDown){
                finalizeUnitSelection();
                selectingUnits = false;
            }
            leftWasDown = leftDown;
            rightWasDown = rightDown;
            return;
        }

        if(!ui.hasMouse() && rightJustPressed && unitCommandMode){
            Tile clickTile = world.tileWorld(mouseWorld.x, mouseWorld.y);
            if(clickTile != null) clickTile = clickTile.target();
            Unit enemyUnit = Units.getClosestEnemy(player.getTeam(), mouseWorld.x, mouseWorld.y, 18f, u -> !u.isDead());
            boolean enemyTile = clickTile != null && state.teams.areEnemies(player.getTeam(), clickTile.getTeam()) && clickTile.entity != null;

            if(enemyUnit != null || enemyTile){
                issueSelectedTargetOrders(mouseWorld.x, mouseWorld.y);
                leftWasDown = leftDown;
                rightWasDown = rightDown;
                return;
            }

            UnitOrderType issued = activeOrderType == UnitOrderType.none ? UnitOrderType.move : activeOrderType;
            if(shift){
                issued = UnitOrderType.attackMove;
            }
            issueSelectedOrders(mouseWorld.x, mouseWorld.y, issued);
            leftWasDown = leftDown;
            rightWasDown = rightDown;
            return;
        }

        if(unitCommandMode){
            // While units are selected, suppress build/break/mine click flows to avoid keybind conflicts.
            player.isShooting = false;
            leftWasDown = leftDown;
            rightWasDown = rightDown;
            return;
        }

        if(Inputs.keyTap(section, "select") && !ui.hasMouse()){
            if(mode == PlaceMode.schematic && schematic != null){
                schematics.place(schematic, cursorX, cursorY, player.getTeam());
                if(!Inputs.keyDown(Input.CONTROL_LEFT)){
                    mode = none;
                    schematic = null;
                }
            }else if(recipe != null){
                selectX = cursorX;
                selectY = cursorY;
                mode = placing;
            }else if(selected != null){
                //only begin shooting if there's no cursor event
                if (!tileTapped(selected) && !tryTapPlayer(Gfx.mouseWorld().x, Gfx.mouseWorld().y) && player.getPlaceQueue().size == 0 && !droppingItem &&
                        !tryBeginMine(selected) && player.getMineTile() == null) {
                    player.isShooting = true;
                }
            }else{ //if it's out of bounds, shooting is just fine
                player.isShooting = true;
            }
        }else if(Inputs.keyTap(section, "deselect") && (recipe != null || mode != none || player.isBuilding()) &&
        !(player.getCurrentRequest() != null && player.getCurrentRequest().breaking && KeyBinds.get(section, "deselect") == KeyBinds.get(section, "break"))){
            if(recipe == null){
                player.clearBuilding();
            }

            recipe = null;
            mode = none;
        }else if(Inputs.keyTap(section, "break") && !ui.hasMouse()){
            //is recalculated because setting the mode to breaking removes potential multiblock cursor offset
            mode = breaking;
            selectX = tileX(Core.input.getX());
            selectY = tileY(Core.input.getY());
        }


        if(Inputs.keyRelease(section, "break") || Inputs.keyRelease(section, "select") || (Inputs.keyRelease(section, "copy") && mode == copying)){

            if(mode == placing){ //touch up while placing, place everything in selection
                NormalizeResult result = PlaceUtils.normalizeArea(selectX, selectY, cursorX, cursorY, rotation, true, maxLength);

                for(int i = 0; i <= result.getLength(); i += recipe.result.size){
                    int x = selectX + i * Mathf.sign(cursorX - selectX) * Mathf.bool(result.isX());
                    int y = selectY + i * Mathf.sign(cursorY - selectY) * Mathf.bool(!result.isX());

                    rotation = result.rotation;

                    tryPlaceBlock(x, y);
                }
            }else if(mode == breaking){ //touch up while breaking, break everything in selection
                NormalizeResult result = PlaceUtils.normalizeArea(selectX, selectY, cursorX, cursorY, rotation, false, maxLength);
                for(int x = 0; x <= Math.abs(result.x2 - result.x); x++){
                    for(int y = 0; y <= Math.abs(result.y2 - result.y); y++){
                        int wx = selectX + x * Mathf.sign(cursorX - selectX);
                        int wy = selectY + y * Mathf.sign(cursorY - selectY);

                        tryBreakBlock(wx, wy);
                    }
                }
            }else if(mode == copying){
                schematic = schematics.create(selectX, selectY, cursorX, cursorY);
                recipe = null;
                mode = PlaceMode.schematic;
            }

            if(selected != null){
                tryDropItems(selected.target(), Gfx.mouseWorld().x, Gfx.mouseWorld().y);
            }

            if(mode != PlaceMode.schematic) mode = none;
        }

        leftWasDown = leftDown;
        rightWasDown = rightDown;
    }

    private void finalizeUnitSelection(){
        float minx = Math.min(unitSelectStart.x, unitSelectEnd.x);
        float miny = Math.min(unitSelectStart.y, unitSelectEnd.y);
        float maxx = Math.max(unitSelectStart.x, unitSelectEnd.x);
        float maxy = Math.max(unitSelectStart.y, unitSelectEnd.y);

        selectedUnits.clear();

        for(BaseUnit unit : unitGroups[player.getTeam().ordinal()].all()){
            if(unit.isDead()) continue;
            if(!unit.isPlayerControllable) continue;
            if(unit.x >= minx && unit.x <= maxx && unit.y >= miny && unit.y <= maxy){
                selectedUnits.add(unit.getID());
            }
        }
    }

    private void issueSelectedOrders(float x, float y, UnitOrderType type){
        if(selectedUnits.size == 0) return;

        IntSet.IntSetIterator it = selectedUnits.iterator();
        while(it.hasNext){
            int id = it.next();
            BaseUnit unit = unitGroups[player.getTeam().ordinal()].getByID(id);
            if(unit == null || unit.isDead()) continue;
            if(Net.active() && Net.client()){
                Call.issueUnitOrder(player, id, (byte)type.ordinal(), x, y);
            }else{
                InputHandler.issueUnitOrder(player, id, (byte)type.ordinal(), x, y);
            }
        }
    }

    private void issueSelectedTargetOrders(float x, float y){
        if(selectedUnits.size == 0) return;

        IntSet.IntSetIterator it = selectedUnits.iterator();
        while(it.hasNext){
            int id = it.next();
            BaseUnit unit = unitGroups[player.getTeam().ordinal()].getByID(id);
            if(unit == null || unit.isDead()) continue;
            if(Net.active() && Net.client()){
                Call.issueUnitAttackTarget(player, id, x, y);
            }else{
                InputHandler.issueUnitAttackTarget(player, id, x, y);
            }
        }
    }

    private boolean hasSelectedUnits(){
        return selectedUnits.size > 0;
    }

    public boolean isUnitCommandMode(){
        return hasSelectedUnits();
    }

    public UnitOrderType getActiveOrderType(){
        return activeOrderType;
    }

    public void setActiveOrderType(UnitOrderType activeOrderType){
        if(activeOrderType != null){
            this.activeOrderType = activeOrderType;
        }
    }

    public void clearUnitSelection(){
        selectedUnits.clear();
    }

    public void setSelectedDronesFollow(boolean follow){
        IntSet.IntSetIterator it = selectedUnits.iterator();
        while(it.hasNext){
            int id = it.next();
            BaseUnit unit = unitGroups[player.getTeam().ordinal()].getByID(id);
            if(!(unit instanceof Drone)) continue;
            if(Net.active() && Net.client()){
                Call.setDroneFollowMode(player, id, follow);
            }else{
                InputHandler.setDroneFollowMode(player, id, follow);
            }
        }
    }

    public boolean hasSelectedDrones(){
        IntSet.IntSetIterator it = selectedUnits.iterator();
        while(it.hasNext){
            BaseUnit unit = unitGroups[player.getTeam().ordinal()].getByID(it.next());
            if(unit instanceof Drone) return true;
        }
        return false;
    }

    public boolean selectedDronesFollowing(){
        IntSet.IntSetIterator it = selectedUnits.iterator();
        while(it.hasNext){
            BaseUnit unit = unitGroups[player.getTeam().ordinal()].getByID(it.next());
            if(unit instanceof Drone && ((Drone)unit).isFollowPlayerMode()) return true;
        }
        return false;
    }

    @Override
    public void drawTop(){
        if(selectingUnits){
            float minx = Math.min(unitSelectStart.x, unitSelectEnd.x);
            float miny = Math.min(unitSelectStart.y, unitSelectEnd.y);
            float maxx = Math.max(unitSelectStart.x, unitSelectEnd.x);
            float maxy = Math.max(unitSelectStart.y, unitSelectEnd.y);

            Draw.color(Palette.accent);
            Lines.stroke(1.5f);
            Lines.rect(minx, miny, maxx - minx, maxy - miny);
            Draw.color();
        }

        IntSet.IntSetIterator it = selectedUnits.iterator();
        while(it.hasNext){
            int id = it.next();
            BaseUnit unit = unitGroups[player.getTeam().ordinal()].getByID(id);
            if(unit == null || unit.isDead()) continue;
            Draw.color(Palette.accent);
            Lines.stroke(1.2f);
            Lines.circle(unit.x, unit.y, unit.getSize() * 0.75f + 2f);
        }

        it = selectedUnits.iterator();
        while(it.hasNext){
            int id = it.next();
            BaseUnit unit = unitGroups[player.getTeam().ordinal()].getByID(id);
            if(unit == null || unit.isDead()) continue;
            TargetTrait target = unit.getTarget();
            if(target != null && target.isValid() && target.getTeam() != unit.getTeam()){
                Draw.color(Palette.remove);
                Lines.stroke(1f);
                Lines.poly(target.getX(), target.getY(), 4, 7f, Timers.time() * 1.5f);
                Lines.spikes(target.getX(), target.getY(), 3f, 6f, 4, Timers.time() * 1.5f);
            }
        }
        Draw.color();
    }

    @Override
    public void drawUnderUnitsAndBlocks(){
        if(!Settings.getBool("massai-debug", false)) return;

        IntSet.IntSetIterator it = selectedUnits.iterator();
        while(it.hasNext){
            int id = it.next();
            BaseUnit unit = unitGroups[player.getTeam().ordinal()].getByID(id);
            if(unit == null || unit.isDead() || !unit.hasOrder()) continue;

            float ox = unit.getOrderX(), oy = unit.getOrderY();

            Draw.color(Palette.command);
            Lines.stroke(1.4f);
            Lines.line(bridgeRegion, unit.x, unit.y, ox, oy, CapStyle.None.ordinal(), -tilesize / 2f);
            Lines.circle(ox, oy, 4f);

            if(unit instanceof GroundUnit){
                GroundUnit g = (GroundUnit)unit;
                int cursor = g.getOrderPathCursor();
                int size = g.getOrderPathSize();

                float lastx = unit.x, lasty = unit.y;
                Draw.color(Palette.placeRotate);
                Lines.stroke(1.8f);
                for(int i = cursor; i < size; i++){
                    Tile t = world.tile(g.getOrderPathTilePacked(i));
                    if(t == null) continue;
                    Lines.line(bridgeRegion, lastx, lasty, t.worldx(), t.worldy(), CapStyle.None.ordinal(), -tilesize / 2f);
                    lastx = t.worldx();
                    lasty = t.worldy();
                }

                if(size > cursor){
                    Lines.line(bridgeRegion, lastx, lasty, ox, oy, CapStyle.None.ordinal(), -tilesize / 2f);
                }
            }
        }
        Draw.color();
    }

    @Override
    public boolean selectedBlock(){
        return isPlacing() && mode != breaking;
    }

    @Override
    public float getMouseX(){
        return !controlling ? Core.input.getX() : controlx;
    }

    @Override
    public float getMouseY(){
        return !controlling ? Core.input.getY() : controly;
    }

    @Override
    public boolean isCursorVisible(){
        return controlling;
    }

    @Override
    public void updateController(){
        //TODO no controller support
        //TODO move controller input to new class, ControllerInput
        boolean mousemove = Core.input.getDeltaX() > 1 || Core.input.getDeltaY() > 1;

        if(state.is(State.menu)){
            droppingItem = false;
        }

        if(KeyBinds.getSection(section).device.type == Inputs.DeviceType.controller && (!mousemove || player.playerIndex > 0)){
            if(player.playerIndex > 0){
                controlling = true;
            }

            float xa = Inputs.getAxis(section, "cursor_x");
            float ya = Inputs.getAxis(section, "cursor_y");

            if(Math.abs(xa) > controllerMin || Math.abs(ya) > controllerMin){
                float scl = Settings.getInt("sensitivity", 100) / 100f;
                controlx += xa * baseControllerSpeed * scl;
                controly -= ya * baseControllerSpeed * scl;
                controlling = true;

                if(player.playerIndex == 0){
                    Core.input.setCursorCatched(true);
                }

                Inputs.getProcessor().touchDragged((int) getMouseX(), (int) getMouseY(), player.playerIndex);
            }

            controlx = Mathf.clamp(controlx, 0, Core.Gfx.getWidth());
            controly = Mathf.clamp(controly, 0, Core.Gfx.getHeight());
        }else{
            controlling = false;
            Core.input.setCursorCatched(false);
        }

        if(!controlling){
            controlx = Core.input.getX();
            controly = Core.input.getY();
        }
    }

}

