package thelm.packagingprovider.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.block.BlockBase;
import thelm.packagedauto.tile.TileBase;
import thelm.packagingprovider.PackagingProvider;
import thelm.packagingprovider.tile.TilePackagingProvider;

public class BlockPackagingProvider extends BlockBase {

	public static final BlockPackagingProvider INSTANCE = new BlockPackagingProvider();
	public static final Item ITEM_INSTANCE = new ItemBlock(INSTANCE).setRegistryName("packagingprovider:packaging_provider");
	public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packagingprovider:packaging_provider#normal");

	public BlockPackagingProvider() {
		super(Material.IRON);
		setHardness(10F);
		setResistance(25F);
		setSoundType(SoundType.METAL);
		setTranslationKey("packagingprovider.packaging_provider");
		setRegistryName("packagingprovider:packaging_provider");
		setCreativeTab(PackagingProvider.CREATIVE_TAB);
	}

	@Override
	public TileBase createNewTileEntity(World worldIn, int meta) {
		return new TilePackagingProvider();
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if(tileentity instanceof TilePackagingProvider) {
			TilePackagingProvider provider = (TilePackagingProvider)tileentity;
			if(provider.currentPattern != null) {
				for(ItemStack stack : provider.currentPattern.getInputs()) {
					if(!stack.isEmpty()) {
						InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
					}
				}
			}
			if(!provider.toSend.isEmpty()) {
				for(ItemStack stack : provider.toSend) {
					if(!stack.isEmpty()) {
						InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
					}
				}
			}
		}
		super.breakBlock(worldIn, pos, state);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels() {
		ModelLoader.setCustomModelResourceLocation(ITEM_INSTANCE, 0, MODEL_LOCATION);
	}
}
