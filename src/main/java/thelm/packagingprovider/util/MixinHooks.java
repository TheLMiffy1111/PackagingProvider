package thelm.packagingprovider.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import appeng.container.ContainerNull;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraftforge.fml.common.Loader;

public class MixinHooks {

	public static final MixinHooks INSTANCE = new MixinHooks();
	private static final Logger LOGGER = LogManager.getLogger();

	private MixinHooks() {}

	private MethodHandle convertingInvConstructor;

	public InventoryCrafting getCraftingInventory(int minSize) {
		// I don't think any processing recipe cares about the inventory dimensions
		// To be safe, we use a 3-wide inventory (should we use 4 instead because UEL?)
		int width = 3;
		int height = -Math.floorDiv(-minSize, 3);
		if(Loader.isModLoaded("ae2fc")) {
			try {
				if(convertingInvConstructor == null) {
					Class<?> convertingInvClass = null;
					try {
						convertingInvClass = Class.forName("com.glodblock.github.inventory.FluidConvertingInventoryCrafting");
					}
					catch(ClassNotFoundException e) {
						convertingInvClass = Class.forName("xyz.phanta.ae2fc.util.FluidConvertingInventoryCrafting");
					}
					if(convertingInvClass != null) {
						convertingInvConstructor = MethodHandles.publicLookup().findConstructor(convertingInvClass, MethodType.methodType(void.class, Container.class, int.class, int.class));
					}
				}
				if(convertingInvConstructor != null) {
					return (InventoryCrafting)convertingInvConstructor.invoke(new ContainerNull(), width, height);
				}
			}
			catch(Throwable e) {
				LOGGER.error("Failed to construct FluidConvertingInventoryCrafting", e);
			}
		}
		return new InventoryCrafting(new ContainerNull(), width, height);
	}
}
