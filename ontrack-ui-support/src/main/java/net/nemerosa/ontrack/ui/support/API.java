package net.nemerosa.ontrack.ui.support;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface API {

    String value();

    String description() default "";

}
