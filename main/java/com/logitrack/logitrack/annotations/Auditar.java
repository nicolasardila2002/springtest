package com.logitrack.logitrack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditar {
    String descripcion() default "";
    com.logitrack.logitrack.enums.TipoOperacion operacion() default com.logitrack.logitrack.enums.TipoOperacion.UPDATE;
}