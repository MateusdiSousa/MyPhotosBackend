package mateus.sousa.myphotobackend.config;

import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class ControllerLogger {
    private static final Logger logger = LoggerFactory.getLogger(ControllerLogger.class);

    @Around("execution(* mateus.sousa.myphotobackend.controller.*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String method = request.getMethod();
        String endpoint = request.getRequestURI();

        if (endpoint.startsWith("/photos/view/")) {
            return joinPoint.proceed();
        }

        logger.info("START: {} {}", method, endpoint);

        long startTime = System.currentTimeMillis();

        try {
            Object response = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            logger.info("SUCCESS: {} {} | Time: {}ms", method, endpoint, executionTime);
            return response;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("ERROR: {} {} | Time: {}ms | Message: {}",
                    method, endpoint, executionTime, e.getMessage());
            throw e;
        }
    }
}
