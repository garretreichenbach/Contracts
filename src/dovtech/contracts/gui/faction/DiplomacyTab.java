package dovtech.contracts.gui.faction;

import api.common.GameClient;
import api.faction.StarFaction;
import api.mod.StarLoader;
import org.schema.schine.graphicsengine.forms.gui.GUIAncor;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIWindowInterface;
import org.schema.schine.input.InputState;

public class DiplomacyTab extends GUIContentPane {

    private int width;
    private int height;
    private FactionDiplomacyList factionDiplomacyList;
    private GUIAncor infoPanel;
    private GUIAncor opinionPanel;
    private FactionDiplomacyModifierList diplomacyModifierList;
    private StarFaction playerFaction;

    public DiplomacyTab(InputState inputState, GUIWindowInterface guiWindowInterface) {
        super(inputState, guiWindowInterface, "DIPLOMACY");
        this.width = guiWindowInterface.getInnerWidth();
        this.height = guiWindowInterface.getInnerHeigth();
        this.playerFaction = new StarFaction(StarLoader.getGameState().getFactionManager().getFaction(GameClient.getClientPlayerState().getFactionId()));
    }

    @Override
    public void onInit() {
        super.onInit();
        createTab();
    }

    private void createTab() {
        setTextBoxHeightLast(270);
        addNewTextBox(50);
        infoPanel = getContent(0, 0);
        addNewTextBox(0, 50);
        opinionPanel = getContent(0, 1);
        addDivider(290);

        factionDiplomacyList = new FactionDiplomacyList(getState(), this);
        factionDiplomacyList.onInit();
        getContent(1, 0).attach(factionDiplomacyList);
    }

    public void onSelectFaction(StarFaction faction) {
        createInfoPanel(faction);
        createOpinionPanel(faction);
    }


    private void createInfoPanel(final StarFaction faction) {
        GUIScrollablePanel scrollablePanel = new GUIScrollablePanel(10, 10, infoPanel, getState());

        GUITextOverlayTable infoBoxText = new GUITextOverlayTable(2, 2, getState());
        infoBoxText.autoHeight = true;
        infoBoxText.autoWrapOn = infoPanel;
        infoBoxText.setTextSimple(new Object() {
            @Override
            public String toString() {
                return faction.getName() + "\n" + faction.getInternalFaction().getDescription();
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
        diplomacyModifierList = new FactionDiplomacyModifierList(getState(), this, playerFaction, faction);
        diplomacyModifierList.onInit();
        scrollablePanel.setContent(diplomacyModifierList);
        scrollablePanel.onInit();
        opinionPanel.getChilds().clear();
        opinionPanel.attach(scrollablePanel);
    }
}
