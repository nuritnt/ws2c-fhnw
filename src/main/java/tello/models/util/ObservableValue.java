package tello.models.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Alternative zu JavaFX-Properties.
 * <p>
 * Man kann weiterhin, wie in OOP2, mit JavaFX-Properties und z.B. Bindings arbeiten.
 * <p>
 * Oder man entwirft eine leicht zu verwendende API auf Basis von ObservableValue, das z.B. garantiert, dass die Wertänderungen
 * sicher im UI-Thread passieren (via 'Platform.runLater')
 */
public class ObservableValue<T> {
    private final Set<ValueChangeListener<T>> listeners = new HashSet<>();

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock                   r   = rwl.readLock();
    private final Lock                   w   = rwl.writeLock();

    private T value;

    public ObservableValue(T initialValue) {
        value = initialValue;
    }

    public void onChange(ValueChangeListener<T> listener) {
        w.lock();
        try {
            listeners.add(listener);
            listener.update(value, value);
        } finally {
            w.unlock();
        }
    }

    public void removeOnChange(ValueChangeListener<T> listener) {
        w.lock();
        try {
            listeners.remove(listener);
        } finally {
            w.unlock();
        }
    }

    public void setValue(T newValue) {
        w.lock();
        try {
            if (Objects.equals(value, newValue)) {
                return;
            }
            T oldValue = value;
            value = newValue;
            listeners.forEach(listener -> listener.update(oldValue, newValue));
        } finally {
            w.unlock();
        }
    }

    public T getValue() {
        r.lock();
        try {
            return value;
        } finally {
            r.unlock();
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
