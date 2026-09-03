package dev.komix.mcontain.mixin;

import dev.komix.mcontain.McontainMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

	@Inject(method = "run", at = @At("HEAD"), require = 0)
	private void mcontain_onRun(CallbackInfo ci) {
		onServerStart();
	}

	@Inject(method = "runServer", at = @At("HEAD"), require = 0)
	private void mcontain_onRunServer(CallbackInfo ci) {
		onServerStart();
	}

	@Inject(method = "tickServer", at = @At("HEAD"), require = 0)
	private void mcontain_onTickServer(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
		McontainMod mod = McontainMod.INSTANCE;
		if (mod != null) {
			mod.onTick(this);
			mod.tryRegisterCommands(this);
		}
	}

	private void onServerStart() {
		McontainMod mod = McontainMod.INSTANCE;
		if (mod != null) {
			mod.onServerStart(this);
		}
	}
}