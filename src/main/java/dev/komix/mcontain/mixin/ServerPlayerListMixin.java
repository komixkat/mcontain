package dev.komix.mcontain.mixin;

import dev.komix.mcontain.McontainMod;
import dev.komix.mcontain.Compat;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class ServerPlayerListMixin {

	@Inject(method = "onPlayerJoin", at = @At("HEAD"), require = 0)
	private void onPlayerJoin(Object player, CallbackInfo ci) {
		McontainMod mod = McontainMod.INSTANCE;
		if (mod != null) {
			Object server = Compat.invoke(this, "getServer");
			if (server != null) {
				mod.tryRegisterCommands(server);
			}
		}
	}
}