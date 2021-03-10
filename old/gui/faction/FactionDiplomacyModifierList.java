package thederpgamer.contracts.gui.faction;

import api.common.GameClient;
import api.entity.StarPlayer;
import api.faction.StarFaction;
import thederpgamer.contracts.gui.faction.diplomacy.FactionDiplomacyModifier;
import thederpgamer.contracts.player.PlayerData;
import thederpgamer.contracts.util.DataUtils;
import thederpgamer.contracts.util.FactionUtils;
import org.hsqldb.lib.StringComparator;
import org.schema.common.util.CompareTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public class FactionDiplomacyModifierList extends ScrollableTableList<FactionDiplomacyModifier> {

    private StarFaction toFaction;
    private StarFaction fromFaction;

    public FactionDiplomacyModifierList(InputState state, GUIElement panel, StarFaction toFaction, StarFaction fromFaction) {
        super(state, 100, 100, panel);
        ((GameClientState)state).getFactionManager().addObserver(this);
        this.toFaction = toFaction;
        this.fromFaction = fromFaction;
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        ((GameClientState)getState()).getFactionManager().deleteObserver(this);
    }

    @Override
    public void initColumns() {
        new StringComparator();

        addColumn(Lng.str("Modifier"), 12.0f, new Comparator<FactionDiplomacyModifier>() {
            @Override
            public int compare(FactionDiplomacyModifier o1, FactionDiplomacyModifier o2) {
                return (o1.getDisplay()).compareTo(o2.getDisplay());
            }
        });

        addColumn(Lng.str("Value"), 7.0f, new Comparator<FactionDiplomacyModifier>() {
            @Override
            public int compare(FactionDiplomacyModifier o1, FactionDiplomacyModifier o2) {
                return CompareTools.compare(o1.getModifier(), o2.getModifier());
            }
        });

        addColumn(Lng.str("Date"), 7.0f, new Comparator<FactionDiplomacyModifier>() {
            @Override
            public int compare(FactionDiplomacyModifier o1, FactionDiplomacyModifier o2) {
                return CompareTools.compare(o1.getModifier(), o2.getModifier());
            }
        });

        this.addTextFilter(new GUIListFilterText<FactionDiplomacyModifier>() {
            public boolean isOk(String s, FactionDiplomacyModifier modifier) {
                return modifier.getDisplay().toLowerCase().contains(s.toLowerCase());
            }
        }, ControllerElement.FilterRowStyle.FULL);

        activeSortColumnIndex = 0;
    }

    @Override
    protected Collection<FactionDiplomacyModifier> getElementList() {
        return FactionUtils.getFactionData(fromFaction).getModifiers(toFaction);
    }

    @Override
    public void updateListEntries(GUIElementList mainList, Set<FactionDiplomacyModifier> set) {
        mainList.deleteObservers();
        mainList.addObserver(this);
        try {
            StarPlayer player = new StarPlayer(GameClient.getClientPlayerState());
            PlayerData playerData = DataUtils.getPlayerData(player.getName());
            if(set.size() != playerData.getOpinions().length) DataUtils.genOpinions(playerData);
            for (final FactionDiplomacyModifier modifier : set) {

                GUITextOverlayTable displayTextElement;
                (displayTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(modifier.getDisplay());
                GUIClippedRow displayRowElement;
                (displayRowElement = new GUIClippedRow(this.getState())).attach(displayTextElement);

                GUITextOverlayTable valueTextElement;
                (valueTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(modifier.getModifier() + " (" + modifier.getChangePerDay() + " per day)");
                GUIClippedRow valueRowElement;
                (valueRowElement = new GUIClippedRow(this.getState())).attach(valueTextElement);

                GUITextOverlayTable dateTextElement;
                (dateTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(modifier.date);
                GUIClippedRow dateRowElement;
                (dateRowElement = new GUIClippedRow(this.getState())).attach(dateTextElement);

                FactionDiplomacyModifierListRow factionDiplomacyModifierListRow = new FactionDiplomacyModifierListRow(getState(), modifier, displayRowElement, valueRowElement, dateRowElement);
                factionDiplomacyModifierListRow.onInit();
                mainList.add(factionDiplomacyModifierListRow);
            }
            mainList.updateDim();
        } catch (PlayerNotFountException e) {
            e.printStackTrace();
        }
    }

    public class FactionDiplomacyModifierListRow extends ScrollableTableList<FactionDiplomacyModifier>.Row {

        public FactionDiplomacyModifierListRow(InputState inputState, FactionDiplomacyModifier modifier, GUIElement... guiElements) {
            super(inputState, modifier, guiElements);
            this.highlightSelect = true;
            this.highlightSelectSimple = true;
            this.setAllwaysOneSelected(true);
        }
    }
}
