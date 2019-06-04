package com.hsjfans.github.annotation;

import java.lang.annotation.*;

/**
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface Ignore {
}
