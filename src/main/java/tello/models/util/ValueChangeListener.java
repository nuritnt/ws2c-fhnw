package tello.models.util;


@FunctionalInterface
public interface ValueChangeListener<T> {
    void update(T oldValue, T newValue);
}
