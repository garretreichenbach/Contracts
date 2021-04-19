package thederpgamer.contracts.gui.contract.playercontractlist;

import api.common.GameClient;
import api.utils.gui.GUIMenuPanel;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.input.InputState;

/**
 * PlayerContractsPanel.java
 * GUI panel for the player's contract list as well as their history and records.
 *
 * @author TheDerpGamer
 * @since 03/17/2021
 */
public class PlayerContractsPanel extends GUIMenuPanel {

    public PlayerContractsPanel(InputState inputState) {
        super(inputState, "PlayerContractsPanel", 800, 500);
    }

    @Override
    public void recreateTabs() {
        guiWindow.clearTabs();

        //Contracts Pane
        GUIContentPane contractsPane = guiWindow.addTab("CONTRACTS");
        contractsPane.setTextBoxHeightLast(300);

        PlayerContractsScrollableList playerContractsList = new PlayerContractsScrollableList(GameClient.getClientState(), 739, 300, contractsPane.getContent(0));
        playerContractsList.onInit();
        contractsPane.getContent(0).attach(playerContractsList);


        //Todo: Player Contract History
        //History Pane
        //GUIContentPane historyPane = guiWindow.addTab("HISTORY");
        //historyPane.setTextBoxHeightLast(300);

        //PlayerContractHistoryList playerContractHistoryList = new PlayerContractHistoryList(GameClient.getClientState(), 739, 300, historyPane.getContent(0));
        //playerContractHistoryList.onInit();
        //historyPane.getContent(0).attach(playerContractHistoryList);
    }
}