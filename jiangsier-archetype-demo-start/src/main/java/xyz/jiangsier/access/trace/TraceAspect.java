package xyz.jiangsier.access.trace;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import xyz.jiangsier.annotation.Secret;
import xyz.jiangsier.annotation.Trace;
import xyz.jiangsier.exception.BadRequestException;
import xyz.jiangsier.util.SpELUtils;
import xyz.jiangsier.util.TraceUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Aspect
@Service
@SuppressWarnings("unused")
public class TraceAspect {
    private static final Logger logger = LoggerFactory.getLogger(TraceAspect.class);

    @Pointcut("@annotation(xyz.jiangsier.annotation.Trace)")
    public void tracePointCut() {}

    @Around("tracePointCut()")
    public Object traceAround(ProceedingJoinPoint point) throws Throwable {
        final long startTime = System.currentTimeMillis();
        TraceUtils.TraceStatus status = TraceUtils.TraceStatus.SUCCESSFUL;
        Object response = null;
        Throwable throwable = null;
        Signature signature = point.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        try {
            response = point.proceed();
        } catch (BadRequestException e) {
            status = TraceUtils.TraceStatus.BAD_REQUEST;
            throwable = e;
            throw e;
        } catch (Throwable t) {
            status = TraceUtils.TraceStatus.FAILED;
            throwable = t;
            throw t;
        } finally {
            Trace trace = getTraceAnnotation(method);
            if (trace != null) {
                String username = Optional.ofNullable(SecurityContextHolder.getContext())
                        .map(SecurityContext::getAuthentication)
                        .map(Principal::getName)
                        .orElse((String) TraceUtils.getTraceAttribute("TRACE_USER"));

                TraceUtils.TraceInfoBuilder builder = new TraceUtils.TraceInfoBuilder();
                builder.traceId(TraceUtils.getTraceId())
                        .username(username)
                        .method(method)
                        .status(status)
                        .args(trace.ignoreArgs() ? null : getArgs(point, method))
                        .response(trace.ignoreReturn() ? null : response)
                        .throwable(throwable)
                        .elapseTime(System.currentTimeMillis() - startTime)
                        .extInfo(getExtInfoBySpEL(point.getTarget(), method, point.getArgs(), trace.extInfo()));
                logger.trace(builder.build());
            }
        }

        return response;
    }

    private Trace getTraceAnnotation(Method method) {
        return Optional.ofNullable(method.getAnnotation(Trace.class))
                .orElseGet(() -> method.getDeclaringClass().getAnnotation(Trace.class));
    }

    private Object[] getArgs(ProceedingJoinPoint point, Method method) {
        Object[] args = point.getArgs();
        if (args == null) {
            return null;
        }

        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < annotations.length; ++i) {
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof Secret) {
                    args[i] = "*";
                    break;
                }
            }
        }
        return args;
    }

    private String getExtInfoBySpEL(Object target, Method method, Object[] params, String extExpr) {
        if (StringUtils.isBlank(extExpr)) {
            return null;
        }

        SpelExpressionParser parser = new SpelExpressionParser();
        SimpleEvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();
        Map<String, Object> contextInfo = SpELUtils.extractInfo(target, method, params);
        contextInfo.forEach(context::setVariable);
        try {
            Expression expr = parser.parseExpression(extExpr);
            return expr.getValue(context, String.class);
        } catch (ParseException | EvaluationException e) {
            logger.warn("Failed to get extra information from SpEL key!", e);
        }
        return extExpr;
    }
}
