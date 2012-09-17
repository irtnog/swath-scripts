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
			return false; // TODO throw exception instead
		}

		toCorporateP = new Parameter(
				"Enter 1 if switching to corporate ownership");
		toCorporateP.setType(Parameter.INTEGER); // TODO use boolean instead?
		toCorporateP.setInteger(0);

		registerParam(toCorporateP);

		return true;
	}

	@Override
	public boolean runScript() throws Exception {
		if (!atPrompt(Swath.COMMAND_PROMPT)) {
			return false; // TODO throw exception instead
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

		// Change the planets.
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
