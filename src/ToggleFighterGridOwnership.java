/**
 * Copyright (c) 2012, Matthew X. Economou <xenophon@irtnog.org>
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
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
public class ToggleFighterGridOwnership extends UserDefinedScript {
	private Parameter bingoFuel;
	private Planet planet;
	private int returnToSector;
	private Parameter toCorporateP;

	@Override
	public String getName() {
		return "Toggle Fighter Grid Ownership";
	}

	@Override
	public boolean initScript() throws Exception {
		// Start from within the citadel of the planet you want to
		// use for transportation.
		if (!atPrompt(Swath.CITADEL_PROMPT)) {
			return false; // TODO throw exception instead
		}

		// Remember which planet we're using and where we started.
		planet = Swath.ship.onPlanet();
		returnToSector = Swath.ship.sector();

		// The planet must have a transwarp drive.
		if (planet.level() < 4) {
			return false; // TODO throw exception instead
		}

		// Ask for the minimum fuel level, which when reached will
		// stop the update process and return the planet to the
		// starting sector.
		bingoFuel = new Parameter("Minimum fuel level");
		bingoFuel.setType(Parameter.INTEGER);
		bingoFuel.setInteger(100000);

		// Ask if we're switching to corporate ownership.
		toCorporateP = new Parameter(
				"Enter 1 if switching to corporate ownership");
		toCorporateP.setType(Parameter.INTEGER);
		toCorporateP.setInteger(0);

		registerParam(bingoFuel);
		registerParam(toCorporateP);

		return true;
	}

	@Override
	public boolean runScript() throws Exception {
		int owner = Swath.PERSONAL;
		if (toCorporateP.getInteger() == 1) {
			owner = Swath.CORPORATE;
		}

		while (true) {
			// Always start from within the citadel.
			if (!atPrompt(Swath.CITADEL_PROMPT)) {
				return false; // TODO throw exception instead
			}

			// Check current fuel levels; return to the starting
			// sector if low.
			int currentFuel = planet.productAmounts()[Swath.FUEL_ORE];
			if (currentFuel <= bingoFuel.getInteger()) {
				PlanetWarp.exec(returnToSector);
				return true;
			}

			// Find all personal/corporate fighter deployments.
			Tools.SectorSearchParameters nearbyFighters;
			nearbyFighters = new Tools.SectorSearchParameters();
			nearbyFighters.setFighterAmount(true, 1);
			if (toCorporateP.getInteger() == 1) {
				nearbyFighters.setFighterOwner(Swath.you);
			} else {
				nearbyFighters.setFighterOwner(Swath.you.corporation());
			}
			int sectors[] = Tools.findSectors(nearbyFighters);
			if (sectors.length == 0) {
				return true;
			}

			// TODO sort the results nearest to farthest

			// Warp the planet to the first sector on the list.
			PlanetWarp.exec(sectors[0]);
			LeaveCitadel.exec();
			LiftOff.exec();

			// Switch the fighters here from personal to corporate.
			DropTakeFighters.exec(Swath.sector.fighters(), owner,
					Swath.sector.ftrType());

			// Return to base.
			Land.exec(planet);
			EnterCitadel.exec();
		}
	}
}
