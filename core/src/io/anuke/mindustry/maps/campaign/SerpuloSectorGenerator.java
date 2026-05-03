package io.anuke.mindustry.maps.campaign;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.maps.Sector;
import io.anuke.mindustry.maps.SectorPresets;
import io.anuke.mindustry.maps.SectorPresets.SectorPreset;
import io.anuke.mindustry.maps.generation.Generation;
import io.anuke.mindustry.maps.generation.WorldGenerator.GenResult;
import io.anuke.mindustry.maps.missions.BattleMission;
import io.anuke.mindustry.maps.missions.BiomassInfectableMission;
import io.anuke.mindustry.maps.missions.BiomassInfectedBattleMission;
import io.anuke.mindustry.maps.missions.BiomassInfectedMission;
import io.anuke.mindustry.maps.missions.Mission;
import io.anuke.mindustry.maps.missions.Missions;
import io.anuke.mindustry.maps.missions.WaveMission;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.type.ItemStack;
import io.anuke.mindustry.type.Recipe;
import io.anuke.mindustry.type.Recipe.RecipeVisibility;
import io.anuke.mindustry.world.blocks.Floor;
import io.anuke.mindustry.world.blocks.defense.Wall;
import io.anuke.ucore.util.Mathf;

import static io.anuke.mindustry.Vars.content;
import static io.anuke.mindustry.Vars.sectorSize;
import static io.anuke.mindustry.Vars.world;

public class SerpuloSectorGenerator implements CampaignSectorGenerator{
    private final SectorPresets presets = new SectorPresets();

    @Override
    public Array<Item> getOres(int x, int y, Array<Item> defaultOres){
        Array<Item> ores = presets.getOres(x, y);
        return ores == null ? defaultOres : ores;
    }

    @Override
    public void initSector(Sector sector){
        sector.missions.clear();
        sector.difficulty = (int)Mathf.dst(sector.x, sector.y);

        if(presets.get(sector.x, sector.y) != null){
            SectorPreset p = presets.get(sector.x, sector.y);
            sector.missions.addAll(p.missions);
            sector.x = (short)p.x;
            sector.y = (short)p.y;
        }else{
            generate(sector);
        }

        sector.spawns = new Array<>();
        for(Mission mission : sector.missions){
            sector.spawns.addAll(mission.getWaves(sector));
        }

        if(sector.difficulty > 12){
            sector.startingItems = Array.with(new ItemStack(Items.copper, 1900), new ItemStack(Items.scrap, 1000), new ItemStack(Items.lead, 500), new ItemStack(Items.densealloy, 470), new ItemStack(Items.silicon, 460), new ItemStack(Items.titanium, 230));
        }else if(sector.difficulty > 8){
            sector.startingItems = Array.with(new ItemStack(Items.copper, 1500), new ItemStack(Items.scrap, 900), new ItemStack(Items.lead, 400), new ItemStack(Items.densealloy, 340), new ItemStack(Items.silicon, 250));
        }else if(sector.difficulty > 5){
            sector.startingItems = Array.with(new ItemStack(Items.copper, 950), new ItemStack(Items.scrap, 800), new ItemStack(Items.lead, 300), new ItemStack(Items.densealloy, 190), new ItemStack(Items.silicon, 140));
        }else if(sector.difficulty > 3){
            sector.startingItems = Array.with(new ItemStack(Items.copper, 700), new ItemStack(Items.scrap, 700), new ItemStack(Items.lead, 200), new ItemStack(Items.densealloy, 130));
        }else if(sector.difficulty > 2){
            sector.startingItems = Array.with(new ItemStack(Items.copper, 400), new ItemStack(Items.scrap, 600), new ItemStack(Items.lead, 100));
        }else{
            sector.startingItems = Array.with();
        }
    }

    private void generate(Sector sector){
        float rand = Mathf.randomSeed(sector.getSeed() + 7);

        if(rand < 0.10){
            sector.missions.add(new BiomassInfectedMission());
        }else if(rand < 0.20){
            sector.missions.add(new BiomassInfectedBattleMission());
        }else if(rand < 0.35f){
            sector.missions.add(new BiomassInfectableMission(sector.difficulty * 5 + Mathf.randomSeed(sector.getSeed(), 1, 4) * 5));
        }else if(rand < 0.60f){
            addRecipeMission(sector, 3);
            sector.missions.add(new WaveMission(sector.difficulty * 5 + Mathf.randomSeed(sector.getSeed(), 1, 4) * 5));
        }else{
            sector.missions.add(new BattleMission());
        }

        addRecipeMission(sector, 11);

        Generation gen = new Generation(sector, null, sectorSize, sectorSize, null);
        Array<GridPoint2> points = new Array<>();
        for(Mission mission : sector.missions){
            points.addAll(mission.getSpawnPoints(gen));
        }

        GenResult result = new GenResult();
        for(GridPoint2 point : new Array.ArrayIterable<>(points)){
            world.generator.generateTile(result, sector.x, sector.y, point.x, point.y, true, null, null);
            if(((Floor)result.floor).isLiquid || result.wall.solid){
                sector.missions.clear();
                sector.missions.add(new WaveMission(Math.max(1, sector.difficulty) * 5 + Mathf.randomSeed(sector.getSeed(), 1, 4) * 5));
                break;
            }
        }

        if(sector.missions.size == 0){
            sector.missions.add(new WaveMission(Math.max(1, sector.difficulty) * 5 + Mathf.randomSeed(sector.getSeed(), 1, 4) * 5));
        }
    }

    private void addRecipeMission(Sector sector, int offset){
        if(Mathf.randomSeed(sector.getSeed() + offset) < 0.5){
            Array<Recipe> recipes = new Array<>();
            for(Recipe r : content.recipes()){
                if(r.result instanceof Wall || (r.visibility != RecipeVisibility.all) || r.cost < 10f) continue;
                recipes.add(r);
            }
            float maxdiff = 8f;
            recipes.sort((r1, r2) -> Float.compare(r1.cost, r2.cost));
            int end = (int)(Mathf.clamp(sector.difficulty / maxdiff + 0.25f) * (recipes.size - 1));
            int start = (int)(Mathf.clamp(sector.difficulty / maxdiff) * (recipes.size / 2f));

            if(recipes.size > 0 && end > start){
                Recipe recipe = recipes.get(Mathf.randomSeed(sector.getSeed() + 10, start, end));
                sector.missions.addAll(Missions.blockRecipe(recipe.result));
            }
        }
    }
}
