package io.anuke.mindustry.ui.dialogs;

import arc.Core;
import arc.graphics.Color;
import arc.math.geom.Vec2;
import arc.util.Align;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.maps.Sector;
import io.anuke.mindustry.maps.campaign.CampaignManager;
import io.anuke.mindustry.maps.campaign.CampaignRegistry;
import io.anuke.mindustry.maps.campaign.CampaignRegistry.PlanetDefinition;
import arc.util.Log;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.Scl;
import arc.scene.utils.Cursors;
import arc.util.Strings;
import arc.math.Mathf;
import arc.Core;
import arc.Input;
import arc.Settings;

import static io.anuke.mindustry.Vars.world;

public class SectorsDialog extends FloatingDialog{
    private static final float sectorSize = Scl.scl(32 * 5);
    private Sector selected;
    private Table sectorTable;
    private Table campaignTable;
    private Table exportedTable;
    private SectorView view;
    private final CampaignManager campaignManager = new CampaignManager();
    private PlanetDefinition selectedPlanet;
    private float selectedDrawX, selectedDrawY;

    public SectorsDialog(){
        super("");

        sectorTable = new Table(){
            @Override
            public float getPrefWidth(){
                return sectorSize * 2f;
            }
        };
        sectorTable.visible(() -> selected != null);
        sectorTable.update(() -> {
            if(selected != null){
                sectorTable.setPosition(selectedDrawX, selectedDrawY - sectorSize / 2f + 1, Align.top);
            }
        });

        campaignTable = new Table();
        campaignTable.top().left().margin(6f);
        campaignTable.update(() -> campaignTable.setPosition(10f, height - 10f, Align.topLeft));

        exportedTable = new Table();
        exportedTable.top().right().margin(6f);
        exportedTable.update(() -> exportedTable.setPosition(width - 10f, height - 10f, Align.topRight));

        Group container = new Group();
        container.setTouchable(Touchable.childrenOnly);
        container.setFillParent(true);
        container.addChild(sectorTable);
        container.addChild(campaignTable);
        container.addChild(exportedTable);

        margin(0);
        getTitleTable().clear();
        clear();
        stack(content(), container, buttons()).grow();

        shown(this::setup);
        hidden(() -> {
            if(Core.scene.getScrollFocus() == view){
                Core.scene.setScrollFocus(null);
            }
            teardown3dScene();
        });
    }

    void setup(){
        teardown3dScene();
        selected = null;
        selectedPlanet = CampaignRegistry.planetForCampaign(world.sectors.getActiveCampaign());

        sectorTable.clear();
        campaignTable.clear();
        exportedTable.clear();
        content().clear();
        buttons().clear();
        buttons().bottom().margin(15);

        addCloseButton();
        setupCampaigns();
        Vars.launchManager.load();
        setupExported();
        world.sectors.refreshActiveCampaignPreviews();
        content().add(view = new SectorView()).grow();
        view.rebuildPlanetModels();
        Core.scene.setScrollFocus(view);
    }

    void setupCampaigns(){
        campaignManager.loadCampaigns();

        campaignTable.defaults().height(52f).pad(2f);
        campaignTable.add("$text.campaigns").left().pad(4f);
        campaignTable.row();

        for(PlanetDefinition planet : CampaignRegistry.planets()){
            float buttonWidth = Math.max(180f, planet.name.length() * 15f + 36f);
            campaignTable.addButton(planet.name, () -> {
                if(!planet.campaign.equals(world.sectors.getActiveCampaign())){
                    world.sectors.setActiveCampaign(planet.campaign);
                    selectedPlanet = planet;
                    selected = null;
                    sectorTable.clear();
                    view.resetCamera();
                    view.rebuildPlanetModels();
                    Vars.launchManager.load();
                    setupExported();
                }
            }).width(buttonWidth);
            campaignTable.row();
        }

        campaignTable.pack();
    }

    void setupExported(){
        exportedTable.clear();
        exportedTable.defaults().pad(2f);
        exportedTable.add("$text.exported").left().pad(4f);
        exportedTable.row();

        Table items = new Table();
        items.left();
        int i = 0;
        for(io.anuke.mindustry.type.Item item : Vars.launchManager.getInventory().keys()){
            int amount = Vars.launchManager.getAmount(item);
            if(amount > 0){
                items.addImage(item.region).size(8 * 3).padRight(4);
                items.add(amount + " / " + Vars.launchManager.getCapacity()).left().padRight(10);
                if(++i % 2 == 0) items.row();
            }
        }

        if(i == 0){
            items.add("$text.none").color(Color.gray);
        }

        exportedTable.add(items).left();
        exportedTable.row();

        exportedTable.pack();
    }

    void selectSector(Sector sector){
        selected = sector;

        sectorTable.clear();
        sectorTable.background("button").margin(5);
        sectorTable.defaults().pad(3);
        sectorTable.add(Bundles.format("text.sector", sector.x + ", " + sector.y));
        sectorTable.row();

        if(selected.missions.size > 0 && selected.completedMissions < selected.missions.size && !selected.complete){
            sectorTable.labelWrap(Bundles.format("text.mission", selected.getDominantMission().menuDisplayString())).growX();
            sectorTable.row();
        }

        if(selected.hasSave()){
            sectorTable.labelWrap(Bundles.format("text.sector.time", selected.getSave().getPlayTime())).growX();
            sectorTable.row();
        }

        sectorTable.table(t -> {
            boolean canDeploy = (selected.missions.size > 0 || selected.complete) && (view.isUnlocked(selected) || selected.hasSave());

            if(canDeploy){
                Cell<?> cell = t.addImageTextButton(selected.hasSave() ? "$text.sector.resume" : "$text.sector.deploy", "icon-play", 10 * 3, () -> {
                    hide();
                    Vars.ui.loadLogic(() -> world.sectors.playSector(selected));
                }).height(60f);

                if(selected.hasSave()){
                    t.addImageTextButton("$text.sector.abandon", "icon-cancel", 16 * 2, () ->
                        Vars.ui.showConfirm("$text.confirm", "$text.sector.abandon.confirm", () -> {
                            world.sectors.abandonSector(selected);
                            selectSector(selected);
                        })
                    ).width(sectorSize / Scl.scl(1f)).height(60f);
                    cell.width(sectorSize / Scl.scl(1f));
                }else{
                    cell.width(sectorSize * 2f / Scl.scl(1f));
                }
            }else{
                t.add("$text.sector.locked").color(Color.gray).pad(10);
            }
        }).pad(-5).growX().padTop(0);

        sectorTable.pack();
        sectorTable.act(Core.graphics.getDeltaTime());
    }

    public Sector getSelected(){
        return selected;
    }

    void teardown3dScene(){
        if(view != null){
            view.disposeScene();
            view.remove();
            view = null;
        }
        content().clear();
        sectorTable.clear();
        campaignTable.clear();
    }

    class SectorView extends Element{
        float lastX, lastY;
        float rotLon = 0f, rotLat = 0f;
        float panX = 0f, panY = 0f;
        float zoom = 1f;
        float pendingScroll = 0f;
        float downX, downY;
        boolean dragged;
        boolean pendingClick;
        Sector hoveredSector;
        float hoveredX, hoveredY;
        final PlanetMeshRenderer mesh = new PlanetMeshRenderer();
        boolean meshFailed;

        void resetCamera(){
            rotLon = 0f;
            rotLat = 0f;
            panX = 0f;
            panY = 0f;
            zoom = 1f;
        }

        void rebuildPlanetModels(){
            PlanetDefinition planet = selectedPlanet == null ? CampaignRegistry.planetForCampaign(world.sectors.getActiveCampaign()) : selectedPlanet;
            mesh.rebuildPlanetModels(planet);
        }

        SectorView(){
            addListener(new InputListener(){
                float lastZoomDistance = -1f;

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                    if(pointer > 1) return false;
                    lastX = x;
                    lastY = y;
                    downX = x;
                    downY = y;
                    dragged = false;
                    pendingClick = false;

                    if(pointer == 1){
                        lastZoomDistance = Vec2.dst(Core.input.getX(0), Core.input.getY(0), Core.input.getX(1), Core.input.getY(1));
                    }

                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer){
                    if(pointer > 1) return;

                    if(pointer == 1){
                        float newDistance = Vec2.dst(Core.input.getX(0), Core.input.getY(0), Core.input.getX(1), Core.input.getY(1));
                        if(lastZoomDistance > 0){
                            float amount = (newDistance - lastZoomDistance) * 0.01f;
                            if(Settings.getBool("planet3d")){
                                pendingScroll -= amount;
                            }else{
                                pendingScroll += amount;
                            }
                        }
                        lastZoomDistance = newDistance;
                        dragged = true;
                        return;
                    }

                    if(Math.abs(x - downX) > 5f || Math.abs(y - downY) > 5f){
                        dragged = true;
                    }

                    if(Settings.getBool("planet3d")){
                        float factor = 0.01f * zoom;
                        rotLon += (x - lastX) * factor * (float)Math.cos(rotLat);
                        rotLat = Mathf.clamp(rotLat - (y - lastY) * factor, -1.4f, 1.4f);
                    }else{
                        panX += (x - lastX);
                        panY += (y - lastY);
                    }

                    lastX = x;
                    lastY = y;
                }

                @Override
                public boolean scrolled(InputEvent event, float x, float y, int amount){
                    pendingScroll += amount * 0.08f;
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button){
                    if(pointer > 1) return;
                    if(pointer == 0){
                        pendingClick = !dragged;
                    }
                    if(pointer == 1){
                        lastZoomDistance = -1f;
                    }
                    Cursors.restoreCursor();
                }
            });
        }

        @Override
        public void draw(){
            float wheel = Inputs.scroll();
            if(wheel != 0f){
                pendingScroll += wheel * 0.10f;
            }

            if(pendingScroll != 0f){
                if(Settings.getBool("planet3d")){
                    zoom = Mathf.clamp(zoom + pendingScroll, 0.45f, 2.3f);
                }else{
                    float lastZoom = zoom;
                    zoom = Mathf.clamp(zoom - pendingScroll, 0.2f, 10f);
                    
                    float mx = Core.input.getX() - getX();
                    float my = (Core.Gfx.getHeight() - Core.input.getY()) - getY();
                }
                pendingScroll = 0f;
            }

            Draw.color(0.02f, 0.03f, 0.06f, 1f);
            Fill.crect(x, y, width, height);
            Draw.reset();

            PlanetDefinition planet = selectedPlanet == null ? CampaignRegistry.planetForCampaign(world.sectors.getActiveCampaign()) : selectedPlanet;
            PlanetMeshRenderer.HoverData hovered;
            if(Settings.getBool("planet3d")){
                try{
                    hovered = mesh.render(x, y, width, height, planet, rotLon, rotLat, zoom, selected);
                    meshFailed = false;
                }catch(Throwable t){
                    if(!meshFailed){
                        Log.err(t);
                    }
                    meshFailed = true;
                    hovered = new PlanetMeshRenderer.HoverData();
                }
                if(meshFailed){
                    drawSerpuloFallback(planet);
                }
            }else{
                hovered = drawClassicSectorMap(planet);
            }

            if(pendingClick && hovered.sector != null){
                selectedDrawX = hovered.x;
                selectedDrawY = hovered.y;
                selectSector(hovered.sector);
            }
            pendingClick = false;
            hoveredSector = hovered.sector;
            hoveredX = hovered.x;
            hoveredY = hovered.y;
        }

        void drawSerpuloFallback(PlanetDefinition planet){
            if(planet == null) return;
            float cx = x + width / 2f;
            float cy = y + height / 2f;
            float radius = Math.min(width, height) * 0.23f * zoom;

            Draw.color(planet.colorR * 0.45f, planet.colorG * 0.45f, planet.colorB * 0.45f, 0.9f);
            Draw.rect("icon-mission-background", cx + radius * 0.08f, cy - radius * 0.08f, radius * 2.08f, radius * 2.08f);
            Draw.color(planet.colorR, planet.colorG, planet.colorB, 1f);
            Draw.rect("icon-mission-background", cx, cy, radius * 2f, radius * 2f);
            Draw.color(Palette.accent);
            Draw.alpha(0.22f);
            Draw.rect("sector-select", cx, cy, radius * 2.02f, radius * 2.02f);
            Draw.reset();
        }

        PlanetMeshRenderer.HoverData drawClassicSectorMap(PlanetDefinition planet){
            PlanetMeshRenderer.HoverData out = new PlanetMeshRenderer.HoverData();
            if(planet == null) return out;

            float mapW = width * 0.85f * zoom;
            float mapH = height * 0.85f * zoom;
            float left = x + width / 2f - mapW / 2f + panX;
            float bottom = y + height / 2f - mapH / 2f + panY;

            Draw.color(0, 0, 0, 0.4f);
            Fill.crect(left, bottom, mapW, mapH);

            Draw.color(planet.colorR * 0.2f, planet.colorG * 0.2f, planet.colorB * 0.2f, 1f);
            for(int i = 0; i <= planet.gridLongitude; i++){
                Fill.crect(left + i * mapW / planet.gridLongitude - 1f, bottom, 2f, mapH);
            }
            for(int i = 0; i <= planet.gridLatitude; i++){
                Fill.crect(left, bottom + i * mapH / planet.gridLatitude - 1f, mapW, 2f);
            }

            Lines.stroke(2f);
            for(int sy = -planet.gridLatitude / 2; sy < planet.gridLatitude / 2; sy++){
                for(int sx = -planet.gridLongitude / 2; sx < planet.gridLongitude / 2; sx++){
                    Sector sector = world.sectors.get(sx, sy);
                    if(sector == null || !sector.complete) continue;

                    float tx = left + ((sx + planet.gridLongitude / 2f + 0.5f) / planet.gridLongitude) * mapW;
                    float ty = bottom + ((sy + planet.gridLatitude / 2f + 0.5f) / planet.gridLatitude) * mapH;

                    for(arc.math.geom.Point2 g : arc.math.geom.Geometry.d4){
                        Sector other = world.sectors.get(sx + g.x, sy + g.y);
                        if(other == null || !isUnlocked(other)) continue;
                        
                        float ox = left + ((sx + g.x + planet.gridLongitude / 2f + 0.5f) / planet.gridLongitude) * mapW;
                        float oy = bottom + ((sy + g.y + planet.gridLatitude / 2f + 0.5f) / planet.gridLatitude) * mapH;
                        
                        if(other.complete){
                            Draw.color(Color.gray);
                            Draw.alpha(0.2f);
                        }else{
                            Draw.color(Palette.accent);
                            Draw.alpha(0.5f);
                        }
                        Lines.line(tx, ty, ox, oy);
                    }
                }
            }
            Lines.stroke(1f);
            Draw.alpha(1f);

            float mx = Core.input.getX();
            float my = Core.Gfx.getHeight() - Core.input.getY();
            float best = Float.MAX_VALUE;

            for(int sy = -planet.gridLatitude / 2; sy < planet.gridLatitude / 2; sy++){
                for(int sx = -planet.gridLongitude / 2; sx < planet.gridLongitude / 2; sx++){
                    Sector sector = world.sectors.get(sx, sy);
                    if(sector == null) continue;

                    float tx = left + ((sx + planet.gridLongitude / 2f + 0.5f) / planet.gridLongitude) * mapW;
                    float ty = bottom + ((sy + planet.gridLatitude / 2f + 0.5f) / planet.gridLatitude) * mapH;
                    float sw = mapW / planet.gridLongitude;
                    float sh = mapH / planet.gridLatitude;

                    boolean unlocked = isUnlocked(sector);

                    if(sector.complete){
                        Draw.color(Palette.accent);
                    }else if(sector.hasSave()){
                        Draw.color(Color.white);
                    }else if(unlocked){
                        Draw.color(Color.white);
                    }else{
                        Draw.color(Color.gray);
                    }

                    if(unlocked){
                        if(sector.texture != null){
                            Draw.color(Color.white);
                            Draw.rect(sector.texture, tx, ty, sw, sh);
                        }else if(sector.complete){
                            Fill.poly(tx, ty, 4, Math.min(sw, sh) * 0.45f, 45f);
                        }

                        if(!sector.complete && sector.missions.size > 0){
                            float isize = Math.min(sw, sh) * 0.6f;
                            Draw.color(0f, 0f, 0f, 0.4f);
                            Fill.circle(tx, ty, isize / 2f + 2f);
                            
                            Color iconColor = Color.white;
                            Draw.color(iconColor);
                            Draw.rect(sector.getDominantMission().getIcon(), tx, ty, isize - 1, isize - 1);
                        }
                    }else{
                        Draw.color(Color.gray);
                        Draw.alpha(0.3f);
                        Fill.crect(tx - sw / 2f, ty - sh / 2f, sw, sh);
                        Draw.alpha(1f);
                    }

                    if(sector == selected){
                        Draw.color(Palette.accent);
                        Draw.rect("sector-select", tx, ty, sw * 1.5f, sh * 1.5f);
                    }

                    float dst = Vec2.dst(mx, my, tx, ty);
                    if(unlocked && dst < Math.max(sw, sh) && dst < best){
                        best = dst;
                        out.sector = sector;
                        out.x = tx;
                        out.y = ty;
                    }
                }
            }

            if(out.sector != null && out.sector != selected){
                float sw = mapW / planet.gridLongitude;
                float sh = mapH / planet.gridLatitude;
                Draw.color(Color.white);
                Draw.rect("sector-select", out.x, out.y, sw * 1.2f, sh * 1.2f);
            }

            Draw.reset();
            return out;
        }

        void disposeScene(){
            mesh.dispose();
        }

        public boolean isUnlocked(Sector sector){
            if(sector.complete || (sector.x == 0 && sector.y == 0)) return true;
            for(arc.math.geom.Point2 g : arc.math.geom.Geometry.d4){
                Sector other = world.sectors.get(sector.x + g.x, sector.y + g.y);
                if(other != null && other.complete) return true;
            }
            return false;
        }
    }

    public static boolean isUnlockedStatic(Sector sector){
        if(sector.complete || (sector.x == 0 && sector.y == 0)) return true;
        for(arc.math.geom.Point2 g : arc.math.geom.Geometry.d4){
            Sector other = world.sectors.get(sector.x + g.x, sector.y + g.y);
            if(other != null && other.complete) return true;
        }
        return false;
    }
}


