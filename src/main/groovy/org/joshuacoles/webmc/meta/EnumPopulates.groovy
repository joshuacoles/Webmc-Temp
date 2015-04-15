package org.joshuacoles.webmc.meta

import javax.annotation.Nonnull
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Marker annotation that alerts {@see SpongeInjector} that this class is to be used to populate the given class.
 * */

@Nonnull
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface EnumPopulates {
    Class value()

    Class type() default _

    static class _ {}
}