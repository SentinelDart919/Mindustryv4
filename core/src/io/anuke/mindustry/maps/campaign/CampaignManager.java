package io.anuke.mindustry.maps.campaign;

import arc.struct.Seq;
import io.anuke.mindustry.io.SaveIO;

public class CampaignManager{
    public Seq<Campaign> campaigns = new Seq<>();
    private static final SaveIO saveIO = new SaveIO();

    public Campaign getCampaign(int index){
        return campaigns.get(index);
    }

    public int getCampaignCount(){
        return campaigns.size;
    }

    public void loadCampaigns(){
        Seq<Campaign> savedCampaigns = saveIO.loadCampaigns();
        campaigns.clear();

        if(savedCampaigns == null){
            savedCampaigns = new Seq<>();
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

    public void saveCampaigns(Seq<Campaign> campaigns){
        saveIO.saveCampaigns(campaigns);
    }

    public Seq<Campaign> getAllCampaigns(){
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
