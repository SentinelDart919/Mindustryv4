package io.anuke.mindustry.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.mindustry.content.fx.BlockFx;
import io.anuke.mindustry.content.fx.Fx;
import io.anuke.mindustry.core.GameState.State;
import io.anuke.mindustry.entities.Player;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.Unit;
import io.anuke.mindustry.entities.bullet.Bullet;
import io.anuke.mindustry.entities.bullet.BulletType;
import io.anuke.mindustry.entities.effect.GroundEffectEntity;
import io.anuke.mindustry.entities.effect.GroundEffectEntity.GroundEffect;
import io.anuke.mindustry.entities.traits.BelowLiquidTrait;
import io.anuke.mindustry.entities.units.BaseUnit;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.graphics.*;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.production.*;
import io.anuke.ucore.util.Tmp;
import io.anuke.mindustry.world.blocks.defense.ForceProjector.ShieldEntity;
import io.anuke.ucore.core.*;
import io.anuke.ucore.entities.EntityDraw;
import io.anuke.ucore.entities.EntityGroup;
import io.anuke.ucore.entities.impl.EffectEntity;
import io.anuke.ucore.entities.trait.DrawTrait;
import io.anuke.ucore.entities.trait.Entity;
import io.anuke.ucore.function.Consumer;
import io.anuke.ucore.function.Predicate;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.graphics.Lines;
import io.anuke.ucore.graphics.Surface;
import io.anuke.ucore.modules.RendererModule;
import io.anuke.ucore.scene.utils.Cursors;
import io.anuke.ucore.util.Bundles;
import io.anuke.ucore.util.Mathf;
import io.anuke.ucore.util.Pooling;
import io.anuke.ucore.util.Translator;

import static io.anuke.mindustry.Vars.*;
import static io.anuke.ucore.core.Core.batch;
import static io.anuke.ucore.core.Core.camera;

public class Renderer extends RendererModule{
    public final Surface effectSurface;
    public final Surface lightSurface;
    public final BlockRenderer blocks = new BlockRenderer();
    public final MinimapRenderer minimap = new MinimapRenderer();
    public final OverlayRenderer overlays = new OverlayRenderer();
    public final FogRenderer fog = new FogRenderer();
    public final WeatherRenderer weather = new WeatherRenderer();

    private Bloom bloom;
    private boolean lastBloom;

    private int targetscale = baseCameraScale;
    private Rectangle rect = new Rectangle(), rect2 = new Rectangle();
    private Vector2 avgPosition = new Translator();
    private Color ambient = new Color();

    private boolean currentFlying;
    private Team currentTeam;
    private final Predicate<BaseUnit> unitFlyingFilter = u -> (u.isFlying() || u.highAltitude) == currentFlying && !u.isDead();
    private final Predicate<BaseUnit> unitFlyingTeamFilter = u -> (u.isFlying() || u.highAltitude) == currentFlying && u.getTeam() == currentTeam;
    private final Predicate<Player> playerFlyingFilter = p -> (p.isFlying() || p.highAltitude) == currentFlying && p.getTeam() == currentTeam;

    /** How far (in tiles) beyond the screen lights are still drawn, so big lights don't pop in/out at the edges.
     * Kept slightly above the largest light radius in the game (the fusion shockwave), while lights further away
     * are culled per-light against the visible area below. */ // hehe optimizations hehe hehe i hate java
    private static final int lightMargin = 48;
    /** Light radius of a player, used to cull player lights. */
    private static final float playerLightRadius = 140f;
    /** Default light radius of units, used to cull unit lights. */
    private static final float unitLightRadius = 60f;
    /** Visible area in world units, used to cull lights that can't be seen. */
    private final Rectangle lightRect = new Rectangle();
    /** Last applied render scale setting, used to detect changes and rebuild the surfaces. */
    private int lastRenderScale;

    public Renderer(){
        Core.batch = new SpriteBatch(4096);

        Lines.setCircleVertices(14);

        Shaders.init();

        Core.cameraScale = baseCameraScale;
        Effects.setEffectProvider((effect, color, x, y, rotation, data) -> {
            if(effect == Fx.none) return;
            if(Settings.getBool("effects")){
                Rectangle view = rect.setSize(camera.viewportWidth, camera.viewportHeight)
                        .setCenter(camera.position.x, camera.position.y);
                Rectangle pos = rect2.setSize(effect.size).setCenter(x, y);

                if(view.overlaps(pos)){

                    if(!(effect instanceof GroundEffect)){
                        EffectEntity entity = Pooling.obtain(EffectEntity.class, EffectEntity::new);
                        entity.effect = effect;
                        entity.color = color;
                        entity.rotation = rotation;
                        entity.data = data;
                        if(data instanceof BlockFx.SmokeData){
                            BlockFx.SmokeData smoke = (BlockFx.SmokeData) data;
                            entity.lifetime = smoke.pathLength() / smoke.speed + smoke.fade;
                        }
                        entity.id++;
                        entity.set(x, y);
                        if(data instanceof Entity){
                            entity.setParent((Entity) data);
                        }
                        threads.runGraphics(() -> effectGroup.add(entity));
                    }else{
                        GroundEffectEntity entity = Pooling.obtain(GroundEffectEntity.class, GroundEffectEntity::new);
                        entity.effect = effect;
                        entity.color = color;
                        entity.rotation = rotation;
                        entity.id++;
                        entity.data = data;
                        entity.set(x, y);
                        if(data instanceof Entity){
                            entity.setParent((Entity) data);
                        }
                        threads.runGraphics(() -> groundEffectGroup.add(entity));
                    }
                }
            }
        });

        Cursors.cursorScaling = 3;
        Cursors.outlineColor = Color.valueOf("444444");

        Cursors.arrow = Cursors.loadCursor("cursor");
        Cursors.hand = Cursors.loadCursor("hand");
        Cursors.ibeam = Cursors.loadCursor("ibar");
        Cursors.restoreCursor();
        Cursors.loadCustom("drill");
        Cursors.loadCustom("unload");

        clearColor = new Color(0f, 0f, 0f, 1f);

        Settings.defaults("renderer", 100);
        Settings.defaults("fogofwar", true);
        lastRenderScale = Settings.getInt("renderer", 100);

        effectSurface = Graphics.createSurface(renderScale());
        pixelSurface = Graphics.createSurface(renderScale());
        lightSurface = Graphics.createSurface(renderScale());

        Settings.defaults("bloom", true);
        Settings.defaults("bloomintensity", 14);
        Settings.defaults("bloomblur", 2);

        Settings.defaults("showweather", true);

        lastBloom = Settings.getBool("bloom");

        rebuildPost();
    }

    @Override
    public void init(){
    }

    @Override
    public void update(){
        //TODO hack, find source of this bug
        Color.WHITE.set(1f, 1f, 1f, 1f);

        checkPostSettings();
        checkRendererSettings();

        if(Core.cameraScale != targetscale){
            float targetzoom = (float) Core.cameraScale / targetscale;
            camera.zoom = Mathf.lerpDelta(camera.zoom, targetzoom, 0.2f);

            if(Mathf.in(camera.zoom, targetzoom, 0.005f)){
                camera.zoom = 1f;
                Graphics.setCameraScale(targetscale);
                for(Player player : players){
                    control.input(player.playerIndex).resetCursor();
                }
            }
        }else{
            camera.zoom = Mathf.lerpDelta(camera.zoom, 1f, 0.2f);
        }

        if(state.is(State.menu)){
            Graphics.clear(Color.BLACK);
        }else{
            Vector2 position = averagePosition();

            if(players[0].isDead()){
                TileEntity core = players[0].getClosestCore();
                if(core != null && players[0].spawner == -1){
                    smoothCamera(core.x, core.y, 0.08f);
                }else{
                    smoothCamera(position.x + 0.0001f, position.y + 0.0001f, 0.08f);
                }
            }else if(!mobile){
                setCamera(position.x + 0.0001f, position.y + 0.0001f);
            }
            if(!world.isOpenWorld()){
                camera.position.x = Mathf.clamp(camera.position.x, -tilesize / 2f, world.width() * tilesize - tilesize / 2f);
                camera.position.y = Mathf.clamp(camera.position.y, -tilesize / 2f, world.height() * tilesize - tilesize / 2f);
            }

            float prex = camera.position.x, prey = camera.position.y;
            updateShake(0.75f);

            float deltax = camera.position.x - prex, deltay = camera.position.y - prey;
            float lastx = camera.position.x, lasty = camera.position.y;

            if(snapCamera){
                camera.position.set((int) camera.position.x, (int) camera.position.y, 0);
            }

            if(Gdx.graphics.getHeight() / Core.cameraScale % 2 == 1){
                camera.position.add(0, -0.5f, 0);
            }

            if(Gdx.graphics.getWidth() / Core.cameraScale % 2 == 1){
                camera.position.add(-0.5f, 0, 0);
            }

            draw();

            camera.position.set(lastx - deltax, lasty - deltay, 0);
        }

        if(!ui.chatfrag.chatOpen()){
            renderer.record(); //this only does something if GdxGifRecorder is on the class path, which it usually isn't
        }
    }

    @Override
    public void draw(){
        camera.update();
        if(Float.isNaN(Core.camera.position.x) || Float.isNaN(Core.camera.position.y)){
            Core.camera.position.x = players[0].x;
            Core.camera.position.y = players[0].y;
        }

        if(bloom != null){
            bloom.setBloomIntensity(Settings.getInt("bloomintensity") / 10f);
            bloom.blurPasses = Settings.getInt("bloomblur");
        }

        Graphics.clear(clearColor);

        batch.setProjectionMatrix(camera.combined);

        Graphics.surface(pixelSurface, false);

        Graphics.clear(clearColor);

        blocks.drawFloor();

        weather.drawUnder();

        drawAndInterpolate(groundEffectGroup, e -> e instanceof BelowLiquidTrait);
        drawAndInterpolate(puddleGroup);
        drawAndInterpolate(groundEffectGroup, e -> !(e instanceof BelowLiquidTrait));
        control.input(0).drawUnderUnitsAndBlocks();

        blocks.processBlocks();
        blocks.drawShadows();
        for(Team team : Team.all){
            if(blocks.isTeamShown(team)){
                boolean outline = team != players[0].getTeam() && team != Team.none;

                if(outline){
                    Shaders.outline.color.set(team.color);
                    Shaders.outline.color.a = 0.8f;
                    Graphics.beginShaders(Shaders.outline);
                }

                blocks.drawTeamBlocks(Layer.block, team);

                if(outline){
                    Graphics.endShaders();
                }
            }
        }
        blocks.skipLayer(Layer.block);

        Graphics.shader(Shaders.blockbuild, false);
        blocks.drawBlocks(Layer.placement);
        Graphics.shader();

        blocks.drawBlocks(Layer.overlay);

        drawAllTeams(false);

        blocks.skipLayer(Layer.turret);
        blocks.drawBlocks(Layer.tree);
        blocks.drawBlocks(Layer.laser);

        drawFlyerShadows();

        drawAllTeams(true);

        drawAndInterpolate(bulletGroup);
        drawAndInterpolate(effectGroup);

        overlays.drawBottom();
        drawAndInterpolate(playerGroup, p -> true, Player::drawBuildRequests);

        Graphics.beginShaders(Shaders.shield);
        shieldGroup.forEach(s -> {
            Shaders.shield.teamColor.set(s.getTeam().color);
            Shaders.shield.apply();
            s.draw();
            batch.flush();
        });
        shieldGroup.forEach(s -> {
            Shaders.shield.teamColor.set(s.getTeam().color);
            Shaders.shield.apply();
            s.drawOver();
            batch.flush();
        });
        Graphics.endShaders();
        Draw.color();

        overlays.drawTop();

        boolean postActive = bloom != null && Settings.getBool("bloom");

        if((weather.isDayNight() ? weather.cycleDarkness() : state.darkness) > 0.01f){
            drawLights();
            drawLightmap();
            Graphics.surface();
            batch.end();
            if(postActive){
                drawPost();
            }else{
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                batch.setProjectionMatrix(camera.combined);
                batch.begin();
                blitPixelSurface();
                batch.end();
            }
        }else if(showFog){
            Graphics.surface();
            batch.end();
        }else if(postActive){
            Graphics.surface();
            batch.end();
            drawPost();
        }else{
            Graphics.flushSurface();
            batch.end();
        }

        if(showFog){
            fog.draw();
        }

        Graphics.beginCam();
        EntityDraw.setClip(false);
        weather.drawOver();
        drawAndInterpolate(playerGroup, p -> !p.isDead() && !p.isLocal, Player::drawName);
        EntityDraw.setClip(true);
        Graphics.end();
        Draw.color();
    }

    public void drawLights(){
        //fill the lightmap with the ambient light level. light sources get added on top
        float darkness = weather.isDayNight() ? weather.cycleDarkness() : state.darkness;
        if(weather.isDayNight()){
            darkness += Mathf.random(-1f, 1f) * 0.002f;
        }
        float light = Math.max(0f, Math.min(1f, 1f - darkness));
        ambient.set(light, light, light, 1f);

        Graphics.surface(lightSurface, false, true);
        Graphics.clear(ambient);
        batch.setProjectionMatrix(camera.combined);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        //draw lights over a wider area than the visible blocks
        int avgx = Mathf.scl(camera.position.x, tilesize);
        int avgy = Mathf.scl(camera.position.y, tilesize);
        int rangex = (int)(camera.viewportWidth * camera.zoom / tilesize / 2) + 2;
        int rangey = (int)(camera.viewportHeight * camera.zoom / tilesize / 2) + 2;

        int minx, miny, maxx, maxy;
        if(world.isOpenWorld()){
            minx = avgx - rangex - lightMargin;
            miny = avgy - rangey - lightMargin;
            maxx = avgx + rangex + lightMargin;
            maxy = avgy + rangey + lightMargin;
        }else{
            minx = Math.max(avgx - rangex - lightMargin, 0);
            miny = Math.max(avgy - rangey - lightMargin, 0);
            maxx = Math.min(world.width() - 1, avgx + rangex + lightMargin);
            maxy = Math.min(world.height() - 1, avgy + rangey + lightMargin);
        }

        lightRect.set(camera.position.x - camera.viewportWidth * camera.zoom / 2f,
                camera.position.y - camera.viewportHeight * camera.zoom / 2f,
                camera.viewportWidth * camera.zoom, camera.viewportHeight * camera.zoom);

        Shaders.light.type = 0;
        Graphics.shader(Shaders.light);

        //Blocks
        for(int x = minx; x <= maxx; x++){
            for(int y = miny; y <= maxy; y++){
                Tile tile = world.rawTile(x, y);
                if(tile != null && tile.block() != Blocks.air){
                    Block block = tile.block();
                    float radius = Math.max(block.lightRadius() * tilesize, block.layerLightRadius * tilesize) + tilesize * 2f;
                    if(lightVisible(tile.drawx(), tile.drawy(), radius)){
                        block.drawLight(tile);
                        block.drawLayerLight(tile);
                    }
                }
            }
        }

        //Bullets
        for(Entity entity : bulletGroup.all()){
            if(entity instanceof Bullet){
                Bullet bullet = (Bullet) entity;
                BulletType type = bullet.getBulletType();
                if(lightVisible(bullet.x, bullet.y, Math.max(type.lightRadius, bullet.lightRadius))){
                    type.drawLight(bullet);
                }
            }
        }
        //Units
        for(EntityGroup<? extends BaseUnit> group : unitGroups){
            for(BaseUnit unit : group.all()){
                if(!unit.isDead() && lightVisible(unit.x, unit.y, Math.max(unit.lightRadius, unitLightRadius))){
                    unit.drawLight();
                }
            }
        }

        //Players
        for(Player player : playerGroup.all()){
            if(!player.isDead() && lightVisible(player.x, player.y, playerLightRadius)){
                player.drawLight();
            }
        }

        //Effects
        for(Entity entity : effectGroup.all()){
            if(entity instanceof EffectEntity){
                drawEffectLight((EffectEntity) entity);
            }
        }
        for(DrawTrait entity : groundEffectGroup.all()){
            if(entity instanceof EffectEntity){
                drawEffectLight((EffectEntity) entity);
            }
        }

        Graphics.shader();
        Draw.color();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Graphics.surface();
    }

    /** Whether a light at (x, y) with the given radius (in world units) overlaps the visible area. */
    private boolean lightVisible(float x, float y, float radius){
        return x + radius >= lightRect.x && x - radius <= lightRect.x + lightRect.width &&
                y + radius >= lightRect.y && y - radius <= lightRect.y + lightRect.height;
    }

    private void drawEffectLight(EffectEntity entity){
        Effects.Effect effect = entity.effect;
        if(effect == null) return;

        boolean emit = entity.emitLight != null ? entity.emitLight : effect.emitLight;
        float radius = entity.lightRadius < 0 ? effect.lightRadius : entity.lightRadius;
        float opacity = entity.lightOpacity < 0 ? effect.lightOpacity : entity.lightOpacity;
        Color color = entity.lightColor != null ? entity.lightColor : effect.lightColor;

        if(!emit) return;

        float fin = entity.fin();
        float fade;
        if(entity instanceof GroundEffectEntity && ((GroundEffect) effect).isStatic){
            fade = 1f;
        }else{
            fade = Mathf.clamp(fin < 0.2f ? fin / 0.2f : (1f - fin) / 0.8f, 0f, 1f);
        }

        radius *= fade;
        opacity *= fade;

        if(radius > 0.001f && opacity > 0.001f){
            if(!lightVisible(entity.x, entity.y, radius)) return;
            Draw.color(color);
            Shaders.light.region = Draw.region("circle");
            Draw.alpha(opacity);
            Draw.rect("circle", entity.x, entity.y, radius * 2, radius * 2);
            Draw.alpha(opacity * 0.5f);
            Draw.rect("circle", entity.x, entity.y, radius * 2, radius * 2);
        }
    }

    // multiplies the scene (pixelSurface) by the lightmap (lightSurface): scene * (ambient + light)
    private void drawLightmap(){
        batch.flush();
        batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO);
        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, pixelSurface.width(), pixelSurface.height()));
        batch.draw(lightSurface.texture(), 0, 0, pixelSurface.width(), pixelSurface.height(), 0, 0, 1, 1);
        batch.flush();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setProjectionMatrix(camera.combined);
    }

    private void blitPixelSurface(){
        batch.draw(pixelSurface.texture(),
                camera.position.x - camera.viewportWidth / 2 * camera.zoom,
                camera.position.y + camera.viewportHeight / 2 * camera.zoom,
                camera.viewportWidth * camera.zoom, -camera.viewportHeight * camera.zoom);
    }

    //applies the bloom chain (threshold -> blur -> photographic combine) to the composed
    //scene and draws the result to the screen. The batch must not be drawing when this is called (crash)
    private void drawPost(){
        if(bloom == null || !bloom.isReady()){
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            blitPixelSurface();
            batch.end();
            return;
        }

        bloom.render(pixelSurface.texture());

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void drawFlyerShadows(){
        Graphics.surface(effectSurface, true, false);

        float trnsX = -12, trnsY = -13;

        for(EntityGroup<? extends BaseUnit> group : unitGroups){
            if(!group.isEmpty()){
                drawAndInterpolate(group, unit -> unit.isFlying() && !unit.isDead(), baseUnit -> baseUnit.drawShadow(trnsX, trnsY));
            }
        }

        if(!playerGroup.isEmpty()){
            drawAndInterpolate(playerGroup, unit -> unit.isFlying() && !unit.isDead(), player -> player.drawShadow(trnsX, trnsY));
        }

        Draw.color(0, 0, 0, 0.15f);
        Graphics.flushSurface();
        Draw.color();
    }

    private void drawAllTeams(boolean flying){
        currentFlying = flying;

        for(Team team : Team.all){
            EntityGroup<BaseUnit> group = unitGroups[team.ordinal()];

            if(group.isEmpty() && playerGroup.count(p -> (p.isFlying() || p.highAltitude) == flying && p.getTeam() == team) == 0) continue;

            if(flying && group.count(p -> p.isFlying() || p.highAltitude) == 0 &&
                    playerGroup.count(p -> (p.isFlying() || p.highAltitude) && p.getTeam() == team) == 0) continue;

            currentTeam = team;

            drawAndInterpolate(unitGroups[team.ordinal()], unitFlyingFilter, Unit::drawUnder);
            drawAndInterpolate(playerGroup, playerFlyingFilter, Unit::drawUnder);

            Shaders.outline.color.set(team.color);
            Shaders.mix.color.set(Color.WHITE);

            Graphics.beginShaders(Shaders.outline);
            Graphics.shader(Shaders.mix, true);
            drawAndInterpolate(unitGroups[team.ordinal()], unitFlyingFilter, Unit::drawAll);
            drawAndInterpolate(playerGroup, playerFlyingFilter, Unit::drawAll);
            Graphics.shader();
            blocks.drawTeamBlocks(Layer.turret, team);
            Graphics.endShaders();

            drawAndInterpolate(unitGroups[team.ordinal()], unitFlyingFilter, Unit::drawOver);
            drawAndInterpolate(playerGroup, playerFlyingFilter, Unit::drawOver);
        }
    }

    public <T extends DrawTrait> void drawAndInterpolate(EntityGroup<T> group){
        drawAndInterpolate(group, t -> true, DrawTrait::draw);
    }

    public <T extends DrawTrait> void drawAndInterpolate(EntityGroup<T> group, Predicate<T> toDraw){
        drawAndInterpolate(group, toDraw, DrawTrait::draw);
    }

    public <T extends DrawTrait> void drawAndInterpolate(EntityGroup<T> group, Predicate<T> toDraw, Consumer<T> drawer){
        EntityDraw.drawWith(group, toDraw, drawer);
    }

    @Override
    public void resize(int width, int height){
        float lastX = camera.position.x, lastY = camera.position.y;
        super.resize(width, height);
        for(Player player : players){
            control.input(player.playerIndex).resetCursor();
        }
        camera.update();
        camera.position.set(lastX, lastY, 0f);

        effectSurface.onResize();
        pixelSurface.onResize();
        lightSurface.onResize();

        rebuildPost();
    }

    @Override
    public void dispose(){
        fog.dispose();
        effectSurface.dispose();
        pixelSurface.dispose();
        lightSurface.dispose();
        if(bloom != null) bloom.dispose();
    }

    private void checkPostSettings(){
        boolean bloomOn = Settings.getBool("bloom");
        if(bloomOn != lastBloom){
            lastBloom = bloomOn;
            rebuildPost();
        }
    }

    /** Rebuilds all surfaces to match the current camera scale and render scale setting. */
    private void applyScale(){
        for(Surface surface : Graphics.getSurfaces()){
            surface.setScale(renderScale());
        }
    }

    /** The scale factor used by the render surfaces. Larger = smaller surfaces.
     *  A render scale below 100% renders at a lower resolution and upscales to the screen, cutting GPU fill rate. */
    private int renderScale(){
        return Math.max(1, Math.round(targetscale * 100f / Math.max(Settings.getInt("renderer", 100), 1)));
    }

    /** Detects render scale changes and rebuilds the surfaces when it changes. */
    private void checkRendererSettings(){
        showFog = Settings.getBool("fogofwar");

        int rs = Settings.getInt("renderer", 100);
        if(rs != lastRenderScale){
            lastRenderScale = rs;
            applyScale();
        }
    }

    /** Rebuilds the bloom effect to match the current screen size and settings. Safe to call between frames. */
    public void rebuildPost(){
        if(bloom != null){
            bloom.dispose();
            bloom = null;
        }

        if(!Settings.getBool("bloom")) return;

        int width = Math.max(Gdx.graphics.getWidth(), 1);
        int height = Math.max(Gdx.graphics.getHeight(), 1);

        bloom = new Bloom(width, height);
        bloom.setThreshold(0.5f);
        bloom.setOriginalIntensity(1f);
        bloom.setBloomIntensity(Settings.getInt("bloomintensity") / 10f);
        bloom.blurPasses = Settings.getInt("bloomblur");
    }

    public Vector2 averagePosition(){
        avgPosition.setZero();
        int count = 0;

        for(Player player : players){
            if(player.isLocal){
                avgPosition.add(player.x, player.y);
                count++;
            }
        }

        if(count > 0){
            avgPosition.scl(1f / count);
        }
        return avgPosition;
    }

    public void setCameraScale(int amount){
        targetscale = amount;
        clampScale();
        applyScale();
    }

    public void scaleCamera(int amount){
        setCameraScale(targetscale + amount);
    }

    public void clampScale(){
        float s = io.anuke.ucore.scene.ui.layout.Unit.dp.scl(1f);
        int amp = Math.max(Settings.getInt("zoom", 100), 100);
        targetscale = Mathf.clamp(targetscale, Math.max(1, Math.round(s * 2 * 100f / amp)), Math.round(s * 5));
    }

    public void takeMapScreenshot(){
        float vpW = Core.camera.viewportWidth, vpH = Core.camera.viewportHeight;
        int w = world.width()*tilesize, h =  world.height()*tilesize;
        int pw = pixelSurface.width(), ph = pixelSurface.height();
        showFog = false;
        disableUI = true;
        pixelSurface.setSize(w, h, true);
        Graphics.getEffectSurface().setSize(w, h, true);
        Core.camera.viewportWidth = w;
        Core.camera.viewportHeight = h;
        Core.camera.position.x = w/2f;
        Core.camera.position.y = h/2f;

        draw();

        showFog = true;
        disableUI = false;
        Core.camera.viewportWidth = vpW;
        Core.camera.viewportHeight = vpH;

        pixelSurface.getBuffer().begin();
        byte[] lines = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
        for(int i = 0; i < lines.length; i+= 4){
            lines[i + 3] = (byte)255;
        }
        pixelSurface.getBuffer().end();

        Pixmap fullPixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        BufferUtils.copy(lines, 0, fullPixmap.getPixels(), lines.length);
        FileHandle file = screenshotDirectory.child("screenshot-" + TimeUtils.millis() + ".png");
        PixmapIO.writePNG(file, fullPixmap);
        fullPixmap.dispose();

        pixelSurface.setSize(pw, ph, false);
        Graphics.getEffectSurface().setSize(pw, ph, false);

        ui.showInfoFade(Bundles.format("text.screenshot", file.toString()));
    }

}
