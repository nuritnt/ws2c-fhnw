package telloflix.model.utils;


@FunctionalInterface
public interface ValueChangeListener<T> {
    void update(T oldValue, T newValue);
}
