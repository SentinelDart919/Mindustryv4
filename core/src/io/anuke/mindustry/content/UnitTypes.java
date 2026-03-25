package io.anuke.mindustry.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ObjectSet;
import io.anuke.mindustry.entities.units.GroundUnit;
import io.anuke.mindustry.entities.units.UnitType;
import io.anuke.mindustry.entities.units.types.*;
import io.anuke.mindustry.game.ContentList;
import io.anuke.mindustry.type.ContentType;

public class UnitTypes implements ContentList{
    public static UnitType
        scavenger, spirit, ghost, phantom,
        alphaDrone,
        scrapper , wraith, ghoul, revenant, lich,
        crawler, bombDrone,
        scrappeon, dagger, titan, fortress, chaosarray,
        debugtank;


    @Override
    public void load(){
        alphaDrone = new UnitType("alpha-drone", AlphaDrone.class, AlphaDrone::new){
            {
                isFlying = true;
                drag = 0.005f;
                speed = 0.6f;
                maxVelocity = 1.7f;
                range = 40f;
                health = 45;
                hitsize = 4f;
                mass = 0.1f;
                weapon = Weapons.droneBlaster;
                trailColor = Color.valueOf("ffd37f");
            }

            @Override
            public boolean isHidden() {
                return true;
            }
        };

        scavenger = new UnitType("scavenger", Scavenger.class, Scavenger::new){{
            weapon = Weapons.mineBlaster;
            isFlying = true;
            drag = 0.01f;
            speed = 0.18f;
            maxVelocity = 0.6f;
            range = 50f;
            health = 20;
            toMine = ObjectSet.with(Items.scrap);
            itemCapacity = 50;
        }};

        spirit = new UnitType("spirit", Spirit.class, Spirit::new){{
            weapon = Weapons.healBlasterDrone;
            isFlying = true;
            drag = 0.01f;
            speed = 0.2f;
            maxVelocity = 0.8f;
            range = 50f;
            health = 60;

        }};

        ghost = new UnitType("ghost", Ghost.class, Ghost::new){{
            weapon = Weapons.healBlasterDrone2;
            isFlying = true;
            drag = 0.01f;
            mass = 1.5f;
            speed = 0.2f;
            maxVelocity = 1.4f;
            range = 60f;
            itemCapacity = 55;
            health = 90;
            buildPower = 0.6f;
            minePower = 0.98f;
            toMine = ObjectSet.with(Items.thorium, Items.titanium);
        }};

        phantom = new UnitType("phantom", Phantom.class, Phantom::new){{
            weapon = Weapons.healBlasterDrone2;
            isFlying = true;
            drag = 0.01f;
            mass = 2f;
            speed = 0.2f;
            maxVelocity = 0.9f;
            range = 70f;
            itemCapacity = 70;
            health = 220;
            buildPower = 0.9f;
            minePower = 1.2f;
            toMine = ObjectSet.with(Items.lead, Items.copper, Items.titanium, Items.thorium);
        }};

        scrappeon = new UnitType("scrappeon", Scrappeon.class, Scrappeon::new){{
            maxVelocity = 0.98f;
            speed = 0.142f;
            drag = 0.4f;
            hitsize = 7.8f;
            mass = 1.25f;
            weapon = Weapons.scrapLockBlaster;
            health = 60;
        }};

        crawler = new UnitType("crawler", Crawler.class, Crawler::new){{
            weapon = Weapons.kamikaze;
            maxVelocity = 1.25f;
            speed = 0.36f;
            drag = 0.01f;
            hitsize = 8f;
            mass = 1.75f;
            health = 100f;
        }};

        bombDrone = new UnitType("bomb_drone", BombDrone.class, BombDrone::new){{
            isFlying = true;
            weapon = Weapons.kamikaze;
            maxVelocity = 1.50f;
            speed = 0.32f;
            drag = 0.01f;
            hitsize = 7.89f;
            mass = 1.25f;
            health = 60;
        }};


        dagger = new UnitType("dagger", Dagger.class, Dagger::new){{
            maxVelocity = 1.1f;
            speed = 0.2f;
            drag = 0.4f;
            hitsize = 8f;
            mass = 1.75f;
            weapon = Weapons.chainBlaster;
            health = 130;
        }};

        titan = new UnitType("titan", Titan.class, Titan::new){{
            maxVelocity = 0.8f;
            speed = 0.18f;
            drag = 0.4f;
            mass = 3.5f;
            hitsize = 9f;
            rotatespeed = 0.1f;
            weapon = Weapons.flamethrower;
            health = 440;
        }};

        fortress = new UnitType("fortress", Fortress.class, Fortress::new){{
            maxVelocity = 0.8f;
            speed = 0.15f;
            drag = 0.4f;
            mass = 5f;
            hitsize = 10f;
            rotatespeed = 0.06f;
            weaponOffsetX = 1;
            targetAir = false;
            weapon = Weapons.artillery;
            health = 800;
        }};

        scrapper = new UnitType("scrapper", Scrapper.class, Scrapper::new){{
            speed = 0.2f;
            maxVelocity = 1.2f;
            drag = 0.01f;
            mass = 0.59f;
            weapon = Weapons.scrapBlaster;
            isFlying = true;
            health = 30;
        }};

        wraith = new UnitType("wraith", Wraith.class, Wraith::new){{
            speed = 0.3f;
            maxVelocity = 1.9f;
            drag = 0.01f;
            mass = 1.5f;
            weapon = Weapons.chainBlaster;
            isFlying = true;
            health = 70;
        }};

        ghoul = new UnitType("ghoul", Ghoul.class, Ghoul::new){{
            health = 250;
            speed = 0.2f;
            maxVelocity = 1.4f;
            mass = 3f;
            drag = 0.01f;
            isFlying = true;
            targetAir = false;
            weapon = Weapons.bomber;
        }};

        revenant = new UnitType("revenant", Revenant.class, Revenant::new){{
            health = 250;
            mass = 5f;
            hitsize = 12f;
            speed = 0.14f;
            maxVelocity = 1.3f;
            drag = 0.01f;
            range = 80f;
            isFlying = true;
            weapon = Weapons.laserBurster;
        }};

        lich = new UnitType("lich", Lich.class, Lich::new){{

            health = 7000;
            mass = 20f;
            hitsize = 40f;
            speed = 0.01f;
            maxVelocity = 0.6f;
            drag = 0.02f;
            range = 80f;
            isFlying = true;
            rotatespeed = 0.01f;
            baseRotateSpeed = 0.04f;
            weaponOffsetX = 14f;
            weaponOffsetY = -3f;
            weapon = Weapons.lichMissiles;
        }};

        chaosarray = new UnitType("chaos-array", ChaosArray.class, ChaosArray::new){{
            health = 3000;
            mass = 5f;
            hitsize = 20;
            speed = 0.12f;
            maxVelocity = 0.68f;
            drag = 0.4f;
            rotatespeed = 0.06f;
            weaponOffsetX = 17f;
            weaponOffsetY = 2f;
            weapon = Weapons.chaos;
        }};

        debugtank = new UnitType("debugtank", TankUnit.class, TankUnit::new){{
            isTank = true;
            mass = 1f;
            hitsize = 2f;
            speed = 0.2f;
            maxVelocity = 2f;
            drag = 0.4f;
            rotatespeed = 0.06f;
            baseRotateSpeed = 0.04f;
            range = 80f;
            weaponOffsetX = 0;
            weaponOffsetY = 0;
            weapon = Weapons.debugtankturret;
        }};
    }

    @Override
    public ContentType type(){
        return ContentType.unit;
    }
}
