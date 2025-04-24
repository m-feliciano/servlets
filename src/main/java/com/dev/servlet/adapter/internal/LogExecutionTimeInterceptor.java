package com.dev.servlet.adapter.internal;

import com.dev.servlet.adapter.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Slf4j
@Interceptor
@LogExecutionTime
public class LogExecutionTimeInterceptor {
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
            log.info(message);
        }
    }
}
