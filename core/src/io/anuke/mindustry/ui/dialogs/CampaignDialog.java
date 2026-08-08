package io.anuke.mindustry.ui.dialogs;

import com.badlogic.gdx.utils.Array;
import io.anuke.mindustry.maps.campaign.Campaign;
import io.anuke.mindustry.maps.campaign.CampaignManager;
import io.anuke.ucore.scene.ui.ScrollPane;
import io.anuke.ucore.scene.ui.layout.Table;

import static io.anuke.mindustry.Vars.ui;
import static io.anuke.mindustry.Vars.world;

public class CampaignDialog extends FloatingDialog {
    private final CampaignManager campaignManager;
    private final Array<Campaign> allCampaigns = new Array<>();

    public CampaignDialog() {
        super("$text.campaigns");
        this.campaignManager = new CampaignManager();
        addCloseButton();
        shown(this::rebuild);
    }

    private void rebuild() {
        content().clear();
        allCampaigns.clear();

        campaignManager.loadCampaigns();
        allCampaigns.addAll(campaignManager.getAllCampaigns());

        if(allCampaigns.size == 0){
            content().add("$text.campaign.none").disabled(true);
            return;
        }

        Table table = new Table();
        table.defaults().growX().height(64f).pad(4f);

        for(Campaign campaign : allCampaigns){
            table.addButton(campaign.name, () -> selectCampaign(campaign));
            table.row();
        }

        ScrollPane pane = new ScrollPane(table);
        pane.setFadeScrollBars(false);
        content().add(pane).grow();
    }

    private void selectCampaign(Campaign selectedCampaign) {
        hide();
        world.sectors.setActiveCampaign(selectedCampaign.name);
        ui.sectors.show();
    }
}
