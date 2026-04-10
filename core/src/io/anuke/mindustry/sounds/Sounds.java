package io.anuke.mindustry.sounds;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.game.Content;
import io.anuke.mindustry.type.ContentType;
import io.anuke.mindustry.type.Weapon;
import io.anuke.mindustry.world.blocks.defense.turrets.Turret;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.blocks.storage.CoreBlock;
import io.anuke.mindustry.world.blocks.units.MechPad;
import io.anuke.mindustry.world.blocks.units.UnitFactory;
import io.anuke.mindustry.world.blocks.units.UnitFactoryAdvanced;

public class Sounds{
    private static final ObjectMap<String, Sound> all = new ObjectMap<>();

    public static final Sound none = new Sound(){
        @Override
        public long play(){
            return 0L;
        }

        @Override
        public long play(float volume){
            return 0L;
        }

        @Override
        public long play(float volume, float pitch, float pan){
            return 0L;
        }

        @Override
        public long loop(){
            return 0L;
        }

        @Override
        public long loop(float volume){
            return 0L;
        }

        @Override
        public long loop(float volume, float pitch, float pan){
            return 0L;
        }

        @Override
        public void stop(){
        }

        @Override
        public void pause(){
        }

        @Override
        public void resume(){
        }

        @Override
        public void dispose(){
        }

        @Override
        public void stop(long soundId){
        }

        @Override
        public void pause(long soundId){
        }

        @Override
        public void resume(long soundId){
        }

        @Override
        public void setLooping(long soundId, boolean looping){
        }

        @Override
        public void setPitch(long soundId, float pitch){
        }

        @Override
        public void setVolume(long soundId, float volume){
        }

        @Override
        public void setPan(long soundId, float pan, float volume){
        }
    };

    public static Sound shoot;
    public static Sound shootDuo;
    public static Sound shootArc;
    public static Sound shootRevenant;
    public static Sound shootLancer;
    public static Sound shootLaser;
    public static Sound shootSalvo;
    public static Sound shootScatter;
    public static Sound shootCyclone;
    public static Sound shootMeltdown;
    public static Sound shootMissileSmall;
    public static Sound shootRipple;
    public static Sound shootArtillery;
    public static Sound shootArtillerySmall;
    public static Sound shootDart;
    public static Sound loopThoriumReactor;
    public static Sound loopCombustion;
    public static Sound loopDifferential;
    public static Sound loopSmelter;
    public static Sound loopGrind;
    public static Sound loopMachine;
    public static Sound loopMachine2;
    public static Sound loopMachineSpin;
    public static Sound loopHum;
    public static Sound loopPulse;
    public static Sound loopThruster;
    public static Sound loopConveyor;
    public static Sound loopCultivator;
    public static Sound loopDrill;
    public static Sound loopCircuit;
    public static Sound loopUnitBuilding;
    public static Sound blockExplode;
    public static Sound blockExplodeAlt;
    public static Sound blockExplodeElectric;
    public static Sound blockExplodeElectricBig;
    public static Sound blockExplodeExplosive;
    public static Sound blockExplodeExplosiveAlt;
    public static Sound blockExplodeFlammable;
    public static Sound blockExplodeWall;
    public static Sound explosion;
    public static Sound explosionArtillery;
    public static Sound explosionArtilleryShock;
    public static Sound explosionArtilleryShockBig;
    public static Sound shootFuse;
    public static Sound explosionCrawler;
    public static Sound explosionReactor;
    public static Sound shockBullet;
    public static Sound unitExplode;
    public static Sound blockBreak;
    public static Sound blockPlace;
    public static Sound unitCreate;
    public static Sound unitCreateBig;

    public static void init(){
        if(Vars.headless || Vars.soundController == null) return;

        all.clear();

        shoot = register("shoot", Vars.soundController.load("shoot", "sounds/shoot/shoot.ogg"));
        shootDuo = register("shootDuo", Vars.soundController.load("shootDuo", "sounds/shoot/shootDuo.ogg"));
        shootArc = register("shootArc", Vars.soundController.load("shootArc", "sounds/shoot/shootArc.ogg"));
        shootRevenant = register("shootRevenant", Vars.soundController.load("shootRevenant", "sounds/shoot/shootRevenant.ogg"));
        shootLancer = register("shootLancer", Vars.soundController.load("shootLancer", "sounds/shoot/shootLancer.ogg"));
        shootArtillery = register("shootArtillery", Vars.soundController.load("shootArtillery", "sounds/shoot/shootArtillery.ogg"));
        shootArtillerySmall = register("shootArtillerySmall", Vars.soundController.load("shootArtillerySmall", "sounds/shoot/shootArtillerySmall.ogg"));
        shootRipple = register("shootRipple", Vars.soundController.load("shootRipple", "sounds/shoot/shootRipple.ogg"));
        shootDart = register("shootDart", Vars.soundController.load("shootDart", "sounds/shoot/shootDart.ogg"));
        shootLaser = register("shootLaser", Vars.soundController.load("shootLaser", "sounds/shoot/shootLaser.ogg"));
        shootSalvo = register("shootSalvo", Vars.soundController.load("shootSalvo", "sounds/shoot/shootSalvo.ogg"));
        shootScatter = register("shootScatter", Vars.soundController.load("shootScatter", "sounds/shoot/shootScatter.ogg"));
        shootMissileSmall = register("shootMissileSmall", Vars.soundController.load("shootMissileSmall", "sounds/shoot/shootMissileSmall.ogg"));
        shootCyclone = register("shootCyclone", Vars.soundController.load("shootCyclone", "sounds/shoot/shootCyclone.ogg"));
        shootFuse = register("shootFuse", Vars.soundController.load("shootFuse", "sounds/shoot/shootFuse.ogg"));
        shootMeltdown = register("shootMeltdown", Vars.soundController.load("shootMeltdown", "sounds/shoot/shootMeltdown.ogg"));
        loopThoriumReactor = register("loopThoriumReactor", Vars.soundController.load("loopThoriumReactor", "sounds/loops/loopThoriumReactor.ogg"));
        loopCombustion = register("loopCombustion", Vars.soundController.load("loopCombustion", "sounds/loops/loopCombustion.ogg"));
        loopDifferential = register("loopDifferential", Vars.soundController.load("loopDifferential", "sounds/loops/loopDifferential.ogg"));
        loopSmelter = register("loopSmelter", Vars.soundController.load("loopSmelter", "sounds/loops/loopSmelter.ogg"));
        loopGrind = register("loopGrind", Vars.soundController.load("loopGrind", "sounds/loops/loopGrind.ogg"));
        loopMachine = register("loopMachine", Vars.soundController.load("loopMachine", "sounds/loops/loopMachine.ogg"));
        loopMachine2 = register("loopMachine2", Vars.soundController.load("loopMachine2", "sounds/loops/loopMachine2.ogg"));
        loopMachineSpin = register("loopMachineSpin", Vars.soundController.load("loopMachineSpin", "sounds/loops/loopMachineSpin.ogg"));
        loopHum = register("loopHum", Vars.soundController.load("loopHum", "sounds/loops/loopHum.ogg"));
        loopPulse = register("loopPulse", Vars.soundController.load("loopPulse", "sounds/loops/loopPulse.ogg"));
        loopConveyor = register("loopConveyor", Vars.soundController.load("loopConveyor", "sounds/loops/loopConveyor.ogg"));
        loopCultivator = register("loopCultivator", Vars.soundController.load("loopCultivator", "sounds/loops/loopCultivator.ogg"));
        loopDrill = register("loopDrill", Vars.soundController.load("loopDrill", "sounds/loops/loopDrill.ogg"));
        loopCircuit = register("loopCircuit", Vars.soundController.load("loopCircuit", "sounds/loops/loopCircuit.ogg"));
        loopUnitBuilding = register("loopUnitBuilding", Vars.soundController.load("loopUnitBuilding", "sounds/loops/loopUnitBuilding.ogg"));
        loopThruster = register("loopThruster", Vars.soundController.load("loopThruster", "sounds/loops/loopThruster.ogg"));
        register("blockExplode1", Vars.soundController.load("blockExplode1", "sounds/explosions/blockExplode1.ogg"));
        register("blockExplode2", Vars.soundController.load("blockExplode2", "sounds/explosions/blockExplode2.ogg"));
        register("blockExplode3", Vars.soundController.load("blockExplode3", "sounds/explosions/blockExplode3.ogg"));
        register("blockExplode1Alt", Vars.soundController.load("blockExplode1Alt", "sounds/explosions/blockExplode1Alt.ogg"));
        register("blockExplode2Alt", Vars.soundController.load("blockExplode2Alt", "sounds/explosions/blockExplode2Alt.ogg"));
        blockExplodeElectric = register("blockExplodeElectric", Vars.soundController.load("blockExplodeElectric", "sounds/explosions/blockExplodeElectric.ogg"));
        blockExplodeElectricBig = register("blockExplodeElectricBig", Vars.soundController.load("blockExplodeElectricBig", "sounds/explosions/blockExplodeElectricBig.ogg"));
        blockExplodeExplosive = register("blockExplodeExplosive", Vars.soundController.load("blockExplodeExplosive", "sounds/explosions/blockExplodeExplosive.ogg"));
        blockExplodeExplosiveAlt = register("blockExplodeExplosiveAlt", Vars.soundController.load("blockExplodeExplosiveAlt", "sounds/explosions/blockExplodeExplosiveAlt.ogg"));
        blockExplodeFlammable = register("blockExplodeFlammable", Vars.soundController.load("blockExplodeFlammable", "sounds/explosions/blockExplodeFlammable.ogg"));
        blockExplodeWall = register("blockExplodeWall", Vars.soundController.load("blockExplodeWall", "sounds/explosions/blockExplodeWall.ogg"));
        explosion = register("explosion", Vars.soundController.load("explosion", "sounds/explosions/explosion.ogg"));
        explosionArtillery = register("explosionArtillery", Vars.soundController.load("explosionArtillery", "sounds/explosions/explosionArtillery.ogg"));
        explosionArtilleryShock = register("explosionArtilleryShock", Vars.soundController.load("explosionArtilleryShock", "sounds/explosions/explosionArtilleryShock.ogg"));
        explosionArtilleryShockBig = register("explosionArtilleryShockBig", Vars.soundController.load("explosionArtilleryShockBig", "sounds/explosions/explosionArtilleryShockBig.ogg"));
        explosionCrawler = register("explosionCrawler", Vars.soundController.load("explosionCrawler", "sounds/explosions/explosionCrawler.ogg"));
        register("explosionReactor1", Vars.soundController.load("explosionReactor1", "sounds/explosions/explosionReactor.ogg"));
        register("explosionReactor2", Vars.soundController.load("explosionReactor2", "sounds/explosions/explosionReactor2.ogg"));
        shockBullet = register("shockBullet", Vars.soundController.load("shockBullet", "sounds/explosions/shockBullet.ogg"));
        register("unitExplode1", Vars.soundController.load("unitExplode1", "sounds/explosions/unitExplode1.ogg"));
        register("unitExplode2", Vars.soundController.load("unitExplode2", "sounds/explosions/unitExplode2.ogg"));
        register("unitExplode3", Vars.soundController.load("unitExplode3", "sounds/explosions/unitExplode3.ogg"));
        register("blockPlace1", Vars.soundController.load("blockPlace1", "sounds/blocks/blockPlace1.ogg"));
        register("blockPlace2", Vars.soundController.load("blockPlace2", "sounds/blocks/blockPlace2.ogg"));
        register("blockPlace3", Vars.soundController.load("blockPlace3", "sounds/blocks/blockPlace3.ogg"));
        register("blockBreak1", Vars.soundController.load("blockBreak1", "sounds/blocks/blockBreak1.ogg"));
        register("blockBreak2", Vars.soundController.load("blockBreak2", "sounds/blocks/blockBreak2.ogg"));
        register("blockBreak3", Vars.soundController.load("blockBreak3", "sounds/blocks/blockBreak3.ogg"));
        unitCreate = register("unitCreate", Vars.soundController.load("unitCreate", "sounds/blocks/unitCreate.ogg"));
        unitCreateBig = register("unitCreateBig", Vars.soundController.load("unitCreateBig", "sounds/blocks/unitCreateBig.ogg"));


        Vars.soundController.createGroup("blockExplode", "blockExplode1", "blockExplode2", "blockExplode3");
        Vars.soundController.createGroup("blockExplodeAlt", "blockExplode1Alt", "blockExplode2Alt");
        Vars.soundController.createGroup("explosionReactor", "explosionReactor1", "explosionReactor2");
        Vars.soundController.createGroup("unitExplode", "unitExplode1", "unitExplode2", "unitExplode3");
        Vars.soundController.createGroup("blockPlace", "blockPlace1", "blockPlace2", "blockPlace3");
        Vars.soundController.createGroup("blockBreak", "blockBreak1", "blockBreak2", "blockBreak3");

        blockExplode = register("blockExplode", randomGroup("blockExplode"));
        blockExplodeAlt = register("blockExplodeAlt", randomGroup("blockExplodeAlt"));
        explosionReactor = register("explosionReactor", randomGroup("explosionReactor"));
        unitExplode = register("unitExplode", randomGroup("unitExplode"));
        blockPlace = register("blockPlace", randomGroup("blockPlace"));
        blockBreak = register("blockBreak", randomGroup("blockBreak"));

        Vars.soundController.setPriority("blockExplode1", 4);
        Vars.soundController.setPriority("blockExplode2", 4);
        Vars.soundController.setPriority("blockExplode3", 4);
        Vars.soundController.setPriority("blockExplode1Alt", 4);
        Vars.soundController.setPriority("blockExplode2Alt", 4);
        Vars.soundController.setPriority("blockExplodeElectric", 4);
        Vars.soundController.setPriority("blockExplodeElectricBig", 4);
        Vars.soundController.setPriority("blockExplodeExplosive", 4);
        Vars.soundController.setPriority("blockExplodeExplosiveAlt", 4);
        Vars.soundController.setPriority("blockExplodeFlammable", 4);
        Vars.soundController.setPriority("blockExplodeWall", 4);
        Vars.soundController.setPriority("explosion", 4);
        Vars.soundController.setPriority("explosionArtillery", 4);
        Vars.soundController.setPriority("explosionArtilleryShock", 4);
        Vars.soundController.setPriority("explosionArtilleryShockBig", 4);
        Vars.soundController.setPriority("explosionCrawler", 4);
        Vars.soundController.setPriority("explosionReactor1", 4);
        Vars.soundController.setPriority("explosionReactor2", 4);
        Vars.soundController.setPriority("shockBullet", 4);
        Vars.soundController.setPriority("unitExplode1", 4);
        Vars.soundController.setPriority("unitExplode2", 4);
        Vars.soundController.setPriority("unitExplode3", 4);

        Vars.soundController.setPriority("shoot", 3);
        Vars.soundController.setPriority("shootDuo", 3);
        Vars.soundController.setPriority("shootArc", 3);
        Vars.soundController.setPriority("shootRevenant", 3);
        Vars.soundController.setPriority("shootLancer", 3);
        Vars.soundController.setPriority("shootArtillery", 3);
        Vars.soundController.setPriority("shootArtillerySmall", 3);
        Vars.soundController.setPriority("shootRipple", 3);
        Vars.soundController.setPriority("shootDart", 3);
        Vars.soundController.setPriority("shootLaser", 3);
        Vars.soundController.setPriority("shootSalvo", 3);
        Vars.soundController.setPriority("shootScatter", 3);
        Vars.soundController.setPriority("shootMissileSmall", 3);
        Vars.soundController.setPriority("shootCyclone", 3);
        Vars.soundController.setPriority("shootFuse", 3);
        Vars.soundController.setPriority("shootMeltdown", 3);
        Vars.soundController.setPriority("unitCreateBig", 3);

        Vars.soundController.setPriority("loopThoriumReactor", 2);
        Vars.soundController.setPriority("loopDifferential", 2);
        Vars.soundController.setPriority("loopSmelter", 2);
        Vars.soundController.setPriority("loopGrind", 2);
        Vars.soundController.setPriority("loopMachine", 2);
        Vars.soundController.setPriority("loopMachine2", 2);
        Vars.soundController.setPriority("loopMachineSpin", 2);
        Vars.soundController.setPriority("loopHum", 2);
        Vars.soundController.setPriority("loopPulse", 2);
        Vars.soundController.setPriority("loopCombustion", 2);
        Vars.soundController.setPriority("loopDrill", 2);
        Vars.soundController.setPriority("loopCircuit", 2);
        Vars.soundController.setPriority("loopUnitBuilding", 2);
        Vars.soundController.setPriority("blockPlace1", 2);
        Vars.soundController.setPriority("blockPlace2", 2);
        Vars.soundController.setPriority("blockPlace3", 2);
        Vars.soundController.setPriority("blockBreak1", 2);
        Vars.soundController.setPriority("blockBreak2", 2);
        Vars.soundController.setPriority("blockBreak3", 2);
        Vars.soundController.setPriority("unitCreate", 2);

        Vars.soundController.setPriority("loopConveyor", 1);
        Vars.soundController.setPriority("loopCultivator", 1);
        Vars.soundController.setPriority("loopThruster", 1);

        rebindContentSounds();
    }

    public static void rebindContentSounds(){
        for(Content content : Vars.content.getBy(ContentType.weapon)){
            if(content instanceof Weapon){
                Weapon weapon = (Weapon)content;
                if(weapon.shootSoundName != null){
                    weapon.shootSound = get(weapon.shootSoundName);
                }
            }
        }

        for(Content content : Vars.content.getBy(ContentType.block)){
            if(content instanceof Block){
                Block block = (Block)content;
                if(block.ambientSoundName != null){
                    block.ambientSound = get(block.ambientSoundName);
                }
                if(block instanceof Turret){
                    Turret turret = (Turret)block;
                    if(turret.shootSoundName != null){
                        turret.shootSound = get(turret.shootSoundName);
                    }
                }
                if(block instanceof UnitFactory){
                    UnitFactory factory = (UnitFactory)block;
                    if(factory.buildUnitSoundName != null){
                        factory.buildUnitSound = get(factory.buildUnitSoundName);
                    }
                }
                if(block instanceof UnitFactoryAdvanced){
                    UnitFactoryAdvanced factory = (UnitFactoryAdvanced)block;
                    if(factory.buildUnitSoundName != null){
                        factory.buildUnitSound = get(factory.buildUnitSoundName);
                    }
                }
                if(block instanceof MechPad){
                    MechPad factory = (MechPad)block;
                    if(factory.buildPlayerSoundName != null){
                        factory.buildPlayerSound = get(factory.buildPlayerSoundName);
                    }
                }
                if(block instanceof CoreBlock){
                    CoreBlock core = (CoreBlock)block;
                    if(core.buildPlayerSoundName != null){
                        core.buildPlayerSound = get(core.buildPlayerSoundName);
                    }
                }
            }
        }
    }

    public static Sound get(String name){
        return name == null ? null : all.get(name);
    }

    private static Sound register(String name, Sound sound){
        all.put(name, sound);
        return sound;
    }

    private static Sound randomGroup(String group){
        return new Sound(){
            @Override
            public long play(){
                return Vars.soundController == null ? -1L : Vars.soundController.playRandom(group);
            }

            @Override
            public long play(float volume){
                return Vars.soundController == null ? -1L : Vars.soundController.playRandom(group, volume);
            }

            @Override
            public long play(float volume, float pitch, float pan){
                return Vars.soundController == null ? -1L : Vars.soundController.playRandom(group, volume, pitch, pan);
            }

            @Override
            public long loop(){
                Sound sound = Vars.soundController == null ? null : Vars.soundController.random(group);
                return sound == null ? -1L : sound.loop();
            }

            @Override
            public long loop(float volume){
                Sound sound = Vars.soundController == null ? null : Vars.soundController.random(group);
                return sound == null ? -1L : sound.loop(volume);
            }

            @Override
            public long loop(float volume, float pitch, float pan){
                Sound sound = Vars.soundController == null ? null : Vars.soundController.random(group);
                return sound == null ? -1L : sound.loop(volume, pitch, pan);
            }

            @Override
            public void stop(){
            }

            @Override
            public void pause(){
            }

            @Override
            public void resume(){
            }

            @Override
            public void dispose(){
            }

            @Override
            public void stop(long soundId){
            }

            @Override
            public void pause(long soundId){
            }

            @Override
            public void resume(long soundId){
            }

            @Override
            public void setLooping(long soundId, boolean looping){
            }

            @Override
            public void setPitch(long soundId, float pitch){
            }

            @Override
            public void setVolume(long soundId, float volume){
            }

            @Override
            public void setPan(long soundId, float pan, float volume){
            }
        };
    }
}
