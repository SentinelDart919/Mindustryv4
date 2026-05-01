package io.anuke.mindustry.maps.campaign;

import com.badlogic.gdx.utils.Array;
import io.anuke.mindustry.maps.Sector;

public class Campaign {
    public String name;
    private final Array<Sector> sectors;
    private int completedSectors;

    public Campaign(String name) {
        this.name = name;
        this.sectors = new Array<>();
    }

    public void addSector(Sector sector) {
        sectors.add(sector);
    }

    public Sector getSector(int index) {
        return sectors.get(index);
    }

    public int getSectorCount() {
        return sectors.size;
    }

    public void completeSector(Sector sector) {
        completedSectors++;
    }

    public boolean isComplete() {
        return completedSectors == sectors.size;
    }

    public int getCompletedSectors() {
        return completedSectors;
    }

    public void setCompletedSectors(int completedSectors) {
        this.completedSectors = completedSectors;
    }

    public Array<Sector> getSectors() {
        return sectors;
    }
}
