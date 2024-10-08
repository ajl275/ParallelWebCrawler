package com.udacity.webcrawler.profiler;

import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.parser.PageParser;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);

    if(!hasAnnotation(klass)) {
      throw new IllegalArgumentException(klass.getName() + " does not contain any profiled methods");
    }


    return (T) Proxy.newProxyInstance(ProfilerImpl.class.getClassLoader(),new Class[]{klass}, new ProfilingMethodInterceptor(clock,state,delegate));
  }

  private boolean hasAnnotation(Class<?> klass) {
    for(Method m : klass.getDeclaredMethods()) {
      if(m.isAnnotationPresent(Profiled.class)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void writeData(Path path) {

    try(BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
      writeData(writer);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
