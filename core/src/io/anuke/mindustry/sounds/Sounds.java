package io.anuke.mindustry.sounds;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.game.Content;
import io.anuke.mindustry.type.ContentType;
import io.anuke.mindustry.type.Weapon;
import io.anuke.mindustry.world.blocks.defense.turrets.Turret;
import io.anuke.mindustry.world.Block;

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
    public static Sound loopConveyor;
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
        loopConveyor = register("loopConveyor", Vars.soundController.load("loopConveyor", "sounds/loops/loopConveyor.ogg"));
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

        /*Vars.soundController.setMinInterval("shoot", 111L);
        Vars.soundController.setMinInterval("shootDuo", 132L);
        Vars.soundController.setMinInterval("shootArc", 114L);
        Vars.soundController.setMinInterval("shootRevenant", 141L);
        Vars.soundController.setMinInterval("shootLancer", 141L);
        Vars.soundController.setMinInterval("shootLaser", 142L);
        Vars.soundController.setMinInterval("shootArtillery", 119);
        Vars.soundController.setMinInterval("shootArtillerySmall", 122);
        Vars.soundController.setMinInterval("shootRipple", 142L);*/

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
