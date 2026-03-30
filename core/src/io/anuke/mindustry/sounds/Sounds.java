package io.anuke.mindustry.sounds;

import com.badlogic.gdx.audio.Sound;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.content.Weapons;
import io.anuke.mindustry.content.blocks.TurretBlocks;
import io.anuke.mindustry.world.blocks.distribution.Conveyor;
import io.anuke.mindustry.world.blocks.defense.turrets.Turret;

public class Sounds{
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
    public static Sound loopThoriumReactor;
    public static Sound loopCombustion;
    public static Sound loopConveyor;

    public static void init(){
        if(Vars.headless || Vars.soundController == null) return;

        shoot = Vars.soundController.load("shoot", "sounds/shoot/shoot.ogg");
        shootDuo = Vars.soundController.load("shootDuo", "sounds/shoot/shootDuo.ogg");
        shootArc = Vars.soundController.load("shootArc", "sounds/shoot/shootArc.ogg");
        shootRevenant = Vars.soundController.load("shootRevenant", "sounds/shoot/shootRevenant.ogg");
        shootLancer = Vars.soundController.load("shootLancer", "sounds/shoot/shootLancer.ogg");
        shootLaser = Vars.soundController.load("shootLaser", "sounds/shoot/shootLaser.ogg");
        loopThoriumReactor = Vars.soundController.load("loopThoriumReactor", "sounds/loops/loopThoriumReactor.ogg");
        loopCombustion = Vars.soundController.load("loopCombustion", "sounds/loops/loopCombustion.ogg");
        loopConveyor = Vars.soundController.load("loopConveyor", "sounds/loops/loopConveyor.ogg");

        Vars.soundController.setMinInterval("shoot", 111L);
        Vars.soundController.setMinInterval("shootDuo", 132L);
        Vars.soundController.setMinInterval("shootArc", 114L);
        Vars.soundController.setMinInterval("shootRevenant", 141L);
        Vars.soundController.setMinInterval("shootLancer", 141L);
        Vars.soundController.setMinInterval("shootLaser", 142L);

        rebindContentSounds();
    }

    public static void rebindContentSounds(){
        if(TurretBlocks.duo instanceof Turret){
            ((Turret)TurretBlocks.duo).shootSound = shootDuo;
        }
        if(TurretBlocks.lancer instanceof Turret){
            ((Turret)TurretBlocks.lancer).shootSound = shootLancer;
        }
        if(TurretBlocks.arc instanceof Turret){
            ((Turret)TurretBlocks.arc).shootSound = shootArc;
        }

        if(Weapons.healBlaster != null) Weapons.healBlaster.shootSound = shootLaser;
        if(Weapons.laserBurster != null) Weapons.laserBurster.shootSound = shootLancer;
        if(Weapons.healBlasterDrone != null) Weapons.healBlasterDrone.shootSound = shootLaser;
        if(Weapons.healBlasterDrone2 != null) Weapons.healBlasterDrone2.shootSound = shootLaser;
        if(Weapons.healBlasterDrone3 != null) Weapons.healBlasterDrone3.shootSound = shootLaser;

        if(Vars.content.getByName(io.anuke.mindustry.type.ContentType.block, "conveyor") instanceof Conveyor){
            ((Conveyor)Vars.content.getByName(io.anuke.mindustry.type.ContentType.block, "conveyor")).ambientSound = loopConveyor;
        }
    }
}
