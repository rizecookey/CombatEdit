package net.rizecookey.combatedit.configuration.representation;

import net.minecraft.util.Identifier;
import net.rizecookey.combatedit.configuration.exception.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConfigurationView implements Configuration {
    private final List<Configuration> configurations;
    private final MiscOptionsView miscOptionsView;

    public ConfigurationView(Configuration... configurations) {
        this.configurations = List.of(configurations);
        this.miscOptionsView = new MiscOptionsView(() -> this.configurations.stream().map(Configuration::getMiscOptions).toList());
    }

    @Override
    public @NotNull List<ItemAttributes> getItemAttributes() {
        return configurations.reversed().stream()
                .flatMap(config -> config.getItemAttributes().stream())
                .toList();
    }

    @Override
    public @NotNull List<EntityAttributes> getEntityAttributes() {
        return configurations.reversed().stream()
                .flatMap(config -> config.getEntityAttributes().stream())
                .toList();
    }

    private static <T, U> Optional<T> firstPresent(Collection<U> sources, Function<U, Optional<T>> providers) {
        for (var source : sources) {
            var result = providers.apply(source);
            if (result.isPresent()) {
                return result;
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Boolean> isSoundEnabled(Identifier soundIdentifier) {
        return firstPresent(configurations, config -> config.isSoundEnabled(soundIdentifier));
    }

    @Override
    public @NotNull Map<Identifier, Boolean> getSoundMap() {
        Map<Identifier, Boolean> map = new HashMap<>();
        configurations.stream()
                .flatMap(config -> config.getSoundMap().keySet().stream())
                .filter(id -> isSoundEnabled(id).isPresent())
                .forEach(id -> map.put(id, isSoundEnabled(id).orElse(null)));
        return map;
    }

    @Override
    public @NotNull MiscOptions getMiscOptions() {
        return miscOptionsView;
    }

    @Override
    public void validate() throws InvalidConfigurationException {
        for (var config : configurations) {
            config.validate();
        }
    }

    public MutableConfiguration compileCurrentState() {
        return new MutableConfiguration(
                getItemAttributes(),
                getEntityAttributes(),
                getSoundMap(),
                new MutableConfiguration.MiscOptions(
                        getMiscOptions().is1_8KnockbackEnabled().orElse(null),
                        getMiscOptions().isSweepingWithoutEnchantmentDisabled().orElse(null)
                )
        );
    }

    public static class MiscOptionsView implements MiscOptions {
        private final Supplier<List<MiscOptions>> optionsSupplier;

        public MiscOptionsView(Supplier<List<MiscOptions>> optionsSupplier) {
            this.optionsSupplier = optionsSupplier;
        }

        @Override
        public Optional<Boolean> is1_8KnockbackEnabled() {
            return firstPresent(optionsSupplier.get(), MiscOptions::is1_8KnockbackEnabled);
        }

        @Override
        public Optional<Boolean> isSweepingWithoutEnchantmentDisabled() {
            return firstPresent(optionsSupplier.get(), MiscOptions::isSweepingWithoutEnchantmentDisabled);
        }
    }
}
