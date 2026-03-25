package io.anuke.mindustry.content.blocks;

import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.content.UnitTypes;
import io.anuke.mindustry.entities.units.UnitType;
import io.anuke.mindustry.game.ContentList;
import io.anuke.mindustry.type.ItemStack;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.blocks.units.*;

public class UnitBlocks extends BlockList implements ContentList{
    public static Block
        scavengerFactory, spiritFactory, phantomFactory, ghostFactory,
        scrapperFactory, wraithFactory, ghoulFactory, revenantFactory,
        scrappeonFactory,
        daggerFactory, titanFactory, fortressFactory,
            crawlerFactory, bombdroneFactory,
        reconstructor, highTierFactory, repairPoint, commandCenter;

    @Override
    public void load(){
        scavengerFactory = new UnitFactory("scavenger-factory"){{
            type = UnitTypes.scavenger;
            produceTime = 7600;
            size = 2;
            consumes.power(0.04f);
            consumes.items(new ItemStack(Items.scrap, 10));
        }};

        spiritFactory = new UnitFactory("spirit-factory"){{
            type = UnitTypes.spirit;
            produceTime = 5700;
            size = 2;
            consumes.power(0.08f);
            consumes.items(new ItemStack(Items.silicon, 30), new ItemStack(Items.lead, 30));
        }};

        ghostFactory = new UnitFactory("ghost-factory"){{
            type = UnitTypes.ghost;
            produceTime = 6300;
            size = 2;
            consumes.power(0.12f);
            consumes.items(new ItemStack(Items.silicon, 50), new ItemStack(Items.lead, 50), new ItemStack(Items.densealloy, 60));
        }};

        phantomFactory = new UnitFactory("phantom-factory"){{
            type = UnitTypes.phantom;
            produceTime = 7300;
            size = 2;
            consumes.power(0.2f);
            consumes.items(new ItemStack(Items.silicon, 70), new ItemStack(Items.lead, 80), new ItemStack(Items.titanium, 80));
        }};

        wraithFactory = new UnitFactory("wraith-factory"){{
            type = UnitTypes.wraith;
            produceTime = 1800;
            size = 2;
            consumes.power(0.1f);
            consumes.items(new ItemStack(Items.silicon, 10), new ItemStack(Items.titanium, 10));
        }};

        scrapperFactory = new UnitFactory("scrapper-factory"){{
            type = UnitTypes.scrapper;
            produceTime = 900;
            size = 2;
            consumes.power(0.05f);
            consumes.items(new ItemStack(Items.scrap, 10), new ItemStack(Items.lead, 10));
        }};

        ghoulFactory = new UnitFactory("ghoul-factory"){{
            type = UnitTypes.ghoul;
            produceTime = 3600;
            size = 3;
            consumes.power(0.2f);
            shadow = "shadow-round-3";
            consumes.items(new ItemStack(Items.silicon, 30), new ItemStack(Items.titanium, 30), new ItemStack(Items.plastanium, 20));
        }};

        revenantFactory = new UnitFactory("revenant-factory"){{
            type = UnitTypes.revenant;
            produceTime = 8000;
            size = 4;
            consumes.power(0.3f);
            shadow = "shadow-round-4";
            consumes.items(new ItemStack(Items.silicon, 80), new ItemStack(Items.titanium, 80), new ItemStack(Items.plastanium, 50));
        }};

        scrappeonFactory = new UnitFactory("scrappeon-factory"){{
            type = UnitTypes.scrappeon;
            produceTime = 1200;
            size = 2;
            consumes.power(0.02f);
            consumes.items(new ItemStack(Items.scrap, 10));
        }};

        crawlerFactory = new UnitFactory("crawler-factory"){{
            type = UnitTypes.crawler;
            produceTime = 1000;
            size = 2;
            consumes.power(0.04f);
            consumes.items(new ItemStack(Items.silicon, 10), new ItemStack(Items.coal, 10));
        }};

        bombdroneFactory = new UnitFactory("bomb_drone-factory"){{
            type = UnitTypes.bombDrone;
            produceTime = 1200;
            size = 2;
            consumes.power(0.04f);
            consumes.items(new ItemStack(Items.silicon, 10), new ItemStack(Items.coal, 10), new ItemStack(Items.lead, 20));
        }};

        daggerFactory = new UnitFactory("dagger-factory"){{
            type = UnitTypes.dagger;
            produceTime = 1700;
            size = 2;
            consumes.power(0.05f);
            consumes.items(new ItemStack(Items.silicon, 10));
        }};

        titanFactory = new UnitFactory("titan-factory"){{
            type = UnitTypes.titan;
            produceTime = 3400;
            size = 3;
            consumes.power(0.15f);
            shadow = "shadow-round-3";
            consumes.items(new ItemStack(Items.silicon, 20), new ItemStack(Items.thorium, 30));
        }};

        fortressFactory = new UnitFactory("fortress-factory"){{
            type = UnitTypes.fortress;
            produceTime = 5000;
            size = 3;
            consumes.power(0.2f);
            shadow = "shadow-round-3";
            consumes.items(new ItemStack(Items.silicon, 40), new ItemStack(Items.thorium, 50));
        }};
        highTierFactory = new UnitFactoryAdvanced("high-tier-factory"){{
            types = new UnitType[]{
                    UnitTypes.lich,
                    UnitTypes.revenant,
                    UnitTypes.fortress,
            };
            consumerStacks = new ItemStack[][]{
                    new ItemStack[]{
                            new ItemStack(Items.silicon, 500),
                            new ItemStack(Items.lead, 650),
                            new ItemStack(Items.thorium, 300),
                            new ItemStack(Items.plastanium, 250),
                            new ItemStack(Items.chromium, 400),
                            new ItemStack(Items.surgealloy, 400),
                    },
                    new ItemStack[]{
                            new ItemStack(Items.silicon, 80),
                            new ItemStack(Items.titanium, 80),
                            new ItemStack(Items.plastanium, 50)},
                    new ItemStack[]{
                            new ItemStack(Items.silicon, 40),
                            new ItemStack(Items.thorium, 50)}

            };
            producerTimes = new float[]{
                    17000f,
                    7500f,
                    11500f

            };
            size = 8;
            consumes.power(0.12f);
        }};


        repairPoint = new RepairPoint("repair-point"){{
            shadow = "shadow-round-1";
            repairSpeed = 0.1f;
        }};

        reconstructor = new Reconstructor("reconstructor"){{
            size = 2;
        }};

        commandCenter = new CommandCenter("command-center"){{
            size = 2;
        }};
    }
}
