package thederpgamer.contracts.gui.faction;

import api.common.GameClient;
import api.entity.StarPlayer;
import api.faction.StarFaction;
import api.mod.StarLoader;
import api.utils.gui.SimpleGUIHorizontalButtonPane;
import thederpgamer.contracts.faction.FactionData;
import thederpgamer.contracts.faction.FactionOpinion;
import thederpgamer.contracts.faction.Opinion;
import thederpgamer.contracts.gui.faction.diplomacy.FactionDiplomacyModifier;
import thederpgamer.contracts.player.PlayerData;
import thederpgamer.contracts.util.DataUtils;
import thederpgamer.contracts.util.FactionUtils;
import org.hsqldb.lib.StringComparator;
import org.schema.common.util.CompareTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public class FactionDiplomacyList extends ScrollableTableList<StarFaction> {

    private DiplomacyTab panel;
    private StarFaction playerFaction;

    public FactionDiplomacyList(InputState state, GUIAncor content, DiplomacyTab panel) {
        super(state, 100, 100, content);
        ((GameClientState) state).getFactionManager().addObserver(this);
        this.panel = panel;
        if(GameClient.getClientPlayerState().getFactionId() != 0) {
            playerFaction = new StarFaction(StarLoader.getGameState().getFactionManager().getFaction(GameClient.getClientPlayerState().getFactionId()));
        } else {
            playerFaction = null;
        }
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
                    FactionData o1Data = FactionUtils.getFactionData(o1);
                    FactionData o2Data = FactionUtils.getFactionData(o2);
                    return CompareTools.compare(o1Data.getFactionPower(), o2Data.getFactionPower());
                }
            });

            this.addColumn("Members", 10.0F, new Comparator<StarFaction>() {
                public int compare(StarFaction o1, StarFaction o2) {
                    return CompareTools.compare(o1.getMembers().size(), o2.getMembers().size());
                }
            });

            this.addColumn("Opinion", 15.0F, new Comparator<StarFaction>() {
                public int compare(StarFaction o1, StarFaction o2) {
                    if(playerFaction != null) {
                        int o1Opinion = FactionUtils.getOpinion(o1, playerFaction).getOpinionScore();
                        int o2Opinion = FactionUtils.getOpinion(o2, playerFaction).getOpinionScore();
                        return CompareTools.compare(o1Opinion, o2Opinion);
                    } else {
                        return 0;
                    }
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
        for (Faction faction : StarLoader.getGameState().getFactionManager().getFactionCollection()) {
            if (!faction.getName().toLowerCase().contains("fauna") && !faction.getName().toLowerCase().contains("guild")) {
                factions.add(new StarFaction(faction));
            }
        }
        return factions;
    }

    private SimpleGUIHorizontalButtonPane redrawButtonPane(StarFaction faction, int width, int height) {
        SimpleGUIHorizontalButtonPane buttonPane = new SimpleGUIHorizontalButtonPane(getState(), width, height, 2);
        StarFaction playerFaction = new StarFaction(StarLoader.getGameState().getFactionManager().getFaction(GameClient.getClientPlayerState().getFactionId()));
        int opinionScore = FactionUtils.getOpinionScore(faction, playerFaction);
        Opinion opinion = Opinion.getFromScore(opinionScore);
        ArrayList<FactionDiplomacyModifier> modifiers = FactionUtils.getOpinionModifiers(faction, playerFaction);

        //Todo
        final boolean hasNonAggressionPact = false;
        final boolean hasDefensivePact = false;

        boolean canRequestAlliance = false;
        boolean canInviteToFederation = false;
        boolean canRequestJoinFederation = false;
        boolean canDeclareIndependence = false;
        boolean canDeclareWar = false;
        boolean canSanction = false;

        if (opinionScore >= -15) {
            GUITextButton sendGiftButton = new GUITextButton(getState(), 130, 24, GUITextButton.ColorPalette.FRIENDLY, "SEND GIFT", new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if (mouseEvent.pressedLeftMouse()) {
                        getState().getController().queueUIAudio("0022_menu_ui - enter");
                        //Todo: Send gift
                    }
                }

                @Override
                public boolean isOccluded() {
                    return !isActive();
                }
            });
            buttonPane.addButton(sendGiftButton);

            String nonAggressionText = "REQUEST NON-AGGRESSION PACT";
            GUITextButton.ColorPalette nonAggressionColor = GUITextButton.ColorPalette.OK;
            if(hasNonAggressionPact) {
                nonAggressionText = "CANCEL NON-AGGRESSION PACT";
                nonAggressionColor = GUITextButton.ColorPalette.CANCEL;
            }

            GUITextButton nonAggressionPactButton = new GUITextButton(getState(), 130, 24, nonAggressionColor, nonAggressionText, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if (mouseEvent.pressedLeftMouse()) {
                        if (!hasNonAggressionPact) {
                            getState().getController().queueUIAudio("0022_menu_ui - enter");

                        } else {
                            getState().getController().queueUIAudio("0022_menu_ui - back");

                        }
                        //Todo: Non-Aggression pacts
                    }
                }

                @Override
                public boolean isOccluded() {
                    return !isActive();
                }
            });
            buttonPane.addButton(nonAggressionPactButton);

            String defensivePactText = "REQUEST DEFENSIVE PACT";
            GUITextButton.ColorPalette defensivePactColor = GUITextButton.ColorPalette.FRIENDLY;
            if (hasDefensivePact) {
                defensivePactText = "CANCEL DEFENSIVE PACT";
                nonAggressionColor = GUITextButton.ColorPalette.CANCEL;
            }

            GUITextButton defensivePactButton = new GUITextButton(getState(), 130, 24, defensivePactColor, defensivePactText, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if (mouseEvent.pressedLeftMouse()) {
                        if (!hasDefensivePact) {
                            getState().getController().queueUIAudio("0022_menu_ui - enter");

                        } else {
                            getState().getController().queueUIAudio("0022_menu_ui - back");

                        }
                        //Todo: Defensive pacts
                    }
                }

                @Override
                public boolean isOccluded() {
                    return !isActive();
                }
            });
            buttonPane.addButton(defensivePactButton);
        }


        return buttonPane;
    }

    @Override
    public void updateListEntries(GUIElementList mainList, Set<StarFaction> set) {
        mainList.deleteObservers();
        mainList.addObserver(this);
        StarPlayer player = new StarPlayer(GameClient.getClientPlayerState());
        //PlayerData playerData = DataUtils.getPlayerData(player.getName());
        //Use faction opinions instead of ones for individual players
            //if(set.size() != playerData.getOpinions().length) DataUtils.genOpinions(playerData);
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

                String opinionText = "NEUTRAL [0]";

                if(player.getPlayerState().getFactionId() != 0) {
                    FactionOpinion factionOpinion = FactionUtils.getOpinion(faction, player.getFaction());
                    opinionText = factionOpinion.toString();
                }

                GUITextOverlayTable opinionTextElement;
                (opinionTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(opinionText);
                GUIClippedRow opinionRowElement;
                (opinionRowElement = new GUIClippedRow(this.getState())).attach(opinionTextElement);

                FactionDiplomacyListRow factionDiplomacyListRow = new FactionDiplomacyListRow(getState(), faction, nameRowElement, powerRowElement, membersRowElement, opinionRowElement);
                if (GameClient.getClientPlayerState().getFactionId() != 0) { //Faction diplomacy should only be available to players in factions (duh)
                    factionDiplomacyListRow.expanded = new GUIElementList(getState());
                    SimpleGUIHorizontalButtonPane buttonPane = redrawButtonPane(faction, factionDiplomacyListRow.expanded.width, factionDiplomacyListRow.expanded.height);
                    buttonPane.setPos(factionDiplomacyListRow.expanded.getPos());
                    factionDiplomacyListRow.expanded.add(new GUIListElement(buttonPane, buttonPane, getState()));
                    factionDiplomacyListRow.expanded.attach(buttonPane);
                }
                factionDiplomacyListRow.onInit();
                mainList.add(factionDiplomacyListRow);
            }
            mainList.updateDim();
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        ((GameClientState) getState()).getFactionManager().deleteObserver(this);
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
