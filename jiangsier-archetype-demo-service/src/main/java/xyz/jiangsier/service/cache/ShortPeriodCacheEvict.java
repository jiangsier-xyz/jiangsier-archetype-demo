package xyz.jiangsier.service.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@CacheEvict(cacheNames = "shortPeriod")
@SuppressWarnings("unused")
public @interface ShortPeriodCacheEvict {
    @AliasFor("keyBy")
    String key() default "";

    @AliasFor("key")
    String keyBy() default "";
}
