package io.github.httpsluch.timecapsule.platform;

//? if forge {
/*import io.github.httpsluch.timecapsule.TimeCapsule;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.DeferredRegister;

public final class ForgeRegistrar implements Registrar {
    private final Map<ResourceKey<?>, DeferredRegister<?>> registers = new LinkedHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <R, T extends R> Supplier<T> register(Registry<R> registry, String name, Function<ResourceKey<R>, T> factory) {
        DeferredRegister<R> deferred = (DeferredRegister<R>) registers.computeIfAbsent(
            registry.key(), k -> DeferredRegister.create(registry.key(), TimeCapsule.MOD_ID));
        ResourceKey<R> key = ResourceKey.create(registry.key(), TimeCapsule.id(name));
        return deferred.register(name, () -> factory.apply(key));
    }
*///?}

//? if forge && <26.1 {
/*    public void registerAll(net.minecraftforge.eventbus.api.IEventBus modBus) {
        registers.values().forEach(register -> register.register(modBus));
    }
*///?}

//? if forge && >=26.1 {
/*    public void registerAll(net.minecraftforge.eventbus.api.bus.BusGroup modBus) {
        registers.values().forEach(register -> register.register(modBus));
    }
*///?}

//? if forge {
/*}
*///?}
