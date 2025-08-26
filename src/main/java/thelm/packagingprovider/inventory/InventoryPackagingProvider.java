package thelm.packagingprovider.inventory;

import it.unimi.dsi.fastutil.ints.IntArrays;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import thelm.packagedauto.api.IRecipeListItem;
import thelm.packagedauto.inventory.InventoryTileBase;
import thelm.packagingprovider.tile.TilePackagingProvider;

public class InventoryPackagingProvider extends InventoryTileBase {

	public final TilePackagingProvider tile;

	public InventoryPackagingProvider(TilePackagingProvider tile) {
		super(tile, 1);
		this.tile = tile;
		slots = IntArrays.EMPTY_ARRAY;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		super.setInventorySlotContents(index, stack);
		updateRecipeList();
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack stack = super.decrStackSize(index, count);
		updateRecipeList();
		return stack;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return stack.getItem() instanceof IRecipeListItem;
	}

	@Override
	public int getField(int id) {
		switch(id) {
		case 0: return tile.blocking ? 1 : 0;
		case 1: return tile.provideDirect ? 1 : 0;
		case 2: return tile.providePackaging ? 1 : 0;
		case 3: return tile.provideUnpackaging ? 1 : 0;
		default: return 0;
		}
	}

	@Override
	public void setField(int id, int value) {
		switch(id) {
		case 0:
			tile.blocking = value != 0;
			break;
		case 1:
			tile.provideDirect = value != 0;
			break;
		case 2:
			tile.providePackaging = value != 0;
			break;
		case 3:
			tile.provideUnpackaging = value != 0;
			break;
		}
	}

	@Override
	public int getFieldCount() {
		return 4;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		updateRecipeList();
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return false;
	}

	public void updateRecipeList() {
		tile.recipeList.clear();
		ItemStack listStack = getStackInSlot(0);
		if(listStack.getItem() instanceof IRecipeListItem) {
			tile.recipeList.addAll(((IRecipeListItem)listStack.getItem()).getRecipeList(listStack).getRecipeList());
		}
		if(tile.getWorld() != null && !tile.getWorld().isRemote) {
			tile.hostHelper.postPatternChange();
		}
	}
}
