package io.github.httpsluch.timecapsule.platform;

//? if fabric {
import io.github.httpsluch.timecapsule.TimeCapsule;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class FabricRegistrar implements Registrar {
    @Override
    public <R, T extends R> Supplier<T> register(Registry<R> registry, String name, Function<ResourceKey<R>, T> factory) {
        ResourceKey<R> key = ResourceKey.create(registry.key(), TimeCapsule.id(name));
        T value = Registry.register(registry, key, factory.apply(key));
        return () -> value;
    }
}
//?}
