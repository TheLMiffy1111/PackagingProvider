package thelm.packagingprovider.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import net.minecraft.inventory.InventoryCrafting;
import thelm.packagingprovider.util.MixinHooks;

@Mixin(CraftingCPUCluster.class)
public abstract class CraftingCPUClusterMixin {

	@ModifyVariable(method = "executeCrafting", at = @At("STORE"), name = "ic", remap = false)
	private InventoryCrafting modifyCraftingInventory(InventoryCrafting original, @Local ICraftingPatternDetails details) {
		if(original != null && !details.isCraftable()) {
			return MixinHooks.INSTANCE.getCraftingInventory(details.getInputs().length);
		}
		return original;
	}
}
