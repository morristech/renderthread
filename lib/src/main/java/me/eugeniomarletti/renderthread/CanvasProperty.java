package me.eugeniomarletti.renderthread;

@SuppressWarnings("unused") // The generics is used to ensure type safety across implementers
public interface CanvasProperty<T> {

    boolean isHardware();
}
