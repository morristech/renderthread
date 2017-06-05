package me.eugeniomarletti.renderthread.typeannotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.SOURCE)
public @interface Property {

    Class value() default Void.class;
}
