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

import java.util.Random;

import com.swath.Parameter;
import com.swath.Planet;
import com.swath.Swath;
import com.swath.UserDefinedScript;
import com.swath.cmd.BlowUpPlanet;
import com.swath.cmd.BuyItems;
import com.swath.cmd.Land;
import com.swath.cmd.LandOnStarDock;
import com.swath.cmd.LaunchGenesisTorpedo;
import com.swath.cmd.LeaveStarDock;
import com.swath.cmd.ScanPlanets;

public class NewPlanetBuster extends UserDefinedScript {
	private Parameter maxAtomicDetonators;
	private Parameter maxGenesisTorpedoes;
	private Random rng;
	private Parameter targetExperienceLevel;

	@Override
	public String getName() {
		return "New Planet Buster";
	}

	@Override
	public boolean initScript() throws Exception {
		// Start from the command prompt in the Star Dock's sector.
		if (!atPrompt(Swath.COMMAND_PROMPT)) {
			return false; // TODO: throw exception instead
		}
		if (Swath.main.currSector() != Swath.main.stardock()) {
			return false; // TODO: throw exception instead
		}

		// Initialize the random number generator (RNG).
		rng = new Random();

		targetExperienceLevel = new Parameter("Target experience level");
		targetExperienceLevel.setType(Parameter.INTEGER);

		maxGenesisTorpedoes = new Parameter(
				"Max. Genesis torpedoes this ship carries");
		maxGenesisTorpedoes.setType(Parameter.INTEGER);
		maxGenesisTorpedoes.setInteger(Swath.ship.shipCategory()
				.maxGenesisTorpedos());

		maxAtomicDetonators = new Parameter(
				"Max. atomic detonators this ship carries (not always the same!)");
		maxAtomicDetonators.setType(Parameter.INTEGER);
		maxAtomicDetonators.setInteger(Swath.ship.shipCategory()
				.maxGenesisTorpedos());

		registerParam(targetExperienceLevel);
		registerParam(maxGenesisTorpedoes);
		registerParam(maxAtomicDetonators);

		return true;
	}

	@Override
	public boolean runScript() throws Exception {
		while (Swath.you.experience() < targetExperienceLevel.getInteger()) {
			// NOTE: Because we're planet busting at Star Dock, we
			// don't need to worry about navigation hazards.

			// Re-supply.
			if ((Swath.ship.genesisTorpedos() == 0)
					|| (Swath.ship.atomicDevices() == 0)) {
				LandOnStarDock.exec();
				BuyItems.exec(
						BuyItems.ATOMIC_DETONATORS,
						maxAtomicDetonators.getInteger()
								- Swath.ship.atomicDevices());
				BuyItems.exec(
						BuyItems.GENESIS_TORPEDOES,
						maxGenesisTorpedoes.getInteger()
								- Swath.ship.genesisTorpedos());
				LeaveStarDock.exec();
			}

			// Pick a number, any number, 0-999. That way more than
			// one person can be running this script, and the chance
			// they'll blow up the wrong planet (or run into some
			// other conflict) is reduced.
			int tag = rng.nextInt(1000);
			String name = "I'm Spartacus! #" + tag;

			// Create a new planet, land on it, and blow it up. The
			// scan is necessary prior to landing because the Genesis
			// torpedo sequence doesn't output a planet number.
			LaunchGenesisTorpedo.exec(name, Swath.PERSONAL);
			Planet planets[] = ScanPlanets.exec();
			for (Planet planet : planets) {
				if (planet.name().equalsIgnoreCase(name)) {
					Land.exec(planet);
					BlowUpPlanet.exec();
				}
			}
		}
		return true;
	}
}
