package main.aspect;


import main.aspect.annotation.Loggable;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
@Aspect
public class LogAspect {
    private static  final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Before("@annotation(loggable)")
    public void logMethod(JoinPoint joinPoint, Loggable loggable) {
        logger.info("@Loggable - вызываем метод: {}", joinPoint.getSignature().getName());
    }

    @Around("annotation(trackExecutionTime)")
    public Object trackTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String methodName = proceedingJoinPoint.getSignature().getName();
        logger.info("@TrackExecutionTime - начало выполнения метода: {}", methodName);
        Object result = proceedingJoinPoint.proceed();
        stopWatch.stop();
        logger.info("@TrackExecutionTime - конец выполнения метода: {}, время: {}", methodName, stopWatch.getTotalTimeMillis());
        return result;
    }

    @AfterReturning(pointcut = "@annotation(logReturnValue)", returning = "result")
    public void logReturnValue(JoinPoint joinPoint, Object result) {
        logger.info("@LogReturnValue - Метод {} вернул: {}", joinPoint.getSignature().toShortString(), result);
    }

    @AfterThrowing(pointcut = "@annotation(handleExceptions)", throwing = "e")
    public void handleException(JoinPoint joinPoint, Throwable e) throws Throwable {
        logger.error("@HandleExceptions - В методе {} произошло исключение: {}",
                joinPoint.getSignature().toShortString(), e.getMessage());
        throw e;
    }

}
