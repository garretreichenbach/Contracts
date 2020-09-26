package dovtech.contracts.gui.contracts;

import api.entity.StarPlayer;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.util.DataUtil;
import org.hsqldb.lib.StringComparator;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import java.util.*;

public class ContractClaimantsScrollableList extends ScrollableTableList<StarPlayer> implements GUIActiveInterface {

    private Contract contract;

    public ContractClaimantsScrollableList(InputState state, float var2, float var3, GUIElement guiElement, Contract contract) {
        super(state, var2, var3, guiElement);
        this.contract = contract;
    }

    @Override
    public void initColumns() {
        new StringComparator();

        this.addColumn("Name", 20.0F, new Comparator<StarPlayer>() {
            public int compare(StarPlayer o1, StarPlayer o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        this.addColumn("Faction", 20.0F, new Comparator<StarPlayer>() {
            public int compare(StarPlayer o1, StarPlayer o2) {
                String f1Name = "NO FACTION";
                String f2Name = "NO FACTION";
                if(o1.getFaction() != null) f1Name = o1.getFaction().getName();
                if(o2.getFaction() != null) f2Name = o2.getFaction().getName();
                return f1Name.compareTo(f2Name);
            }
        });

        this.addTextFilter(new GUIListFilterText<StarPlayer>() {
            public boolean isOk(String s, StarPlayer player) {
                return player.getName().toLowerCase().contains(s.toLowerCase());
            }
        }, ControllerElement.FilterRowStyle.FULL);

        this.activeSortColumnIndex = 0;
    }

    @Override
    protected Collection<StarPlayer> getElementList() {
        this.contract.setClaimants(DataUtil.contracts.get(contract.getUid()).getClaimants());
        return this.contract.getClaimants();
    }

    @Override
    public void updateListEntries(GUIElementList guiElementList, Set<StarPlayer> set) {
        guiElementList.deleteObservers();
        guiElementList.addObserver(this);
        for(StarPlayer player : set) {
            GUITextOverlayTable nameTextElement;
            (nameTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(player.getName());
            GUIClippedRow nameRowElement;
            (nameRowElement = new GUIClippedRow(this.getState())).attach(nameTextElement);
            String factionName = "NO FACTION";
            if(player.getPlayerState().getFactionId() != 0) factionName = player.getFaction().getName();

            GUITextOverlayTable factionTextElement;
            (factionTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(factionName);
            GUIClippedRow factionRowElement;
            (factionRowElement = new GUIClippedRow(this.getState())).attach(factionTextElement);

            ClaimantListRow claimantListRow;
            (claimantListRow = new ClaimantListRow(this.getState(), player, nameRowElement, factionRowElement)).onInit();

            claimantListRow.onInit();
            guiElementList.add(claimantListRow);
        }
        guiElementList.updateDim();
    }

    public class ClaimantListRow extends ScrollableTableList<StarPlayer>.Row {

        public ClaimantListRow(InputState inputState, StarPlayer player, GUIElement... guiElements) {
            super(inputState, player, guiElements);
            this.highlightSelect = true;
            this.highlightSelectSimple = true;
            this.setAllwaysOneSelected(true);
        }

        @Override
        public void clickedOnRow() {
            super.clickedOnRow();
            clear();
            handleDirty();
        }
    }
}