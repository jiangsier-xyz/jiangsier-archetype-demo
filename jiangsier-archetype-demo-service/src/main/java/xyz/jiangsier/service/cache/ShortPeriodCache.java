package xyz.jiangsier.service.cache;

import org.springframework.cache.annotation.Cacheable;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Cacheable(cacheNames = "shortPeriod", keyGenerator = "fullNameKeyGenerator", unless= "#result == null")
@SuppressWarnings("unused")
public @interface ShortPeriodCache {
    String keyBy() default "";
}
