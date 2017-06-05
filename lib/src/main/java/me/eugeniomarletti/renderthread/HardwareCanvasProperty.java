package me.eugeniomarletti.renderthread;

import android.support.annotation.NonNull;

import me.eugeniomarletti.renderthread.typeannotation.Property;

public final class HardwareCanvasProperty<T> implements CanvasProperty<T> {

    @NonNull
    @Property
    private final Object property;

    HardwareCanvasProperty(@Property @NonNull Object property) {
        this.property = property;
    }

    @NonNull
    @Property
    Object getProperty() {
        return property;
    }

    @Override
    public boolean isHardware() {
        return true;
    }
}
