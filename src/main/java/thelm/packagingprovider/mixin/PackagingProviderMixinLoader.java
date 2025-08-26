package thelm.packagingprovider.mixin;

import java.util.Collections;
import java.util.List;

import zone.rong.mixinbooter.ILateMixinLoader;

public class PackagingProviderMixinLoader implements ILateMixinLoader {

	@Override
	public List<String> getMixinConfigs() {
		return Collections.singletonList("packagingprovider.mixins.json");
	}
}
