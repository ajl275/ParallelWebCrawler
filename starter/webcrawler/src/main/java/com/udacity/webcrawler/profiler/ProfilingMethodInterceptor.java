package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final ProfilingState state;
  private final Object delegate;

  ProfilingMethodInterceptor(Clock clock, ProfilingState state, Object delegate) {
    this.state = Objects.requireNonNull(state);
    this.clock = Objects.requireNonNull(clock);
    this.delegate = Objects.requireNonNull(delegate);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    Object result = null;
    boolean isProfiled = method.isAnnotationPresent(Profiled.class);
    Instant start = null;

    if(isProfiled) {
      start = clock.instant();
    }

    try{
      if (method.getName().equals("equals") && method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(Object.class)) {
        result = delegate.equals(args[0]);
      } else {
        result = method.invoke(delegate, args);
      }
    } catch (InvocationTargetException e) {
        throw e.getCause();
    }catch (IllegalAccessException e) {
        throw new RuntimeException(e);
    } finally {
        if(isProfiled){
          state.record(delegate.getClass(),method, Duration.between(start,clock.instant()));
        }
    }

    return result;
  }
}
