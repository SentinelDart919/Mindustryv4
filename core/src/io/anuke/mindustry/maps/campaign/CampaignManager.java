package io.anuke.mindustry.maps.campaign;

import com.badlogic.gdx.utils.Array;
import io.anuke.mindustry.io.SaveIO;

public class CampaignManager{
    public Array<Campaign> campaigns = new Array<>();
    private static final SaveIO saveIO = new SaveIO();

    public Campaign getCampaign(int index){
        return campaigns.get(index);
    }

    public int getCampaignCount(){
        return campaigns.size;
    }

    public void loadCampaigns(){
        Array<Campaign> savedCampaigns = saveIO.loadCampaigns();
        campaigns.clear();

        if(savedCampaigns == null){
            savedCampaigns = new Array<>();
        }

        for(String campaignName : CampaignRegistry.all()){
            Campaign existing = null;
            for(Campaign saved : savedCampaigns){
                if(campaignName.equals(saved.name)){
                    existing = saved;
                    break;
                }
            }

            if(existing == null){
                existing = new Campaign(campaignName);
            }

            campaigns.add(existing);
        }
        save();
    }

    public void save(){
        saveIO.saveCampaigns(this.campaigns);
    }

    public void saveCampaigns(Array<Campaign> campaigns){
        saveIO.saveCampaigns(campaigns);
    }

    public Array<Campaign> getAllCampaigns(){
        return campaigns;
    }

    public Campaign ensureCampaign(String name){
        for(Campaign campaign : campaigns){
            if(name.equals(campaign.name)){
                return campaign;
            }
        }

        if(!CampaignRegistry.contains(name)){
            return null;
        }

        Campaign created = new Campaign(name);
        campaigns.add(created);
        return created;
    }
}
