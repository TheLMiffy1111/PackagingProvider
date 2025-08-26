package thelm.packagingprovider.event;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import thelm.packagingprovider.block.BlockPackagingProvider;
import thelm.packagingprovider.network.PacketHandler;
import thelm.packagingprovider.tile.TilePackagingProvider;

public class CommonEventHandler {

	public void registerBlock(Block block) {
		ForgeRegistries.BLOCKS.register(block);
	}

	public void registerItem(Item item) {
		ForgeRegistries.ITEMS.register(item);
	}

	public void onPreInit(FMLPreInitializationEvent event) {
		registerBlocks();
		registerItems();
		registerTileEntities();
		registerNetwork();
	}

	protected void registerBlocks() {
		registerBlock(BlockPackagingProvider.INSTANCE);
	}

	protected void registerItems() {
		registerItem(BlockPackagingProvider.ITEM_INSTANCE);
	}

	protected void registerTileEntities() {
		GameRegistry.registerTileEntity(TilePackagingProvider.class, new ResourceLocation("packagingprovider:packaging_provider"));
	}

	protected void registerNetwork() {
		PacketHandler.registerPackets();
	}
}
