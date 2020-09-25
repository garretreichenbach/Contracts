package dovtech.contracts.gui.contracts;

import api.faction.StarFaction;
import api.utils.gui.SimpleGUIHorizontalButtonPane;
import dovtech.contracts.contracts.target.*;
import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIBigInputPanel;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;

public class NewContractPanel extends PlayerInput {

    private GUIBigInputPanel inputPanel;
    private StarFaction contractor;
    private ContractTargetSelectionPanel targetSelectionPanel;
    public ContractTarget target;

    public NewContractPanel(GameClientState state, StarFaction contractor) {
        super(state);
        this.contractor = contractor;
        this.inputPanel = createInputPanel();
        this.inputPanel.setCallback(this);
        this.target = null;
        updatePanel();
    }

    @Override
    public void onDeactivate() {

    }

    @Override
    public void handleMouseEvent(MouseEvent mouseEvent) {

    }

    @Override
    public GUIElement getInputPanel() {
        return inputPanel;
    }

    public StarFaction getContractor() {
        return contractor;
    }

    public void updatePanel() {
        if(target != null) {
            this.targetSelectionPanel = new ContractTargetSelectionPanel(getState(), target);
            if(target instanceof PlayerTarget) {
                //Todo:
            }
        }
    }

    public GUIBigInputPanel createInputPanel() {
        GUIBigInputPanel panel = new GUIBigInputPanel(getState(), this, "Add Contract", "");
        panel.onInit();
        panel.setPos(470.0F, 35.0F, 0.0F);
        SimpleGUIHorizontalButtonPane buttonPane = new SimpleGUIHorizontalButtonPane(getState(), 100, 32);

        GUITextButton cargoButton = new GUITextButton(getState(), 100, 24, GUITextButton.ColorPalette.OK, "CARGO", new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    target = new CargoTarget();
                    updatePanel();
                }
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });
        buttonPane.addButton(cargoButton);

        GUITextButton miningButton = new GUITextButton(getState(), 100, 24, GUITextButton.ColorPalette.OK, "MINING", new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    target = new MiningTarget();
                    updatePanel();
                }
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });
        buttonPane.addButton(miningButton);

        GUITextButton bountyButton = new GUITextButton(getState(), 100, 24, GUITextButton.ColorPalette.OK, "BOUNTY", new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    target = new PlayerTarget();
                    updatePanel();
                }
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });
        buttonPane.addButton(bountyButton);

        GUITextButton productionButton = new GUITextButton(getState(), 100, 24, GUITextButton.ColorPalette.OK, "PRODUCTION", new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    target = new ProductionTarget();
                    updatePanel();
                }
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });
        buttonPane.addButton(productionButton);

        buttonPane.setPos(0, 300, 0);
        panel.attach(buttonPane);

        GUITextOverlay targetLabel = new GUITextOverlay((int) buttonPane.getWidth(), 10, getState());
        targetLabel.setTextSimple("Target Types");
        targetLabel.setPos(40, buttonPane.getPos().y + 12, 0);
        panel.attach(targetLabel);
        return panel;
    }
}
