package net.william.commandscheduler;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class CommandschedulerDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		// This mod shouldn't use data generation.
		// Placeholder kept in case future updates require tag or recipe generation.
	}
}
