package thelm.packagingprovider.networking;

import appeng.api.networking.IGrid;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import thelm.packagedauto.integration.appeng.networking.HostHelperTile;
import thelm.packagingprovider.tile.TilePackagingProvider;

public class HostHelperTilePackagingProvider extends HostHelperTile<TilePackagingProvider> {

	public HostHelperTilePackagingProvider(TilePackagingProvider tile) {
		super(tile);
	}

	public void postPatternChange() {
		if(isActive()) {
			IGrid grid = getNode().getGrid();
			grid.postEvent(new MENetworkCraftingPatternChange(tile, getNode()));
		}
	}
}
