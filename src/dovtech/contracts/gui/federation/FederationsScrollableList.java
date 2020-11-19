package dovtech.contracts.gui.federation;

import api.common.GameClient;
import api.faction.StarFaction;
import api.mod.StarLoader;
import api.utils.gui.SimpleGUIHorizontalButtonPane;
import dovtech.contracts.faction.FactionData;
import dovtech.contracts.federation.Federation;
import dovtech.contracts.util.FactionUtils;
import dovtech.contracts.util.FederationUtils;
import org.hsqldb.lib.StringComparator;
import org.schema.common.util.CompareTools;
import org.schema.schine.graphicsengine.forms.gui.GUIAncor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public class FederationsScrollableList extends ScrollableTableList<Federation> {

    private FederationsTab panel;
    private StarFaction playerFaction;
    private Federation playerFederation;

    public FederationsScrollableList(InputState inputState, GUIAncor content, FederationsTab panel) {
        super(inputState, 100, 100, content);
        this.panel = panel;
        this.playerFaction = new StarFaction(StarLoader.getGameState().getFactionManager().getFaction(GameClient.getClientPlayerState().getFactionId()));
        FactionData fData = FactionUtils.getFactionData(playerFaction);
        if(fData.getFederation() != null) {
            this.playerFederation = fData.getFederation();
        } else {
            this.playerFederation = null;
        }
    }

    @Override
    public void initColumns() {
        new StringComparator();

        this.addColumn("Name", 15.0F, new Comparator<Federation>() {
            public int compare(Federation o1, Federation o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        this.addColumn("Power", 10.0F, new Comparator<Federation>() {
            public int compare(Federation o1, Federation o2) {
                return CompareTools.compare(o1.getPower(), o2.getPower());
            }
        });

        this.addColumn("Members", 10.0F, new Comparator<Federation>() {
            public int compare(Federation o1, Federation o2) {
                return CompareTools.compare(o1.getMemberFactions().size(), o2.getMemberFactions().size());
            }
        });

        /* NOT IMPLEMENTED YET
        this.addColumn("Opinion", 15.0F, new Comparator<Federation>() {
            public int compare(Federation o1, Federation o2) {
                return CompareTools.compare(playerData.getOpinion(o1).getOpinionScore(), playerData.getOpinion(o2).getOpinionScore());
            }
        });
         */

        this.addTextFilter(new GUIListFilterText<Federation>() {
            public boolean isOk(String s, Federation federation) {
                return federation.getName().toLowerCase().contains(s.toLowerCase());
            }
        }, ControllerElement.FilterRowStyle.FULL);

        this.activeSortColumnIndex = 0;
    }

    @Override
    protected Collection<Federation> getElementList() {
        return FederationUtils.getAllFederations();
    }

    private SimpleGUIHorizontalButtonPane redrawButtonPane(Federation federation, int width, int height) {
        SimpleGUIHorizontalButtonPane buttonPane = new SimpleGUIHorizontalButtonPane(getState(), width, height, 2);

        //Todo

        return buttonPane;
    }

    @Override
    public void updateListEntries(GUIElementList guiElementList, Set<Federation> set) {
        guiElementList.deleteObservers();
        guiElementList.addObserver(this);

        for(Federation federation : set) {
            GUITextOverlayTable nameTextElement;
            (nameTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(federation.getName());
            GUIClippedRow nameRowElement;
            (nameRowElement = new GUIClippedRow(this.getState())).attach(nameTextElement);

            GUITextOverlayTable sizeTextElement;
            (sizeTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(federation.getMemberFactions().size());
            GUIClippedRow sizeRowElement;
            (sizeRowElement = new GUIClippedRow(this.getState())).attach(sizeTextElement);

            GUITextOverlayTable powerTextElement;
            (powerTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(federation.getPower());
            GUIClippedRow powerRowElement;
            (powerRowElement = new GUIClippedRow(this.getState())).attach(powerTextElement);

            /* NOT YET IMPLEMENTED
            GUITextOverlayTable opinionTextElement;
            (opinionTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(federation.getOpinion(playerFederation));
            GUIClippedRow opinionRowElement;
            (opinionRowElement = new GUIClippedRow(this.getState())).attach(opinionTextElement);
             */

            FederationListRow listRow = new FederationListRow(getState(), federation, nameRowElement, sizeRowElement, powerRowElement);
            if (playerFederation != null) {
                listRow.expanded = new GUIElementList(getState());
                SimpleGUIHorizontalButtonPane buttonPane = redrawButtonPane(federation, listRow.expanded.width, listRow.expanded.height);
                buttonPane.setPos(listRow.expanded.getPos());
                listRow.expanded.add(new GUIListElement(buttonPane, buttonPane, getState()));
                listRow.expanded.attach(buttonPane);
            }
            listRow.onInit();
            guiElementList.add(listRow);
        }

        guiElementList.updateDim();
    }

    public class FederationListRow extends ScrollableTableList<Federation>.Row {
        public FederationListRow(InputState inputState, Federation federation, GUIElement... guiElements) {
            super(inputState, federation, guiElements);
            this.highlightSelect = true;
            this.highlightSelectSimple = true;
            this.setAllwaysOneSelected(true);
        }

        @Override
        public void clickedOnRow() {
            panel.onSelectFederation(f);
            super.clickedOnRow();
        }
    }
}
