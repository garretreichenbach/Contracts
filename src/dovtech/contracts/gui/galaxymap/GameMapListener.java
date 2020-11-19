package dovtech.contracts.gui.galaxymap;

import api.entity.Fleet;
import api.listener.fastevents.GameMapDrawListener;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.contracts.target.CargoTarget;
import dovtech.contracts.util.ContractUtils;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.common.data.fleet.missions.machines.states.FleetState;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class GameMapListener implements GameMapDrawListener {

    @Override
    public void system_PreDraw(GameMapDrawer drawer, Vector3i system, boolean explored) {

    }

    @Override
    public void system_PostDraw(GameMapDrawer drawer, Vector3i system, boolean explored) {

    }

    @Override
    public void galaxy_PreDraw(GameMapDrawer drawer) {

    }

    @Override
    public void galaxy_PostDraw(GameMapDrawer drawer) {

    }

    @Override
    public void galaxy_DrawLines(GameMapDrawer drawer) {
        for(Contract contract : ContractUtils.tradeFleets.keySet()) {
            Fleet tradeFleet = new Fleet(Fleet.getServerFleetManager().getByFleetDbId(ContractUtils.tradeFleets.get(contract)));
            Vector4f color = null;
            FleetState fleetState = (FleetState) tradeFleet.getInternalFleet().getStateCurrent();
            if(tradeFleet.getInternalFleet().getCurrentMoveTarget() == null || fleetState.getType().equals(FleetState.FleetStateType.IDLING)) {
                color = new Vector4f(0.235f, 0.235f, 0.82f, 0.85f);
            } else {
                color = new Vector4f(0.83f, 0.237f, 0.91f, 0.85f);
            }
            CargoTarget target = (CargoTarget) contract.getTarget();
            Vector3f start = tradeFleet.getFlagshipSector().getCoordinates().toVector3f();
            Vector3f end = new Vector3f(target.getLocation()[0], target.getLocation()[1], target.getLocation()[2]);

            DrawUtils.drawFTLLine(start, end, color, color);
        }
    }

    @Override
    public void galaxy_DrawSprites(GameMapDrawer gameMapDrawer) {

    }

    @Override
    public void galaxy_DrawQuads(GameMapDrawer drawer) {
        DrawUtils.drawCube(new Vector3f(100, 100, 100), 100, 0.5F, new Vector3f(0F, 1F, 1F));
        DrawUtils.tintSystem(new Vector3i(2, 3, 4), 0.8F, new Vector3f(1, 0, 0));
    }
}
