/**
 * 
 */

import com.swath.Parameter;
import com.swath.Planet;
import com.swath.Swath;
import com.swath.Tools;
import com.swath.UserDefinedScript;
import com.swath.cmd.DropTakeFighters;
import com.swath.cmd.EnterCitadel;
import com.swath.cmd.Land;
import com.swath.cmd.LeaveCitadel;
import com.swath.cmd.LiftOff;
import com.swath.cmd.PlanetWarp;

/**
 * @author xenophon
 * 
 */
public class FighterGridUpdaterPersonalToCorporate extends UserDefinedScript {
	private Parameter bingoFuel;
	private Planet planet;
	private int returnToSector;

	@Override
	public void endScript(boolean finished) {
		if (finished == true) {
			try {
				PlanetWarp.exec(returnToSector);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getName() {
		return "Fighter Grid Updater (Personal to Corporate)";
	}

	@Override
	public boolean initScript() throws Exception {
		// Start from within the citadel of the planet you want to
		// use for transportation.
		if (!atPrompt(Swath.CITADEL_PROMPT)) {
			return false;
		}

		planet = Swath.ship.onPlanet();
		returnToSector = Swath.ship.sector();

		// The planet must have a transwarp drive.
		if (planet.level() < 4) {
			return false;
		}

		// Ask for the minimum fuel level, which when reached will
		// stop the update process and return the planet to the
		// starting sector.
		bingoFuel = new Parameter("Minimum fuel level");
		bingoFuel.setType(Parameter.INTEGER);
		bingoFuel.setInteger(100000);

		registerParam(bingoFuel);

		return true;
	}

	@Override
	public boolean runScript() throws Exception {
		while (true) {
			// Always start from within the citadel.
			if (!atPrompt(Swath.CITADEL_PROMPT)) {
				return false;
			}

			// Check current fuel levels.
			int currentFuel = planet.productAmounts()[Swath.FUEL_ORE];
			if (currentFuel <= bingoFuel.getInteger()) {
				return false;
			}

			// Find all personal fighter deployment.
			Tools.SectorSearchParameters nearbyPersonalFighters;
			nearbyPersonalFighters = new Tools.SectorSearchParameters();
			nearbyPersonalFighters.setFighterAmount(true, 1);
			nearbyPersonalFighters.setFighterOwner(Swath.you);
			int sectors[] = Tools.findSectors(nearbyPersonalFighters);
			if (sectors.length == 0) {
				return true;
			}

			// TODO: Sort the results nearest to farthest.

			// Warp the planet to the first sector on the list.
			PlanetWarp.exec(sectors[0]);
			LeaveCitadel.exec();
			LiftOff.exec();

			// Switch the fighters here from personal to corporate.
			DropTakeFighters.exec(Swath.sector.fighters(), Swath.CORPORATE,
					Swath.sector.ftrType());

			// Return to base.
			Land.exec(planet);
			EnterCitadel.exec();
		}
	}
}
