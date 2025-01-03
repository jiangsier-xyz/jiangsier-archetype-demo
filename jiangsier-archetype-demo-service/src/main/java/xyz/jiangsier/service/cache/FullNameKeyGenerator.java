package xyz.jiangsier.service.cache;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import xyz.jiangsier.util.SpELUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

@Component("fullNameKeyGenerator")
@SuppressWarnings("unused")
public class FullNameKeyGenerator implements KeyGenerator {
    private static final Logger logger = LoggerFactory.getLogger(FullNameKeyGenerator.class);

    private static final String[] SHORT_PERIOD_CACHE_NAMES = new String[]{"shortPeriod"};
    private static final String[] MIDDLE_PERIOD_CACHE_NAMES = new String[]{"middlePeriod"};
    private static final String[] LONG_PERIOD_CACHE_NAMES = new String[]{"longPeriod"};

    @Override
    public Object generate(Object target, Method method, Object... params) {
        Pair<String[], String> cachesKeyExpr = getCachesKeyExpr(method);
        String key = null;
        if (cachesKeyExpr != null && !ObjectUtils.isEmpty(cachesKeyExpr.getRight())) {
            key = generateSpELKey(target, method, params, cachesKeyExpr);
        }
        if (!StringUtils.hasText(key)) {
            key = generateDefaultKey(target, method, params);
        }
        return key;
    }

    private Pair<String[], String> getCachesKeyExpr(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof ShortPeriodCache) {
                return Pair.of(SHORT_PERIOD_CACHE_NAMES, ((ShortPeriodCache) annotation).keyBy());
            } else if (annotation instanceof MiddlePeriodCache) {
                return Pair.of(MIDDLE_PERIOD_CACHE_NAMES, ((MiddlePeriodCache) annotation).keyBy());
            } else if (annotation instanceof LongPeriodCache) {
                return Pair.of(LONG_PERIOD_CACHE_NAMES, ((LongPeriodCache) annotation).keyBy());
            } else if (annotation instanceof Cacheable) {
                return Pair.of(((Cacheable) annotation).cacheNames(), ((Cacheable) annotation).key());
            }
        }

        return null;
    }

    private Pair<String[], String> getCachesKeyExpr(Method method) {
        Pair<String[], String> cachesKeyExpr = getCachesKeyExpr(method.getAnnotations());
        return cachesKeyExpr != null ? cachesKeyExpr
                : getCachesKeyExpr(method.getDeclaringClass().getAnnotations());
    }

    private String generateDefaultKey(Object target, Method method, Object[] params) {
        String key = target.getClass().getName() + "::" + method.getName() + "_";
        if (params.length > 0) {
            key += StringUtils.arrayToDelimitedString(params, "_");
        }
        return key;
    }

    private String generateSpELKey(Object target, Method method, Object[] params,
                                   Pair<String[], String> cachesKeyExpr) {
        SpelExpressionParser parser = new SpelExpressionParser();
        SimpleEvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();
        Map<String, Object> contextInfo = SpELUtils.extractInfo(target, method, params);
        contextInfo.forEach(context::setVariable);
        context.setVariable("caches", cachesKeyExpr.getLeft());
        try {
            Expression expr = parser.parseExpression(cachesKeyExpr.getRight());
            return expr.getValue(context, String.class);
        } catch (ParseException | EvaluationException e) {
            logger.error("Failed to generate SpEL key!", e);
        }
        return null;
    }

    static class Ada {
        public String hi = "Hello ";

        public String sayHello(String somebody) {
            return hi + somebody;
        }
    }

    private static Pair<String[], String> testPair(String expr) {
        return Pair.of(LONG_PERIOD_CACHE_NAMES, expr);
    }

    public static void main(String[] args) throws NoSuchMethodException {
        Ada ada = new Ada();
        Method hello = Ada.class.getMethod("sayHello", String.class);
        String bob = "bob";

        FullNameKeyGenerator gen = new FullNameKeyGenerator();
        Object[] params = new Object[]{bob};
        System.out.println(gen.generateSpELKey(ada, hello, params, testPair("#methodName")));
        System.out.println(gen.generateSpELKey(ada, hello, params, testPair("#method.name")));
        System.out.println(gen.generateSpELKey(ada, hello, params, testPair("#args")));
        System.out.println(gen.generateSpELKey(ada, hello, params, testPair("#p0")));
        System.out.println(gen.generateSpELKey(ada, hello, params, testPair("#somebody")));
        System.out.println(gen.generateSpELKey(ada, hello, params, testPair("#target.hi")));
        System.out.println(gen.generateSpELKey(ada, hello, params, testPair("'Bye ' + #p0")));
    }
}
