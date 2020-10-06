package dovtech.contracts.gui.faction;

import api.common.GameClient;
import api.entity.StarPlayer;
import api.faction.StarFaction;
import api.mod.StarLoader;
import dovtech.contracts.faction.Opinion;
import dovtech.contracts.player.PlayerData;
import dovtech.contracts.util.DataUtils;
import org.hsqldb.lib.StringComparator;
import org.schema.common.util.CompareTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.graphicsengine.forms.gui.GUIAncor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public class FactionDiplomacyList extends ScrollableTableList<StarFaction> {

    private DiplomacyTab panel;

    public FactionDiplomacyList(InputState state, DiplomacyTab panel) {
        super(state, 100, 100, panel);
        ((GameClientState)state).getFactionManager().addObserver(this);
        this.panel = panel;
    }

    @Override
    public void initColumns() {
        try {
            new StringComparator();

            final StarPlayer player = new StarPlayer(GameClient.getClientPlayerState());
            final PlayerData playerData = DataUtils.getPlayerData(player.getName());

            this.addColumn("Name", 15.0F, new Comparator<StarFaction>() {
                public int compare(StarFaction o1, StarFaction o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            this.addColumn("Power", 10.0F, new Comparator<StarFaction>() {
                public int compare(StarFaction o1, StarFaction o2) {
                    return CompareTools.compare(o1.getInternalFaction().factionPoints, o2.getInternalFaction().factionPoints);
                }
            });

            this.addColumn("Members", 10.0F, new Comparator<StarFaction>() {
                public int compare(StarFaction o1, StarFaction o2) {
                    return CompareTools.compare(o1.getMembers().size(), o2.getMembers().size());
                }
            });

            this.addColumn("Opinion", 15.0F, new Comparator<StarFaction>() {
                public int compare(StarFaction o1, StarFaction o2) {
                    return CompareTools.compare(playerData.getOpinion(o1).getOpinionScore(), playerData.getOpinion(o2).getOpinionScore());
                }
            });

            this.addTextFilter(new GUIListFilterText<StarFaction>() {
                public boolean isOk(String s, StarFaction faction) {
                    return faction.getName().toLowerCase().contains(s.toLowerCase());
                }
            }, ControllerElement.FilterRowStyle.LEFT);

            this.addDropdownFilter(new GUIListFilterDropdown<StarFaction, Opinion>(Opinion.values()) {
                public boolean isOk(Opinion opinion, StarFaction faction) {
                    switch (opinion) {
                        case ALL:
                            return true;
                        case TRUSTED:
                            return playerData.getOpinion(faction).getOpinion().equals(Opinion.TRUSTED);
                        case EXCELLENT:
                            return playerData.getOpinion(faction).getOpinion().equals(Opinion.EXCELLENT);
                        case GOOD:
                            return playerData.getOpinion(faction).getOpinion().equals(Opinion.GOOD);
                        case CORDIAL:
                            return playerData.getOpinion(faction).getOpinion().equals(Opinion.CORDIAL);
                        case NEUTRAL:
                            return playerData.getOpinion(faction).getOpinion().equals(Opinion.NEUTRAL);
                        case COOL:
                            return playerData.getOpinion(faction).getOpinion().equals(Opinion.COOL);
                        case POOR:
                            return playerData.getOpinion(faction).getOpinion().equals(Opinion.POOR);
                        case HOSTILE:
                            return playerData.getOpinion(faction).getOpinion().equals(Opinion.HOSTILE);
                        case HATED:
                            return playerData.getOpinion(faction).getOpinion().equals(Opinion.HATED);
                    }
                    return true;
                }

            }, new CreateGUIElementInterface<Opinion>() {
                @Override
                public GUIElement create(Opinion opinion) {
                    GUIAncor anchor = new GUIAncor(getState(), 10.0F, 24.0F);
                    GUITextOverlayTableDropDown dropDown;
                    (dropDown = new GUITextOverlayTableDropDown(10, 10, getState())).setTextSimple(opinion.display);
                    dropDown.setPos(4.0F, 4.0F, 0.0F);
                    anchor.setUserPointer(opinion);
                    anchor.attach(dropDown);
                    return anchor;
                }

                @Override
                public GUIElement createNeutral() {
                    return null;
                }
            }, ControllerElement.FilterRowStyle.RIGHT);

            this.activeSortColumnIndex = 0;
        } catch (PlayerNotFountException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Collection<StarFaction> getElementList() {
        ArrayList<StarFaction> factions = new ArrayList<>();
        for(Faction faction : StarLoader.getGameState().getFactionManager().getFactionCollection()) factions.add(new StarFaction(faction));
        return factions;
    }

    @Override
    public void updateListEntries(GUIElementList mainList, Set<StarFaction> set) {
        mainList.deleteObservers();
        mainList.addObserver(this);
        try {
            StarPlayer player = new StarPlayer(GameClient.getClientPlayerState());
            PlayerData playerData = DataUtils.getPlayerData(player.getName());
            if(set.size() != playerData.getOpinions().length) DataUtils.genOpinions(playerData);
            for (final StarFaction faction : set) {

                GUITextOverlayTable nameTextElement;
                (nameTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(faction.getName());
                GUIClippedRow nameRowElement;
                (nameRowElement = new GUIClippedRow(this.getState())).attach(nameTextElement);

                GUITextOverlayTable powerTextElement;
                (powerTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(faction.getInternalFaction().factionPoints);
                GUIClippedRow powerRowElement;
                (powerRowElement = new GUIClippedRow(this.getState())).attach(powerTextElement);

                GUITextOverlayTable membersTextElement;
                (membersTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(faction.getMembers().size());
                GUIClippedRow membersRowElement;
                (membersRowElement = new GUIClippedRow(this.getState())).attach(membersTextElement);

                GUITextOverlayTable opinionTextElement;
                (opinionTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(playerData.getOpinion(faction).getOpinion().display + " [" + playerData.getOpinion(faction).getOpinionScore() + "]");
                GUIClippedRow opinionRowElement;
                (opinionRowElement = new GUIClippedRow(this.getState())).attach(opinionTextElement);

                FactionDiplomacyListRow factionDiplomacyListRow = new FactionDiplomacyListRow(getState(), faction, nameRowElement, powerRowElement, membersRowElement, opinionRowElement);
                factionDiplomacyListRow.onInit();
                mainList.add(factionDiplomacyListRow);
            }
            mainList.updateDim();
        } catch (PlayerNotFountException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        ((GameClientState)getState()).getFactionManager().deleteObserver(this);
    }

    public class FactionDiplomacyListRow extends ScrollableTableList<StarFaction>.Row {

        public FactionDiplomacyListRow(InputState inputState, StarFaction faction, GUIElement... guiElements) {
            super(inputState, faction, guiElements);
            this.highlightSelect = true;
            this.highlightSelectSimple = true;
            this.setAllwaysOneSelected(true);
        }

        @Override
        public void clickedOnRow() {
            panel.onSelectFaction(f);
            super.clickedOnRow();
        }
    }
}
