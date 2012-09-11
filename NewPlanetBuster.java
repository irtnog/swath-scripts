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
			return false;
		}
		if (Swath.main.currSector() != Swath.main.stardock()) {
			return false;
		}

		// Initialize the RNG.
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
			// TODO: Stop if fighters or shields are dangerously low.

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

			// Pick a number, any number, 0-999.
			int tag = rng.nextInt(1000);
			String name = "I'm Spartacus! #" + tag;

			// Create a new planet and land on it.
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
