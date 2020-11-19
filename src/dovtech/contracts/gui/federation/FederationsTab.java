package dovtech.contracts.gui.federation;

import api.common.GameClient;
import api.faction.StarFaction;
import api.mod.StarLoader;
import dovtech.contracts.faction.FactionData;
import dovtech.contracts.federation.Federation;
import dovtech.contracts.util.FactionUtils;
import org.schema.schine.graphicsengine.forms.gui.GUIAncor;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIWindowInterface;
import org.schema.schine.input.InputState;

public class FederationsTab extends GUIContentPane {

    private StarFaction playerFaction;
    private Federation playerFederation;
    private boolean inFederation;

    private GUIAncor infoPanel;
    private GUIAncor actionPanel;
    private FederationsScrollableList federationsList;

    public FederationsTab(InputState inputState, GUIWindowInterface guiWindowInterface) {
        super(inputState, guiWindowInterface, "FEDERATIONS");
        this.playerFaction = new StarFaction(StarLoader.getGameState().getFactionManager().getFaction(GameClient.getClientPlayerState().getFactionId()));
        FactionData playerFactionData = FactionUtils.getFactionData(playerFaction);
        if (playerFactionData.getFederation() != null) {
            playerFederation = playerFactionData.getFederation();
            inFederation = true;
        } else {
            playerFederation = null;
            inFederation = false;
        }
    }

    @Override
    public void onInit() {
        super.onInit();
        createTab();
    }

    private void createTab() {
        setTextBoxHeightLast(270);
        infoPanel = getContent(0, 0);
        if (inFederation) {
            addNewTextBox(0, 50);
            actionPanel = getContent(0, 1);
            createActionPanel();
        }
        addDivider(330);

        federationsList = new FederationsScrollableList(getState(), getContent(1, 0), this);
        federationsList.onInit();
        federationsList.setInside(true);
        getContent(1, 0).attach(federationsList);
    }

    public void onSelectFederation(Federation federation) {
        infoPanel.getChilds().clear();
        infoPanel.cleanUp();
        createInfoPanel(federation);
    }

    private void createInfoPanel(final Federation federation) {
        GUIScrollablePanel scrollablePanel = new GUIScrollablePanel(10, 10, infoPanel, getState());
        GUITextOverlayTable infoBoxText = new GUITextOverlayTable(2, 2, getState());
        infoBoxText.autoHeight = true;
        infoBoxText.autoWrapOn = infoPanel;

        final String govString = federation.getGovernmentType().display;
        final StringBuilder memberBuilder = new StringBuilder();
        for(int f = 0; f < federation.getMemberFactions().size() - 1; f ++) {
            memberBuilder.append(federation.getMemberFactions().get(f).getName());
            memberBuilder.append(", ");
        }
        memberBuilder.append(federation.getMemberFactions().get(federation.getMemberFactions().size() - 1).getName());

        infoBoxText.setTextSimple(new Object() {
            @Override
            public String toString() {
                return federation.getName() + "\n" + federation.getDescription() +
                        "\n" +
                        "\nGovernment: " + govString +
                        "\nFactions: " + memberBuilder.toString() +
                        "\nPower: " + federation.getPower() +
                        "\nOpinion: [NOT YET IMPLEMENTED]";
            }

        });
        infoBoxText.onInit();
        scrollablePanel.setContent(infoBoxText);
        scrollablePanel.onInit();
        infoPanel.getChilds().clear();
        infoPanel.attach(scrollablePanel);
    }

    private void createActionPanel() {
        GUIScrollablePanel scrollablePanel = new GUIScrollablePanel(10, 10, actionPanel, getState());

        //scrollablePanel.setContent(actions);
        scrollablePanel.onInit();
        actionPanel.getChilds().clear();
        actionPanel.attach(scrollablePanel);
    }
}
