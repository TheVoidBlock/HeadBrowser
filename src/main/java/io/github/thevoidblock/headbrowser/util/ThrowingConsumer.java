package io.github.thevoidblock.headbrowser.util;

@FunctionalInterface
public interface ThrowingConsumer<T> {
    void accept(T t) throws Exception;
}
