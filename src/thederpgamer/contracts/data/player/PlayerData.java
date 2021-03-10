package thederpgamer.contracts.data.player;

import api.common.GameCommon;
import org.schema.game.common.data.player.PlayerState;
import thederpgamer.contracts.data.contract.Contract;
import java.io.Serializable;
import java.util.ArrayList;

public class PlayerData implements Serializable {

    public String name;
    public ArrayList<Contract> contracts;
    public int factionID;

    public PlayerData(PlayerState playerState) {
        this.name = playerState.getName();
        this.contracts = new ArrayList<>();
        this.factionID = playerState.getFactionId();
    }

    public void sendMail(String from, String title, String contents) {
        GameCommon.getPlayerFromName(name).getClientChannel().getPlayerMessageController().serverSend(from, name, title, contents);
    }
}