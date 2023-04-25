package xyz.jiangsier.cache;

import org.springframework.cache.annotation.Cacheable;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Cacheable(cacheNames = "middlePeriod", keyGenerator="fullNameKeyGenerator")
@SuppressWarnings("unused")
public @interface MiddlePeriodCache {
    String keyBy() default "";
}