package thederpgamer.contracts.gui.contract.playercontractlist;

import api.utils.gui.GUIControlManager;
import api.utils.gui.GUIMenuPanel;
import org.schema.game.client.data.GameClientState;

/**
 * PlayerContractsControlManager.java
 * <Description>
 *
 * @author TheDerpGamer
 * @since 03/17/2021
 */
public class PlayerContractsControlManager extends GUIControlManager {

    public PlayerContractsControlManager(GameClientState clientState) {
        super(clientState);
    }

    @Override
    public GUIMenuPanel createMenuPanel() {
        return new PlayerContractsPanel(getState());
    }
}
