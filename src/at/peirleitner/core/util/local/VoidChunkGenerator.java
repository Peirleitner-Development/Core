package at.peirleitner.core.util.local;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

/**
 * Used to create void worlds with {@link LocalUtils}
 * @since 1.0.10
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class VoidChunkGenerator extends ChunkGenerator {

	@SuppressWarnings("deprecation")
	@Override
	public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
		ChunkData chunk = createChunkData(world);
		setBiome(biome);
		return chunk;
	}

	@SuppressWarnings("deprecation")
	private void setBiome(BiomeGrid biomeGrid) {
		Biome biome = Biome.PLAINS;
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				biomeGrid.setBiome(x, 0, z, biome);
			}
		}
	}

	@Override
	public boolean canSpawn(World world, int x, int z) {
		return true;
	}
}