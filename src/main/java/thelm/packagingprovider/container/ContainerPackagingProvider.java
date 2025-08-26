package thelm.packagingprovider.container;

import net.minecraft.entity.player.InventoryPlayer;
import thelm.packagedauto.container.ContainerTileBase;
import thelm.packagedauto.slot.SlotSingleStack;
import thelm.packagingprovider.tile.TilePackagingProvider;

public class ContainerPackagingProvider extends ContainerTileBase<TilePackagingProvider> {

	public ContainerPackagingProvider(InventoryPlayer playerInventory, TilePackagingProvider tile) {
		super(playerInventory, tile);
		addSlotToContainer(new SlotSingleStack(inventory, 0, 26, 35));
		setupPlayerInventory();
	}
}
