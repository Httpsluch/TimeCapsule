package io.github.httpsluch.timecapsule.platform;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * Registers game objects the way each loader expects: Fabric registers immediately,
 * Forge and NeoForge defer to their registry events. The factory receives the entry's key
 * because 26.x block and item properties need it.
 */
public interface Registrar {
    <R, T extends R> Supplier<T> register(Registry<R> registry, String name, Function<ResourceKey<R>, T> factory);
}
