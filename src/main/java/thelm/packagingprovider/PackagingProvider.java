package thelm.packagingprovider;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagingprovider.block.BlockPackagingProvider;
import thelm.packagingprovider.event.CommonEventHandler;

@Mod(
		modid = PackagingProvider.MOD_ID,
		name = PackagingProvider.NAME,
		version = PackagingProvider.VERSION,
		dependencies = PackagingProvider.DEPENDENCIES
		)
public class PackagingProvider {

	public static final String MOD_ID = "packagingprovider";
	public static final String NAME = "PackagingProvider";
	public static final String VERSION = "1.12.2-0@VERSION@";
	public static final String DEPENDENCIES = "required:mixinbooter;required-after:packagedauto@[1.12.2-1.0.23,);required-after:appliedenergistics2;before:ae2fc";
	public static final CreativeTabs CREATIVE_TAB = new CreativeTabs("packagingprovider") {
		@SideOnly(Side.CLIENT)
		@Override
		public ItemStack createIcon() {
			return new ItemStack(BlockPackagingProvider.INSTANCE);
		}
	};
	@SidedProxy(
			clientSide = "thelm.packagingprovider.client.event.ClientEventHandler",
			serverSide = "thelm.packagingprovider.event.CommonEventHandler",
			modId = MOD_ID)
	public static CommonEventHandler eventHandler;

	@EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		eventHandler.onPreInit(event);
	}
}
