import com.swath.Parameter;
import com.swath.Planet;
import com.swath.Swath;
import com.swath.UserDefinedScript;
import com.swath.cmd.ClaimPlanet;
import com.swath.cmd.DropTakeFighters;
import com.swath.cmd.DropTakeMines;
import com.swath.cmd.Land;
import com.swath.cmd.LiftOff;
import com.swath.cmd.ScanPlanets;

public class ToggleSectorAssetsOwnership extends UserDefinedScript {
	private Parameter toCorporateP;

	@Override
	public String getName() {
		return "Toggle Sector Assets Ownership";
	}

	@Override
	public boolean initScript() throws Exception {
		if (!atPrompt(Swath.COMMAND_PROMPT)) {
			return false;
		}

		toCorporateP = new Parameter(
				"Enter 1 if switching to corporate ownership");
		toCorporateP.setType(Parameter.INTEGER);
		toCorporateP.setInteger(0);

		registerParam(toCorporateP);

		return true;
	}

	@Override
	public boolean runScript() throws Exception {
		if (!atPrompt(Swath.COMMAND_PROMPT)) {
			return false;
		}

		// Reconfigure fighters and mines first.
		if (toCorporateP.getInteger() == 1) {
			DropTakeFighters.exec(Swath.sector.fighters(), Swath.CORPORATE,
					Swath.sector.ftrType());
			DropTakeMines.exec(DropTakeMines.ARMID, Swath.sector.armidMines(),
					Swath.CORPORATE);
			DropTakeMines.exec(DropTakeMines.LIMPET,
					Swath.sector.limpetMines(), Swath.CORPORATE);
		} else {
			DropTakeFighters.exec(Swath.sector.fighters(), Swath.PERSONAL,
					Swath.sector.ftrType());
			DropTakeMines.exec(DropTakeMines.ARMID, Swath.sector.armidMines(),
					Swath.PERSONAL);
			DropTakeMines.exec(DropTakeMines.LIMPET,
					Swath.sector.limpetMines(), Swath.PERSONAL);
		}

		// Change the planets and mines.
		Planet planets[] = ScanPlanets.exec();
		for (Planet planet : planets) {
			Land.exec(planet);
			if (toCorporateP.getInteger() == 1) {
				ClaimPlanet.exec(Swath.CORPORATE);
			} else {
				ClaimPlanet.exec(Swath.PERSONAL);
			}
			LiftOff.exec();
		}

		return true;
	}
}
