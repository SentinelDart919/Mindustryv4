package io.anuke.mindustry.maps.generation;

import arc.math.geom.Point2;

import arc.struct.Seq;
import arc.struct.IntIntMap;
import arc.func.Boolf;
import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.content.Liquids;
import io.anuke.mindustry.content.blocks.*;
import io.anuke.mindustry.game.Difficulty;
import io.anuke.mindustry.game.GameMode;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.type.AmmoType;
import io.anuke.mindustry.type.Recipe;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Edges;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.PowerBlock;
import io.anuke.mindustry.world.blocks.defense.Door;
import io.anuke.mindustry.world.blocks.defense.ForceProjector;
import io.anuke.mindustry.world.blocks.defense.MendProjector;
import io.anuke.mindustry.world.blocks.defense.Wall;
import io.anuke.mindustry.world.blocks.defense.turrets.ItemTurret;
import io.anuke.mindustry.world.blocks.defense.turrets.LiquidTurret;
import io.anuke.mindustry.world.blocks.defense.turrets.PowerTurret;
import io.anuke.mindustry.world.blocks.defense.turrets.Turret;
import io.anuke.mindustry.world.blocks.power.FusionReactor;
import io.anuke.mindustry.world.blocks.power.NuclearReactor;
import io.anuke.mindustry.world.blocks.power.PowerGenerator;
import io.anuke.mindustry.world.blocks.power.SolarGenerator;
import io.anuke.mindustry.world.blocks.storage.CoreBlock;
import io.anuke.mindustry.world.blocks.storage.StorageBlock;
import io.anuke.mindustry.world.blocks.units.UnitFactory;
import io.anuke.mindustry.world.blocks.units.UnitFactoryAdvanced;
import arc.func.Func2;
import arc.func.Intc2;
import arc.func.Func3;
import arc.math.geom.Geometry;
import arc.math.Mathf;

import static io.anuke.mindustry.Vars.state;
import static io.anuke.mindustry.Vars.content;

public class FortressGenerator{
    //TODO add a way to control the Seed for generating the bases
    private final static int coreDst = 60;
    private final static float customAttackDifficultyMultiplier = 2.25f;
    private final static float customAttackDifficultyBonus = 2f;
    private int enemyX, enemyY, coreX, coreY;
    private Team team;
    private Generation gen;

    public void generate(Generation gen, Team team, int coreX, int coreY, int enemyX, int enemyY){
        this.enemyX = enemyX;
        this.enemyY = enemyY;
        this.coreX = coreX;
        this.coreY = coreY;
        this.gen = gen;
        this.team = team;

        gen();
    }

    void gen(){
        gen.setBlock(enemyX, enemyY, StorageBlocks.core, team);
        gen.random.nextFloat();

        float difficulty = gen.sector == null ? state.difficulty.ordinal() : gen.sector.difficulty;
        if(gen.sector == null  && state.mode == GameMode.customAttackMode){
            difficulty = difficulty * customAttackDifficultyMultiplier + (customAttackDifficultyBonus * difficulty);
        }
        float difficultyScl = Mathf.clamp(difficulty / 20f + gen.random.range(0.25f), 0f, 0.9999f);
        float dscl2 = Mathf.clamp(0.5f + difficulty / 20f + gen.random.range(0.1f), 0f, 1.5f);

        Seq<Block> turrets = find(b -> b instanceof ItemTurret && !b.living);
        Seq<Block> powerTurrets = find(b -> b instanceof PowerTurret);
        Seq<Block> walls = find(b -> b instanceof Wall && !(b instanceof Door) && b.size == 1);

        Block wall = walls.get((int)(difficultyScl * walls.size));

        Turret powerTurret = (Turret) powerTurrets.get((int)(difficultyScl * powerTurrets.size));
        Turret bigTurret = (Turret) turrets.get(Mathf.clamp((int)((difficultyScl+0.2f+gen.random.range(0.2f)) * turrets.size), 0, turrets.size-1));
        Turret turret1 = (Turret) turrets.get(Mathf.clamp((int)((difficultyScl+gen.random.range(0.2f)) * turrets.size), 0, turrets.size-1));
        Turret turret2 = (Turret) turrets.get(Mathf.clamp((int)((difficultyScl+gen.random.range(0.2f)) * turrets.size), 0, turrets.size-1));
        float placeChance = difficultyScl*0.75f+0.25f;

        IntIntMap ammoPerType = new IntIntMap();
        for(Block turret : turrets){
            if(!(turret instanceof ItemTurret)) continue;
            ItemTurret t = (ItemTurret)turret;
            int size = t.getAmmoTypes().length;
            ammoPerType.put(t.id, (int)Mathf.clamp((int)(size* difficultyScl) + gen.random.range(1), 0, size - 1));
        }

        Func3<Tile, Block, Boolf<Tile>, Boolean> checker = (current, block, pred) -> {
            for(Point2 point : Edges.getEdges(block.size)){
                Tile tile = gen.tile(current.x + point.x, current.y + point.y);
                if(tile != null){
                    tile = tile.target();
                    if(tile.getTeamID() == team.ordinal() && pred.get(tile)){
                        return true;
                    }
                }
            }
            return false;
        };

        Func2<Block, Boolf<Tile>, Intc2> seeder = (block, pred) -> (x, y) -> {
            if(gen.canPlace(x, y, block) && ((block instanceof Wall && block.size == 1) || gen.random.chance(placeChance)) && checker.get(gen.tile(x, y), block, pred)){
                gen.setBlock(x, y, block, team);
            }
        };

        Func2<Block, Float, Intc2> placer = (block, chance) -> (x, y) -> {
            if(gen.canPlace(x, y, block) && gen.random.chance(chance)){
                gen.setBlock(x, y, block, team);
            }
        };

        Seq<Intc2> passes = Seq.with(
            //initial seeding solar panels
            placer.get(PowerBlocks.largeSolarPanel, 0.001f),

            placer.get(PowerBlocks.fusionReactor, 0.00038f),
            placer.get(PowerBlocks.thoriumReactor, 0.0005f),

            //extra seeding
            seeder.get(PowerBlocks.solarPanel, tile -> tile.block() == PowerBlocks.largeSolarPanel && gen.random.chance(0.3)),

            //coal gens
            seeder.get(PowerBlocks.combustionGenerator, tile -> tile.block() instanceof SolarGenerator && gen.random.chance(0.2)),

            //water extractors
            seeder.get(ProductionBlocks.waterExtractor, tile -> tile.block() instanceof NuclearReactor && gen.random.chance(0.3)),

            //mend projectors
            seeder.get(DefenseBlocks.mendProjector, tile -> tile.block() instanceof PowerGenerator && gen.random.chance(0.04)),

            seeder.get(DefenseBlocks.mendProjector, tile -> (tile.block() instanceof NuclearReactor || tile.block() instanceof FusionReactor) && gen.random.chance(0.10)),

            //power turrets
            seeder.get(powerTurret, tile -> tile.block() instanceof PowerGenerator && gen.random.chance(0.04)),

            //repair point
            seeder.get(UnitBlocks.repairPoint, tile -> tile.block() instanceof PowerGenerator && gen.random.chance(0.1)),

            //turrets1
            seeder.get(turret1, tile -> tile.block() instanceof PowerBlock && gen.random.chance(0.22 - turret1.size*0.02)),
            seeder.get(turret1, tile -> isUnitFactory(tile.block()) && gen.random.chance(0.12 - turret1.size*0.02)),
            seeder.get(turret1, tile -> (tile.block() instanceof ForceProjector| tile.block() instanceof CoreBlock) && gen.random.chance(0.20 - turret1.size*0.02)),

            //turrets2
            seeder.get(turret2, tile -> tile.block() instanceof PowerBlock && gen.random.chance(0.12 - turret2.size*0.02)),
            seeder.get(turret2, tile -> (tile.block() instanceof ForceProjector || tile.block() instanceof CoreBlock) && gen.random.chance(0.10 - turret2.size*0.02)),
            seeder.get(turret2, tile -> (tile.block() instanceof FusionReactor || tile.block() instanceof NuclearReactor) && gen.random.chance(0.15 - turret2.size*0.02)),
            seeder.get(turret2, tile -> (tile.block() instanceof UnitFactoryAdvanced) && gen.random.chance(0.08 - turret2.size*0.01)),

            //shields
            seeder.get(DefenseBlocks.forceProjector, tile -> (tile.block() instanceof CoreBlock || isUnitFactory(tile.block())) && gen.random.chance(0.2 * dscl2)),
            seeder.get(DefenseBlocks.forceProjector, tile -> (tile.block() instanceof NuclearReactor || tile.block() instanceof FusionReactor) && gen.random.chance(0.3 * dscl2)),

            //unit pads (assorted)

            seeder.get(UnitBlocks.daggerFactory, tile -> (tile.block() instanceof MendProjector || tile.block() instanceof ForceProjector) && gen.random.chance(0.3 * dscl2)),

            seeder.get(UnitBlocks.highTierFactory, tile -> (tile.block() instanceof FusionReactor || tile.block() instanceof NuclearReactor) && gen.random.chance(0.098 *dscl2)),

            seeder.get(UnitBlocks.revenantFactory, tile -> (tile.block() instanceof MendProjector || tile.block() instanceof ForceProjector) && gen.random.chance(0.099 *dscl2)),

            seeder.get(UnitBlocks.fortressFactory, tile -> (tile.block() instanceof MendProjector || tile.block() instanceof ForceProjector) && gen.random.chance(0.099 *dscl2)),

            seeder.get(UnitBlocks.crawlerFactory, tile ->(tile.block() instanceof MendProjector || tile.block() instanceof ForceProjector) && gen.random.chance(0.3 *dscl2)),
            seeder.get(UnitBlocks.bombdroneFactory, tile ->(tile.block() instanceof MendProjector || tile.block() instanceof ForceProjector) && gen.random.chance(0.3 *dscl2)),

            seeder.get(UnitBlocks.scrappeonFactory, tile ->(tile.block() instanceof MendProjector || tile.block() instanceof ForceProjector) && gen.random.chance(0.35 *dscl2)),

            seeder.get(UnitBlocks.scrapperFactory, tile ->(tile.block() instanceof MendProjector || tile.block() instanceof ForceProjector) && gen.random.chance(0.35 *dscl2)),

            //unit pads (assorted)
            seeder.get(UnitBlocks.wraithFactory, tile -> (tile.block() instanceof MendProjector || tile.block() instanceof ForceProjector) && gen.random.chance(0.3 * dscl2)),

            //unit pads (assorted)
            seeder.get(UnitBlocks.titanFactory, tile -> (tile.block() instanceof MendProjector || tile.block() instanceof ForceProjector) && gen.random.chance(0.23 * dscl2)),

            //unit pads (assorted)
            seeder.get(UnitBlocks.ghoulFactory, tile -> (tile.block() instanceof MendProjector || tile.block() instanceof ForceProjector) && gen.random.chance(0.23 * dscl2)),

            //vaults
            seeder.get(StorageBlocks.vault, tile -> (tile.block() instanceof CoreBlock || tile.block() instanceof ForceProjector) && gen.random.chance(0.4)),

            //big turrets
            seeder.get(bigTurret, tile -> (tile.block() instanceof StorageBlock || tile.block() instanceof  NuclearReactor || tile.block() instanceof FusionReactor)  && gen.random.chance(0.65)),

            //walls
            (x, y) -> {
                if(!gen.canPlace(x, y, wall)) return;

                for(Point2 point : Geometry.d8){
                    Tile tile = gen.tile(x + point.x, y + point.y);
                    if(tile != null){
                        tile = tile.target();
                        if(tile.getTeamID() == team.ordinal() && !(tile.block() instanceof Wall) && !isUnitFactory(tile.block())){
                            gen.setBlock(x, y, wall, team);
                            break;
                        }
                    }
                }
            },

            //mines
            placer.get(DefenseBlocks.shockMine, 0.02f * difficultyScl),

            //fill up turrets w/ ammo
            (x, y) -> {
                Tile tile = gen.tile(x, y);
                Block block = tile.block();

                if(block instanceof PowerTurret){
                    tile.entity.power.amount = block.powerCapacity;
                }else if(block instanceof ItemTurret){
                    ItemTurret turret = (ItemTurret)block;
                    AmmoType[] type = turret.getAmmoTypes();
                    int index = ammoPerType.get(block.id, 0);
                    block.handleStack(type[index].item, block.acceptStack(type[index].item, 9999, tile, null), tile, null);
                }else if(block instanceof NuclearReactor){
                    tile.entity.items.add(Items.thorium, block.itemCapacity);
                    tile.entity.liquids.add(Liquids.cryofluid, tile.block().liquidCapacity);
                }else if(block instanceof FusionReactor){
                    tile.entity.items.add(Items.blastCompound, block.itemCapacity);
                    tile.entity.liquids.add(Liquids.cryofluid, tile.block().liquidCapacity);
                }else if(block instanceof LiquidTurret){
                    tile.entity.liquids.add(Liquids.water, tile.block().liquidCapacity);
                }else if(block instanceof UnitFactoryAdvanced){
                    UnitFactoryAdvanced factory = (UnitFactoryAdvanced)block;
                    UnitFactoryAdvanced.UnitFactoryAdvancedEntity entity = tile.entity();
                    entity.buildTime = 0f;

                    if(factory.types != null && factory.types.length > 0){
                        entity.unitNumber = gen.random.nextInt(factory.types.length);
                        entity.unitSource = factory.types[entity.unitNumber];
                    }
                }
            }
        );

        for(Intc2 i : passes){
            for(int x = 0; x < gen.width; x++){
                for(int y = 0; y < gen.height; y++){
                    if(Mathf.dst((float)x, (float)y, (float)enemyX, (float)enemyY) > coreDst){
                        continue;
                    }

                    i.get(x, y);
                }
            }
        }
    }

    Seq<Block> find(Boolf<Block> pred){
        Seq<Block> out = new Seq<>();
        for(Block block : content.blocks()){
            if(pred.get(block) && Recipe.getByResult(block) != null){
                out.add(block);
            }
        }
        return out;
    }

    private boolean isUnitFactory(Block block){
        return block instanceof UnitFactory || block instanceof UnitFactoryAdvanced;
    }
}
