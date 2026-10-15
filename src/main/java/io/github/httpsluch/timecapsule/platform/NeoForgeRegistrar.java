package io.github.httpsluch.timecapsule.platform;

//? if neoforge {
/*import io.github.httpsluch.timecapsule.TimeCapsule;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeRegistrar implements Registrar {
    private final Map<ResourceKey<?>, DeferredRegister<?>> registers = new LinkedHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <R, T extends R> Supplier<T> register(Registry<R> registry, String name, Function<ResourceKey<R>, T> factory) {
        DeferredRegister<R> deferred = (DeferredRegister<R>) registers.computeIfAbsent(
            registry.key(), k -> DeferredRegister.create(registry.key(), TimeCapsule.MOD_ID));
        ResourceKey<R> key = ResourceKey.create(registry.key(), TimeCapsule.id(name));
        return deferred.register(name, () -> factory.apply(key));
    }

    public void registerAll(IEventBus modBus) {
        registers.values().forEach(register -> register.register(modBus));
    }
}
*///?}
