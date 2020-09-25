package dovtech.contracts.gui.contracts;

import api.DebugFile;
import dovtech.contracts.Contracts;
import dovtech.contracts.contracts.target.*;
import org.schema.schine.graphicsengine.forms.gui.GUIAncor;
import org.schema.schine.graphicsengine.forms.gui.GUITextInput;
import org.schema.schine.input.InputState;

public class ContractTargetSelectionPanel extends GUIAncor {

    public GUITextInput textInput;

    public ContractTargetSelectionPanel(InputState state, ContractTarget target) {
        super(state, 500, 250);
        if(target instanceof CargoTarget) {
            //Todo
        } else if(target instanceof MiningTarget) {
            //Todo
        } else if(target instanceof PlayerTarget) {
            textInput = new GUITextInput(300, 150, state);
            textInput.setTextBox(true);
            textInput.setPreText("Select Player Target");
            textInput.onInit();
            textInput.setPos(50, 100, 0);
            attach(textInput);
        } else if(target instanceof ProductionTarget) {
            //Todo
        } else {
            DebugFile.log("[ERROR]: Error occurred in updating target selection panel!", Contracts.getInstance());
        }
    }
}
