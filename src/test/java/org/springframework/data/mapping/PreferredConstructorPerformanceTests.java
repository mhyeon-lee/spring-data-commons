package org.springframework.data.mapping;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.context.SampleMappingContext;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Threads(1000)
@Warmup(iterations = 3)
@Fork(1)
public class PreferredConstructorPerformanceTests {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(PreferredConstructorPerformanceTests.class.getSimpleName())
            .build();

        new Runner(opt).run();
    }

    MappingContext<?, ?> mappingContext = new SampleMappingContext();
    PersistentEntity<?, ?> entity = mappingContext.getRequiredPersistentEntity(Person.class);

    @Setup
    public void setup() {
        // cache
        PersistentEntity<?, ?> entity = mappingContext.getRequiredPersistentEntity(Person.class);
        this.executeIsConstructorParameter(entity, 1, (PreferredConstructor::isConstructorParameter));
        this.executeIsConstructorParameter(entity, 1, (PreferredConstructor::isConstructorParameterWithConcurrencyHashMap));
    }

    @Benchmark
    public void isConstructorParameterReadLock() {
        this.executeIsConstructorParameter(entity, 200, (PreferredConstructor::isConstructorParameter));
    }

    @Benchmark
    public void isConstructorParameterConcurrentHashMap() {
        this.executeIsConstructorParameter(entity, 200, (PreferredConstructor::isConstructorParameterWithConcurrencyHashMap));
    }

    @Benchmark
    public void isConstructorParameterConcurrentHashMapComputeIfAbsent() {
        this.executeIsConstructorParameter(entity, 200, (PreferredConstructor::isConstructorParameterWithConcurrencyHashMapComputeIfAbsent));
    }

    private void executeIsConstructorParameter(
        PersistentEntity<?, ?> entity, int row, BiConsumer<PreferredConstructor<?, ?>, PersistentProperty<?>> isConstructorParameter) {

        for (int i = 0; i < row; i++) {
            PreferredConstructor<?, ?> constructor = entity.getPersistenceConstructor();
            for (PersistentProperty<?> property : entity) {
                isConstructorParameter.accept(constructor, property);   // 15
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class Person {
        @Id
        private String id;
        private String firstName;
        private String lastName;
        private String ssn;
        private int age;
        private Instant birth;
        private boolean adult;
        private String address;
        private String addressDetail;
        private String country;
        private String state;
        private String zipCode;
        private String job;
        private String company;
        private String email;
    }
}
