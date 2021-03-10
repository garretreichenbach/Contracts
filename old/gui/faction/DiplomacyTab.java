package thederpgamer.contracts.gui.faction;

import api.common.GameClient;
import api.faction.StarFaction;
import api.mod.StarLoader;
import thederpgamer.contracts.faction.FactionData;
import thederpgamer.contracts.faction.FactionOpinion;
import thederpgamer.contracts.util.FactionUtils;
import org.schema.schine.graphicsengine.forms.gui.GUIAncor;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIWindowInterface;
import org.schema.schine.input.InputState;

public class DiplomacyTab extends GUIContentPane {

    private FactionDiplomacyList factionDiplomacyList;
    private GUIAncor infoPanel;
    private GUIAncor opinionPanel;
    private FactionDiplomacyModifierList diplomacyModifierList;
    private StarFaction playerFaction;

    public DiplomacyTab(InputState inputState, GUIWindowInterface guiWindowInterface) {
        super(inputState, guiWindowInterface, "FACTIONS");
        this.playerFaction = new StarFaction(StarLoader.getGameState().getFactionManager().getFaction(GameClient.getClientPlayerState().getFactionId()));
    }

    @Override
    public void onInit() {
        super.onInit();
        createTab();
    }

    private void createTab() {
        setTextBoxHeightLast(270);
        infoPanel = getContent(0, 0);
        addNewTextBox(0, 50);
        opinionPanel = getContent(0, 1);
        addDivider(330);

        factionDiplomacyList = new FactionDiplomacyList(getState(), getContent(1, 0), this);
        factionDiplomacyList.onInit();
        factionDiplomacyList.setInside(true);
        getContent(1, 0).attach(factionDiplomacyList);
    }

    public void onSelectFaction(StarFaction faction) {
        infoPanel.getChilds().clear();
        infoPanel.cleanUp();
        opinionPanel.getChilds().clear();
        opinionPanel.cleanUp();

        createInfoPanel(faction);
        createOpinionPanel(faction);
    }


    private void createInfoPanel(final StarFaction faction) {
        final FactionData factionData = FactionUtils.getFactionData(faction);
        FactionOpinion opinion = new FactionOpinion(faction.getID(), 0);
        String fedName = "NONE";
        if (factionData.getFederation() != null) fedName = factionData.getFederation().getName();

        if (GameClient.getClientPlayerState().getFactionId() != 0) {
            StarFaction playerFaction = new StarFaction(StarLoader.getGameState().getFactionManager().getFaction(GameClient.getClientPlayerState().getFactionId()));
            opinion = FactionUtils.getOpinion(faction, playerFaction);
        }

        GUIScrollablePanel scrollablePanel = new GUIScrollablePanel(10, 10, infoPanel, getState());
        GUITextOverlayTable infoBoxText = new GUITextOverlayTable(2, 2, getState());
        infoBoxText.autoHeight = true;
        infoBoxText.autoWrapOn = infoPanel;
        final String opinionString = opinion.toString();
        final String federationString = fedName;
        infoBoxText.setTextSimple(new Object() {
            @Override
            public String toString() {
                return faction.getName() + "\n" + faction.getInternalFaction().getDescription() +
                        "\n" +
                        "\nMembers: " + faction.getMembers().size() +
                        "\nFederation: " + federationString +
                        "\nPower: " + factionData.getFactionPower() +
                        "\nOpinion: " + opinionString;
            }

        });
        infoBoxText.onInit();
        scrollablePanel.setContent(infoBoxText);
        scrollablePanel.onInit();
        infoPanel.getChilds().clear();
        infoPanel.attach(scrollablePanel);
    }

    private void createOpinionPanel(StarFaction faction) {
        GUIScrollablePanel scrollablePanel = new GUIScrollablePanel(10, 10, opinionPanel, getState());
        diplomacyModifierList = new FactionDiplomacyModifierList(getState(), scrollablePanel, playerFaction, faction);
        diplomacyModifierList.onInit();
        scrollablePanel.setContent(diplomacyModifierList);
        scrollablePanel.onInit();
        opinionPanel.getChilds().clear();
        opinionPanel.attach(scrollablePanel);
    }
}
