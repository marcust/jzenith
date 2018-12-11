/**
 * Copyright © 2018 Marcus Thiesen (marcus@thiesen.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jzenith.core;

import com.englishtown.vertx.guice.GuiceVerticleFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.plugins.RxJavaPlugins;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.micrometer.MetricsDomain;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.reactivex.RxHelper;
import lombok.NonNull;
import one.util.streamex.StreamEx;
import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector;
import org.apache.logging.log4j.core.util.Constants;
import org.jzenith.core.configuration.ExtraConfiguration;
import org.jzenith.core.guice.CloseableListener;
import org.jzenith.core.guice.LifeCycleObjectRepository;
import org.jzenith.core.health.HealthCheck;
import org.jzenith.core.metrics.JvmOptionMetrics;
import org.jzenith.core.model.InitResult;
import org.jzenith.core.tracing.OpenTracingInterceptor;
import org.jzenith.core.util.CompletableFutureHandler;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;

public class JZenith {

    static {
        System.setProperty(Constants.LOG4J_CONTEXT_SELECTOR, AsyncLoggerContextSelector.class.getName());
        System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
    }

    // Manually (not Lombok) after static block to ensure that the property for the context selector has been set.
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JZenith.class);

    private final LinkedList<AbstractPlugin> plugins = Lists.newLinkedList();
    private final LinkedList<Module> modules = Lists.newLinkedList();
    private final Map<String, Object> extraConfiguration = Maps.newHashMap();

    private final CoreConfiguration configuration;

    private final LifeCycleObjectRepository repository = new LifeCycleObjectRepository();

    private Tracer tracer;
    private Vertx vertx;

    private JZenith(CoreConfiguration configuration) {
        this.configuration = configuration;
    }

    public static JZenith application(@NonNull String... args) {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> log.error("Uncaught exception", throwable));
        return new JZenith(() -> Arrays.asList(args));
    }

    public JZenith withPlugins(@NonNull AbstractPlugin... plugins) {
        Preconditions.checkArgument(plugins.length > 0, "You need to provide a plugin");

        this.plugins.addAll(Arrays.asList(plugins));

        return this;
    }

    public JZenith withTracer(@NonNull final Tracer tracer) {
        this.tracer = tracer;

        return this;
    }

    public JZenith run() {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        if (log.isDebugEnabled()) {
            log.debug("jZenith starting up");
        }
        if (tracer != null && !GlobalTracer.isRegistered()) {
            GlobalTracer.register(tracer);
        }

        final InitResult initResult = initVertx();
        setupMeterRegistry();

        final Injector injector = createInjector(initResult);
        setVerticleFactoryInjector(initResult.getVertx(), injector);

        startPlugins(initResult.getVertx(), injector);

        final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        log.debug("jZenith startup complete after {}ms ({} has been up for {}ms)",
                stopwatch.elapsed(TimeUnit.MILLISECONDS),
                runtimeMXBean.getVmName(),
                runtimeMXBean.getUptime());

        this.vertx = initResult.getVertx();

        return this;
    }

    private void startPlugins(Vertx vertx, Injector injector) {
        final CompletableFuture[] deploymentResults = plugins.stream()
                .map(plugin -> plugin.start(injector))
                .toArray(CompletableFuture[]::new);

        try {
            CompletableFuture.allOf(deploymentResults)
                    .get();
        } catch (Exception e) {
            vertx.close();
            Throwables.throwIfUnchecked(e);
            throw new JZenithException(e);
        }
    }

    private void setVerticleFactoryInjector(Vertx vertx, Injector injector) {
        StreamEx.of(vertx.verticleFactories())
                .select(GuiceVerticleFactory.class)
                .findFirst()
                .ifPresent(guiceVerticleFactory -> guiceVerticleFactory.setInjector(injector));
    }

    private InitResult initVertx() {
        final VertxPrometheusOptions prometheusOptions = new VertxPrometheusOptions()
                .setEnabled(true);
        final MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions()
                .setEnabled(true)
                .setDisabledMetricsCategories(ImmutableSet.of(MetricsDomain.HTTP_SERVER))
                .setPrometheusOptions(prometheusOptions);

        final Vertx createdVertx = Vertx.vertx(
                new VertxOptions().setMetricsOptions(
                        metricsOptions
                ).setPreferNativeTransport(true)
        );

        if (!createdVertx.isNativeTransportEnabled()) {
            log.warn("Native transport could not be enabled");
        }

        final MeterRegistry meterRegistry = BackendRegistries.setupBackend(metricsOptions).getMeterRegistry();
        checkState(meterRegistry != null, "Meter registry should have been initialized");

        RxJavaPlugins.setComputationSchedulerHandler(s -> RxHelper.scheduler(createdVertx));
        RxJavaPlugins.setIoSchedulerHandler(s -> RxHelper.blockingScheduler(createdVertx));
        RxJavaPlugins.setNewThreadSchedulerHandler(s -> RxHelper.scheduler(createdVertx));

        return new InitResult(createdVertx, meterRegistry);
    }

    public Injector createInjectorForTesting() {
        return createInjector(initVertx());
    }

    private Injector createInjector(@NonNull InitResult initResult) {
        final ImmutableMap.Builder<String, Object> extraConfigurationBuilder = ImmutableMap.builder();
        extraConfigurationBuilder.putAll(this.extraConfiguration);
        plugins.forEach(plugin -> extraConfigurationBuilder.putAll(plugin.getExtraConfiguration()));

        final List<Module> allModules = ImmutableList.<Module>builder()
                .add(new AbstractModule() {
                    @Override
                    protected void configure() {
                        install(new JacksonModule());
                        bind(MeterRegistry.class).toInstance(initResult.getMeterRegistry());
                        bind(CoreConfiguration.class).toInstance(configuration);
                        bind(ExtraConfiguration.class).toInstance(extraConfigurationBuilder.build()::get);
                        bind(Vertx.class).toInstance(initResult.getVertx());
                        bind(io.vertx.reactivex.core.Vertx.class).toInstance(io.vertx.reactivex.core.Vertx.newInstance(initResult.getVertx()));
                        bindListener(Matchers.any(), new CloseableListener(repository));

                        if (tracer != null) {
                            final OpenTracingInterceptor interceptor = new OpenTracingInterceptor(tracer);

                            Stream.of(Single.class, Observable.class, Completable.class, Maybe.class)
                                    .forEach(clz ->
                                            bindInterceptor(Matchers.any(), Matchers.returns(Matchers.subclassesOf(clz)), interceptor));
                            bind(Tracer.class).toInstance(tracer);
                        }

                        Multibinder.newSetBinder(binder(), HealthCheck.class);
                    }
                })
                .addAll(plugins.stream().flatMap(plugin -> plugin.getModules().stream()).collect(ImmutableList.toImmutableList()))
                .addAll(modules)
                .build();

        return Guice.createInjector(allModules);
    }

    private void setupMeterRegistry() {
        final MeterRegistry registry = BackendRegistries.getDefaultNow();

        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        try {
            new JvmOptionMetrics().bindTo(registry);
        } catch (IllegalArgumentException e) {
            log.debug("Not running on HotSpot");
        }
    }

    @SafeVarargs
    public final JZenith withModules(@NonNull Module... modules) {
        this.modules.addAll(Arrays.asList(modules));

        return this;
    }

    public JZenith withConfiguration(@NonNull String name, @NonNull Object value) {
        this.extraConfiguration.put(name, value);

        return this;
    }

    public JZenith withConfiguration(@NonNull String name, int value) {
        this.extraConfiguration.put(name, String.valueOf(value));

        return this;
    }

    public void stop() {
        final CompletableFutureHandler<Void> completableFutureHandler = new CompletableFutureHandler<>();
        if (vertx != null) {
            vertx.close(completableFutureHandler.handler());

            try {
                completableFutureHandler.get();
            } catch (Exception e) {
                Throwables.throwIfUnchecked(e);
                throw new JZenithException(e);
            }
        }

        repository.closeAll();
    }
}
