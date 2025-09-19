package thelm.packagingprovider.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.item.ItemPackage;
import thelm.packagingprovider.recipe.DirectCraftingPatternHelper;

@Mixin(ItemPackage.class)
public abstract class ItemPackageMixin implements IPackageItem {

	@Inject(method = "getPatternForItem", at = @At("TAIL"), cancellable = true, remap = false)
	private void onGetPatternForItem(ItemStack stack, World world, CallbackInfoReturnable<ICraftingPatternDetails> ci) {
		if(stack.hasTagCompound() && "direct".equals(stack.getTagCompound().getString("PatternType"))) {
			IRecipeInfo recipe = getRecipeInfo(stack);
			if(recipe != null && recipe.isValid()) {
				ci.setReturnValue(new DirectCraftingPatternHelper(recipe));
				return;
			}
		}
	}
}
