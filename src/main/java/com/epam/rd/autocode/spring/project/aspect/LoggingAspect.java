package com.epam.rd.autocode.spring.project.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @AfterThrowing(pointcut = "execution(* com.epam.rd.autocode.spring.project.service..*(..))", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String currentUser = getCurrentUsername();

        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            log.warn("SECURITY ALERT: User '{}' tried to execute {}.{}() but was DENIED. Message: {}",
                    currentUser, className, methodName, ex.getMessage());
        }
        else if (ex instanceof com.epam.rd.autocode.spring.project.exception.NotFoundException) {
            log.warn("DATA NOT FOUND: In {}.{}() by User '{}'. Message: {}",
                    className, methodName, currentUser, ex.getMessage());
        }
        else {
            log.error("SYSTEM ERROR in {}.{}() | User: {} | Exception: {} | Message: {}",
                    className, methodName, currentUser, ex.getClass().getSimpleName(), ex.getMessage(), ex);
        }
    }


    @Around("@annotation(com.epam.rd.autocode.spring.project.annotation.Loggable)")
    public Object logBusinessLogic(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String currentUser = getCurrentUsername();
        Object[] args = joinPoint.getArgs();

        if (log.isDebugEnabled()) {
            log.debug("DEBUG: Entering {}.{}() | User: {} | Args: {}",
                    className, methodName, currentUser, Arrays.toString(args));
        }

        long start = System.currentTimeMillis();
        Object result;

        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            throw e;
        }

        long executionTime = System.currentTimeMillis() - start;

        log.info("BUSINESS EVENT: {}.{}() completed | User: {} | Time: {}ms",
                className, methodName, currentUser, executionTime);

        if (log.isDebugEnabled()) {
            log.debug("DEBUG: Exiting {}.{}() | Result: {}",
                    className, methodName, result);
        }

        return result;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "Anonymous";
    }
}