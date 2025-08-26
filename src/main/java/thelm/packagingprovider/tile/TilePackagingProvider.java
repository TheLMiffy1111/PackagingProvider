package thelm.packagingprovider.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageProvidingMachine;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeList;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.integration.appeng.AppEngUtil;
import thelm.packagedauto.integration.appeng.recipe.PackageCraftingPatternHelper;
import thelm.packagedauto.integration.appeng.recipe.RecipeCraftingPatternHelper;
import thelm.packagedauto.item.ItemRecipeHolder;
import thelm.packagedauto.tile.TileBase;
import thelm.packagedauto.tile.TilePackager;
import thelm.packagedauto.tile.TilePackagerExtension;
import thelm.packagedauto.tile.TileUnpackager;
import thelm.packagingprovider.client.gui.GuiPackagingProvider;
import thelm.packagingprovider.container.ContainerPackagingProvider;
import thelm.packagingprovider.inventory.InventoryPackagingProvider;
import thelm.packagingprovider.networking.HostHelperTilePackagingProvider;
import thelm.packagingprovider.recipe.DirectCraftingPatternHelper;

public class TilePackagingProvider extends TileBase implements ITickable, IPackageProvidingMachine, ISettingsCloneable, IGridHost, IActionHost, ICraftingProvider {

	public boolean firstTick = true;
	public List<IRecipeInfo> recipeList = new ArrayList<>();
	public IPackagePattern currentPattern;
	public List<ItemStack> toSend = new ArrayList<>();
	public EnumFacing sendDirection;
	public boolean sendOrdered;
	public boolean powered = false;
	public boolean blocking = false;
	public boolean provideDirect = true;
	public boolean providePackaging = false;
	public boolean provideUnpackaging = false;
	public int roundRobinIndex = 0;
	public HostHelperTilePackagingProvider hostHelper;

	public TilePackagingProvider() {
		setInventory(new InventoryPackagingProvider(this));
		hostHelper = new HostHelperTilePackagingProvider(this);
	}

	@Override
	protected String getLocalizedName() {
		return I18n.translateToLocal("tile.packagingprovider.packaging_provider.name");
	}

	@Override
	public String getConfigTypeName() {
		return "tile.packagingprovider.packaging_provider.name";
	}

	@Override
	public void onLoad() {
		updatePowered();
	}

	@Override
	public void update() {
		if(firstTick) {
			firstTick = false;
			if(!world.isRemote) {
				hostHelper.postPatternChange();
			}
		}
		if(!world.isRemote) {
			if(currentPattern != null) {
				sendPackaging();
			}
			if(!toSend.isEmpty()) {
				sendUnpackaging();
			}
		}
	}

	@Override
	public ItemStack getPatternStack() {
		return inventory.getStackInSlot(0);
	}

	@Override
	public void setPatternStack(ItemStack stack) {
		inventory.setInventorySlotContents(0, stack);
	}

	protected void sendPackaging() {
		if(currentPattern == null) {
			return;
		}
		if(hostHelper.isActive()) {
			IGrid grid = hostHelper.getNode().getGrid();
			IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
			IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
			IItemStorageChannel storageChannel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
			IMEMonitor<IAEItemStack> inventory = storageGrid.getInventory(storageChannel);
			IAEItemStack stack = storageChannel.createStack(currentPattern.getOutput());
			IAEItemStack rem = AEApi.instance().storage().poweredInsert(energyGrid, inventory, stack, hostHelper.source, Actionable.MODULATE);
			if(rem == null || rem.getStackSize() == 0) {
				currentPattern = null;
			}
		}
	}

	protected void sendUnpackaging() {
		if(toSend.isEmpty()) {
			return;
		}
		if(sendDirection != null) {
			TileEntity tile = world.getTileEntity(pos.offset(sendDirection));
			if(!validSendTarget(tile, sendDirection.getOpposite())) {
				sendDirection = null;
				return;
			}
			IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, sendDirection.getOpposite());
			if(itemHandler == null) {
				sendDirection = null;
				return;
			}
			for(int i = 0; i < toSend.size(); ++i) {
				ItemStack stack = toSend.get(i);
				ItemStack stackRem = MiscUtil.insertItem(itemHandler, stack, sendOrdered, false);
				toSend.set(i, stackRem);
			}
			toSend.removeIf(ItemStack::isEmpty);
			markDirty();
		}
		else if(hostHelper.isActive()) {
			IGrid grid = hostHelper.getNode().getGrid();
			IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
			IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
			IItemStorageChannel storageChannel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
			IMEMonitor<IAEItemStack> inventory = storageGrid.getInventory(storageChannel);
			for(int i = 0; i < toSend.size(); ++i) {
				ItemStack is = toSend.get(i);
				if(is.isEmpty()) {
					continue;
				}
				IAEItemStack stack = storageChannel.createStack(is);
				IAEItemStack rem = AEApi.instance().storage().poweredInsert(energyGrid, inventory, stack, hostHelper.source, Actionable.MODULATE);
				if(rem == null || rem.getStackSize() == 0) {
					toSend.set(i, ItemStack.EMPTY);
				}
				else {
					toSend.set(i, rem.createItemStack());
				}
			}
			toSend.removeIf(ItemStack::isEmpty);
			markDirty();
		}
	}

	public void updatePowered() {
		if(world.getRedstonePowerFromNeighbors(pos) > 0 != powered) {
			powered = !powered;
			markDirty();
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if(hostHelper != null) {
			hostHelper.invalidate();
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if(hostHelper != null) {
			hostHelper.invalidate();
		}
	}

	@Override
	public IGridNode getGridNode(AEPartLocation dir) {
		return getActionableNode();
	}

	@Override
	public AECableType getCableConnectionType(AEPartLocation dir) {
		return AECableType.SMART;
	}

	@Override
	public void securityBreak() {
		world.destroyBlock(pos, true);
	}

	@Override
	public IGridNode getActionableNode() {
		return hostHelper.getNode();
	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
		if(hostHelper.isActive() && !isBusy()) {
			IGrid grid = hostHelper.getNode().getGrid();
			IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
			double conversion = PowerUnits.RF.convertTo(PowerUnits.AE, 1);
			IRecipeInfo recipe = null;
			if(patternDetails instanceof DirectCraftingPatternHelper) {
				recipe = ((DirectCraftingPatternHelper)patternDetails).recipe;
			}
			else if(patternDetails instanceof RecipeCraftingPatternHelper) {
				recipe = ((RecipeCraftingPatternHelper)patternDetails).recipe;
			}
			else if(patternDetails instanceof PackageCraftingPatternHelper) {
				double request = TilePackager.energyReq*2*conversion;
				if(request - energyGrid.extractAEPower(request, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.0001) {
					return false;
				}
				energyGrid.extractAEPower(request, Actionable.MODULATE, PowerMultiplier.CONFIG);
				currentPattern = ((PackageCraftingPatternHelper)patternDetails).pattern;
				return true;
			}
			if(recipe != null) {
				double request = (TilePackager.energyReq*2+TileUnpackager.energyUsage)*conversion;
				if(request - energyGrid.extractAEPower(request, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.0001) {
					return false;
				}
				if(recipe.getRecipeType().hasMachine()) {
					List<EnumFacing> directions = Lists.newArrayList(EnumFacing.values());
					Collections.rotate(directions, roundRobinIndex);
					for(EnumFacing facing : directions) {
						TileEntity tile = world.getTileEntity(pos.offset(facing));
						if(tile instanceof IPackageCraftingMachine) {
							IPackageCraftingMachine machine = (IPackageCraftingMachine)tile;
							if(!machine.isBusy() && machine.acceptPackage(recipe, Lists.transform(recipe.getInputs(), ItemStack::copy), facing.getOpposite(), blocking)) {
								energyGrid.extractAEPower(request, Actionable.SIMULATE, PowerMultiplier.CONFIG);
								roundRobinIndex = (roundRobinIndex+1) % 6;
								return true;
							}
						}
					}
				}
				else {
					List<ItemStack> toSend = new ArrayList<>();
					recipe.getInputs().stream().map(ItemStack::copy).forEach(toSend::add);
					List<EnumFacing> directions = Lists.newArrayList(EnumFacing.values());
					Collections.rotate(directions, roundRobinIndex);
					for(EnumFacing facing : directions) {
						TileEntity tile = world.getTileEntity(pos.offset(facing));
						if(!validSendTarget(tile, facing.getOpposite())) {
							continue;
						}
						IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
						if(itemHandler == null) {
							continue;
						}
						if(blocking && !MiscUtil.isEmpty(itemHandler)) {
							continue;
						}
						boolean acceptsAll = true;
						for(int i = 0; i < toSend.size(); ++i) {
							ItemStack stack = toSend.get(i);
							ItemStack stackRem = MiscUtil.insertItem(itemHandler, stack, false, true);
							acceptsAll &= stackRem.getCount() < stack.getCount();
						}
						if(acceptsAll) {
							energyGrid.extractAEPower(request, Actionable.MODULATE, PowerMultiplier.CONFIG);
							sendDirection = facing;
							this.toSend.addAll(toSend);
							sendOrdered = recipe.getRecipeType().isOrdered();
							roundRobinIndex = (roundRobinIndex+1) % 6;
							sendUnpackaging();
							return true;
						}
					}
					return false;
				}
			}
		}
		return false;
	}

	protected boolean validSendTarget(TileEntity tile, EnumFacing facing) {
		return tile != null &&
				!(tile instanceof IPackageCraftingMachine) &&
				!(tile instanceof TilePackager) &&
				!(tile instanceof TilePackagerExtension) &&
				!(tile instanceof TileUnpackager) &&
				!AppEngUtil.isInterface(tile, facing.getOpposite());
	}

	@Override
	public boolean isBusy() {
		return powered || currentPattern != null || !toSend.isEmpty();
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		if(hostHelper.isActive()) {
			if(provideDirect) {
				recipeList.stream().filter(pattern->!pattern.getOutputs().isEmpty()).
				map(pattern->new DirectCraftingPatternHelper(pattern)).
				forEach(pattern->craftingTracker.addCraftingOption(this, pattern));
			}
			if(providePackaging) {
				recipeList.stream().filter(IRecipeInfo::isValid).
				flatMap(recipe->Streams.concat(recipe.getPatterns().stream(), recipe.getExtraPatterns().stream())).
				map(pattern->new PackageCraftingPatternHelper(pattern)).
				forEach(pattern->craftingTracker.addCraftingOption(this, pattern));
			}
			if(provideUnpackaging) {
				recipeList.stream().filter(pattern->!pattern.getOutputs().isEmpty()).
				map(pattern->new RecipeCraftingPatternHelper(pattern)).
				forEach(pattern->craftingTracker.addCraftingOption(this, pattern));
			}
		}
	}

	@MENetworkEventSubscribe
	public void onChannelsChanged(MENetworkChannelsChanged event) {
		hostHelper.postPatternChange();
	}

	@MENetworkEventSubscribe
	public void onPowerStatusChange(MENetworkPowerStatusChange event) {
		hostHelper.postPatternChange();
	}

	public void changeBlockingMode() {
		blocking = !blocking;
		markDirty();
	}

	public void changeProvideType(Type type) {
		switch(type) {
		case DIRECT: {
			provideDirect = !provideDirect;
			if(provideDirect && providePackaging && provideUnpackaging) {
				providePackaging = provideUnpackaging = false;
			}
			if(!provideDirect && !providePackaging && !provideUnpackaging) {
				providePackaging = provideUnpackaging = true;
			}
			break;
		}
		case PACKAGING: {
			providePackaging = !providePackaging;
			if(provideDirect && providePackaging && provideUnpackaging) {
				provideDirect = false;
			}
			if(!provideDirect && !providePackaging && !provideUnpackaging) {
				provideDirect = true;
			}
			break;
		}
		case UNPACKAGING: {
			provideUnpackaging = !provideUnpackaging;
			if(provideDirect && providePackaging && provideUnpackaging) {
				provideDirect = false;
			}
			if(!provideDirect && !providePackaging && !provideUnpackaging) {
				provideDirect = true;
			}
			break;
		}
		}
		hostHelper.postPatternChange();
		markDirty();
	}

	@Override
	public ISettingsCloneable.Result loadConfig(NBTTagCompound nbt, EntityPlayer player) {
		blocking = nbt.getBoolean("Blocking");
		provideDirect = nbt.getBoolean("Direct");
		providePackaging = nbt.getBoolean("Packaging");
		provideUnpackaging = nbt.getBoolean("Unpackaging");
		ITextComponent message = null;
		if(nbt.hasKey("Recipes")) {
			f:if(inventory.getStackInSlot(0).isEmpty()) {
				InventoryPlayer playerInventory = player.inventory;
				for(int i = 0; i < playerInventory.getSizeInventory(); ++i) {
					ItemStack stack = playerInventory.getStackInSlot(i);
					if(!stack.isEmpty() && stack.getItem() == ItemRecipeHolder.INSTANCE && !stack.hasTagCompound()) {
						ItemStack stackCopy = stack.splitStack(1);
						IRecipeList recipeListObj = ItemRecipeHolder.INSTANCE.getRecipeList(stackCopy);
						List<IRecipeInfo> recipeList = MiscUtil.readRecipeListFromNBT(nbt.getTagList("Recipes", 10));
						recipeListObj.setRecipeList(recipeList);
						ItemRecipeHolder.INSTANCE.setRecipeList(stackCopy, recipeListObj);
						inventory.setInventorySlotContents(0, stackCopy);
						break f;
					}
				}
				message = new TextComponentTranslation("tile.packagingprovider.packaging_provider.no_holders");
			}
			else {
				message = new TextComponentTranslation("tile.packagingprovider.packaging_provider.holder_present");
			}
		}
		if(message != null) {
			return ISettingsCloneable.Result.partial(message);
		}
		else {
			return ISettingsCloneable.Result.success();
		}
	}

	@Override
	public ISettingsCloneable.Result saveConfig(NBTTagCompound nbt, EntityPlayer player) {
		nbt.setBoolean("Blocking", blocking);
		nbt.setBoolean("Direct", provideDirect);
		nbt.setBoolean("Packaging", providePackaging);
		nbt.setBoolean("Unpackaging", provideUnpackaging);
		if(!recipeList.isEmpty()) {
			nbt.setTag("Recipes", MiscUtil.writeRecipeListToNBT(new NBTTagList(), recipeList));
		}
		return ISettingsCloneable.Result.success();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		hostHelper.readFromNBT(nbt);
		super.readFromNBT(nbt);
		blocking = nbt.getBoolean("Blocking");
		provideDirect = nbt.getBoolean("Direct");
		providePackaging = nbt.getBoolean("Packaging");
		provideUnpackaging = nbt.getBoolean("Unpackaging");
		powered = nbt.getBoolean("Powered");
		if(nbt.hasKey("Pattern")) {
			NBTTagCompound tag = nbt.getCompoundTag("Pattern");
			IRecipeInfo recipe = MiscUtil.readRecipeFromNBT(tag);
			if(recipe != null) {
				List<IPackagePattern> patterns = recipe.getPatterns();
				byte index = tag.getByte("Index");
				if(index >= 0 && index < patterns.size()) {
					currentPattern = patterns.get(index);
				}
			}
		}
		MiscUtil.loadAllItems(nbt.getTagList("ToSend", 10), toSend);
		if(nbt.hasKey("SendDirection")) {
			sendDirection = EnumFacing.byIndex(nbt.getByte("SendDirection"));
		}
		sendOrdered = nbt.getBoolean("SendOrdered");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("Blocking", blocking);
		nbt.setBoolean("Direct", provideDirect);
		nbt.setBoolean("Packaging", providePackaging);
		nbt.setBoolean("Unpackaging", provideUnpackaging);
		nbt.setBoolean("Powered", powered);
		if(currentPattern != null) {
			NBTTagCompound tag = MiscUtil.writeRecipeToNBT(new NBTTagCompound(), currentPattern.getRecipeInfo());
			tag.setByte("Index", (byte)currentPattern.getIndex());
			nbt.setTag("Pattern", tag);
		}
		nbt.setTag("ToSend", MiscUtil.saveAllItems(new NBTTagList(), toSend));
		if(sendDirection != null) {
			nbt.setByte("SendDirection", (byte)sendDirection.getIndex());
		}
		nbt.setBoolean("SendOrdered", sendOrdered);
		hostHelper.writeToNBT(nbt);
		return nbt;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getClientGuiElement(EntityPlayer player, Object... args) {
		return new GuiPackagingProvider(new ContainerPackagingProvider(player.inventory, this));
	}

	@Override
	public Container getServerGuiElement(EntityPlayer player, Object... args) {
		return new ContainerPackagingProvider(player.inventory, this);
	}

	public static enum Type {
		DIRECT, PACKAGING, UNPACKAGING;
	}
}
