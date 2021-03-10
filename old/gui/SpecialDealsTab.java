package thederpgamer.contracts.gui;

import thederpgamer.contracts.faction.Opinion;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIWindowInterface;
import org.schema.schine.input.InputState;

public class SpecialDealsTab extends GUIContentPane {

    private int width;
    private int height;
    private Opinion opinion;

    public SpecialDealsTab(InputState inputState, GUIWindowInterface guiWindowInterface, Opinion opinion) {
        super(inputState, guiWindowInterface, "SPECIAL DEALS");
        this.width = guiWindowInterface.getInnerWidth();
        this.height = guiWindowInterface.getInnerHeigth();
        this.opinion = opinion;
    }

    @Override
    public void onInit() {
        super.onInit();
        createTab();
    }

    private void createTab() {

    }
}
