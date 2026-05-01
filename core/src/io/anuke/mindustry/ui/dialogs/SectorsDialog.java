package io.anuke.mindustry.ui.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Align;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.maps.Sector;
import io.anuke.mindustry.maps.campaign.CampaignManager;
import io.anuke.mindustry.maps.campaign.CampaignRegistry;
import io.anuke.mindustry.maps.campaign.CampaignRegistry.PlanetDefinition;
import io.anuke.ucore.util.Log;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.graphics.Fill;
import io.anuke.ucore.scene.Element;
import io.anuke.ucore.scene.Group;
import io.anuke.ucore.scene.event.InputEvent;
import io.anuke.ucore.scene.event.InputListener;
import io.anuke.ucore.scene.event.Touchable;
import io.anuke.ucore.scene.ui.layout.Cell;
import io.anuke.ucore.scene.ui.layout.Table;
import io.anuke.ucore.scene.ui.layout.Unit;
import io.anuke.ucore.scene.utils.Cursors;
import io.anuke.ucore.util.Bundles;
import io.anuke.ucore.util.Mathf;
import io.anuke.ucore.core.Core;

import static io.anuke.mindustry.Vars.world;

public class SectorsDialog extends FloatingDialog{
    private static final float sectorSize = Unit.dp.scl(32 * 5);
    private Sector selected;
    private Table sectorTable;
    private Table campaignTable;
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

        campaignTable = new Table("button");
        campaignTable.top().left().margin(6f);
        campaignTable.update(() -> campaignTable.setPosition(10f, height - 10f, Align.topLeft));

        Group container = new Group();
        container.setTouchable(Touchable.childrenOnly);
        container.addChild(sectorTable);
        container.addChild(campaignTable);

        margin(0);
        getTitleTable().clear();
        clear();
        stack(content(), container, buttons()).grow();

        shown(this::setup);
    }

    void setup(){
        selected = null;
        selectedPlanet = CampaignRegistry.planetForCampaign(world.sectors.getActiveCampaign());

        sectorTable.clear();
        campaignTable.clear();
        content().clear();
        buttons().clear();
        buttons().bottom().margin(15);

        addCloseButton();
        setupCampaigns();
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
                }
            }).width(buttonWidth);
            campaignTable.row();
        }

        campaignTable.pack();
    }

    void selectSector(Sector sector){
        selected = sector;

        sectorTable.clear();
        sectorTable.background("button").margin(5);
        sectorTable.defaults().pad(3);
        sectorTable.add(Bundles.format("text.sector", sector.x + ", " + sector.y));
        sectorTable.row();

        if(selected.completedMissions < selected.missions.size && !selected.complete){
            sectorTable.labelWrap(Bundles.format("text.mission", selected.getDominantMission().menuDisplayString())).growX();
            sectorTable.row();
        }

        if(selected.hasSave()){
            sectorTable.labelWrap(Bundles.format("text.sector.time", selected.getSave().getPlayTime())).growX();
            sectorTable.row();
        }

        sectorTable.table(t -> {
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
                ).width(sectorSize / Unit.dp.scl(1f)).height(60f);
                cell.width(sectorSize / Unit.dp.scl(1f));
            }else{
                cell.width(sectorSize * 2f / Unit.dp.scl(1f));
            }
        }).pad(-5).growX().padTop(0);

        sectorTable.pack();
        sectorTable.act(Gdx.graphics.getDeltaTime());
    }

    public Sector getSelected(){
        return selected;
    }

    class SectorView extends Element{
        float lastX, lastY;
        boolean clicked = false;
        float rotLon = 0f, rotLat = 0f;
        float zoom = 1f; // camera distance factor: higher = farther
        float pendingScroll = 0f;
        final PlanetMeshRenderer mesh = new PlanetMeshRenderer();
        boolean meshFailed;

        void resetCamera(){
            rotLon = 0f;
            rotLat = 0f;
            zoom = 1f;
        }

        void rebuildPlanetModels(){
            PlanetDefinition planet = selectedPlanet == null ? CampaignRegistry.planetForCampaign(world.sectors.getActiveCampaign()) : selectedPlanet;
            mesh.rebuildPlanetModels(planet);
        }

        SectorView(){
            addListener(new InputListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                    if(pointer != 0) return false;
                    lastX = x;
                    lastY = y;
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer){
                    if(pointer != 0) return;
                    rotLon += (x - lastX) * 0.01f;
                    rotLat = Mathf.clamp(rotLat - (y - lastY) * 0.01f, -1.2f, 1.2f);
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
                    if(pointer != 0) return;
                    Cursors.restoreCursor();
                }
            });

            clicked(() -> clicked = true);
        }

        @Override
        public void draw(){
            if(pendingScroll != 0f){
                // wheel down -> farther, wheel up -> closer
                zoom = Mathf.clamp(zoom + pendingScroll, 0.45f, 2.3f);
                pendingScroll = 0f;
            }

            Draw.color(0.02f, 0.03f, 0.06f, 1f);
            Fill.crect(x, y, width, height);
            Draw.reset();

            PlanetDefinition planet = selectedPlanet == null ? CampaignRegistry.planetForCampaign(world.sectors.getActiveCampaign()) : selectedPlanet;
            PlanetMeshRenderer.HoverData hovered;
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

            if(hovered.sector != null && clicked){
                selectedDrawX = hovered.x;
                selectedDrawY = hovered.y;
                selectSector(hovered.sector);
            }
            clicked = false;
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
    }
}
