package io.anuke.mindustry.game;

import arc.util.Bundles;
import arc.util.Strings;

public enum GameMode{
    waves,
	sandbox{{
        infiniteResources = true;
        disableWaveTimer = true;
    }},
    freebuild{{
        disableWaveTimer = true;
    }},
    noWaves{{
        disableWaves = true;
        hidden = true;
        enemyCheat = true;
    }},
    victory{{
        disableWaves = true;
        hidden = true;
        enemyCheat = false;
        showMission = false;
    }},
    pvp{{
        disableWaves = true;
        isPvp = true;
        enemyCoreBuildRadius = 600f;
        respawnTime = 60 * 10;
    }},
    customAttackMode{{
        disableWaveTimer = true;
        disableWaves = true;
        enemyCheat  = true;
    }},
    SiegeMode {{
        enemyCheat = true;
        disableWaveTimer = true;
    }}
    ;

    public boolean infiniteResources, disableWaveTimer, disableWaves, showMission = true, hidden, enemyCheat, isPvp;
    public float enemyCoreBuildRadius = 400f;
    public float respawnTime = 60 * 4;

    public String description(){
        return Bundles.get("mode." + name() + ".description");
    }

    @Override
    public String toString(){
        return Bundles.get("mode." + name() + ".name");
    }

}
