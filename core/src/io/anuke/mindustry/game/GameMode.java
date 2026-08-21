package io.anuke.mindustry.game;

import io.anuke.ucore.util.Bundles;

public enum GameMode{
    waves{
        @Override
        void config(){
            super.config();
        }
    },
    sandbox{
        @Override
        void config(){
            super.config();
            infiniteResources = true;
            disableWaveTimer = true;
        }
    },
    freebuild{
        @Override
        void config(){
            super.config();
            disableWaveTimer = true;
        }
    },
    noWaves{
        @Override
        void config(){
            super.config();
            disableWaves = true;
            hidden = true;
            enemyCheat = true;
        }
    },
    victory{
        @Override
        void config(){
            super.config();
            disableWaves = true;
            hidden = true;
            enemyCheat = false;
            showMission = false;
        }
    },
    pvp{
        @Override
        void config(){
            super.config();
            disableWaves = true;
            isPvp = true;
            enemyCoreBuildRadius = 600f;
            respawnTime = 60 * 10;
        }
    },
    customAttackMode{
        @Override
        void config(){
            super.config();
            disableWaveTimer = true;
            disableWaves = true;
            enemyCheat = true;
        }
    },
    SiegeMode{
        @Override
        void config(){
            super.config();
            enemyCheat = true;
            disableWaveTimer = true;
        }
    };

    static{
        //apply the canonical configuration to every mode at class load,
        //so default values (e.g. hidden, infiniteResources) are correct before any reset() is called
        for(GameMode mode : values()){
            mode.config();
        }
    }

    public boolean infiniteResources, disableWaveTimer, disableWaves, showMission = true, hidden, enemyCheat, isPvp;
    public float enemyCoreBuildRadius = 400f;
    public float respawnTime = 60 * 4;

    /**Sets this mode's fields to their canonical configuration. Called by {@link #reset()}.*/
    void config(){
        infiniteResources = false;
        disableWaveTimer = false;
        disableWaves = false;
        showMission = true;
        hidden = false;
        enemyCheat = false;
        isPvp = false;
        enemyCoreBuildRadius = 400f;
        respawnTime = 60 * 4;
    }

    /**Restores this mode's canonical configuration, discarding any runtime modifications made through the custom game dialog.
     * This prevents custom game settings from leaking into other maps, sectors or saved games.*/
    public void reset(){
        config();
    }

    public String description(){
        return Bundles.get("mode." + name() + ".description");
    }

    @Override
    public String toString(){
        return Bundles.get("mode." + name() + ".name");
    }

}
