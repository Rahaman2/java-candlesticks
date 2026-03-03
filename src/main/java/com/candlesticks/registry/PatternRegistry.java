package com.candlesticks.registry;

import com.candlesticks.pattern.PatternDefinition;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry of pattern definitions.
 *
 * <p>Preserves insertion order so patterns are scanned in a predictable sequence.
 * Not thread-safe for concurrent writes — configure once at startup, then treat as read-only.</p>
 *
 * <p>To add custom patterns:</p>
 * <pre>{@code
 * PatternRegistry registry = BuiltInPatterns.createRegistry();
 * registry.register(new PatternDefinition("myPattern", c -> ..., 1, metadata));
 * CandleScanner scanner = new CandleScanner(registry);
 * }</pre>
 */
public class PatternRegistry {

    private final Map<String, PatternDefinition> patterns = new LinkedHashMap<>();

    /**
     * Register a pattern definition.
     *
     * @throws IllegalArgumentException if a pattern with the same name is already registered
     */
    public void register(PatternDefinition definition) {
        if (patterns.containsKey(definition.name())) {
            throw new IllegalArgumentException(
                    "Pattern already registered: \"" + definition.name() + "\"");
        }
        patterns.put(definition.name(), definition);
    }

    /**
     * Remove a pattern by name.
     *
     * @return {@code true} if the pattern existed and was removed
     */
    public boolean unregister(String name) {
        return patterns.remove(name) != null;
    }

    /** Look up a pattern definition by name. */
    public Optional<PatternDefinition> get(String name) {
        return Optional.ofNullable(patterns.get(name));
    }

    /** Returns an unmodifiable view of all registered patterns in insertion order. */
    public Collection<PatternDefinition> getAll() {
        return Collections.unmodifiableCollection(patterns.values());
    }

    /** Returns {@code true} if a pattern with the given name is registered. */
    public boolean isRegistered(String name) {
        return patterns.containsKey(name);
    }

    /** Number of registered patterns. */
    public int size() {
        return patterns.size();
    }
}
