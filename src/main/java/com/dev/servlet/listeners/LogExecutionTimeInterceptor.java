package com.dev.servlet.listeners;

import com.dev.servlet.interfaces.LogExecutionTime;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@LogExecutionTime
public class LogExecutionTimeInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogExecutionTimeInterceptor.class);

    @AroundInvoke
    public Object logMethodExecutionTime(InvocationContext context) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            return context.proceed();
        } finally {
            stopWatch.stop();
            long time = stopWatch.getTime();

            String message = "The method {method} from {class} took {time} ms.";
            message = message.replace("{method}", context.getMethod().getName())
                    .replace("{class}", context.getTarget().getClass().getSuperclass().getName())
                    .replace("{time}", String.valueOf(time));

            LOGGER.info(message);
        }
    }
}