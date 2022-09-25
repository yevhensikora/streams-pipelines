package com.efimchick.ifmo;


import com.efimchick.ifmo.util.CourseResult;
import com.efimchick.ifmo.util.Person;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.efimchick.ifmo.CollectingConstants.A;
import static com.efimchick.ifmo.CollectingConstants.B;
import static com.efimchick.ifmo.CollectingConstants.C;
import static com.efimchick.ifmo.CollectingConstants.D;
import static com.efimchick.ifmo.CollectingConstants.E;
import static com.efimchick.ifmo.CollectingConstants.F;
import static com.efimchick.ifmo.CollectingConstants.TASK_NOT_FOUND;

public class Collecting {
    public int sum(IntStream intStream) {
        return intStream.sum();
    }

    public int production(IntStream intStream) {
        return intStream.reduce((left, right) -> left * right)
                .orElse(0);
    }

    public int oddSum(IntStream intStream) {
        return intStream.filter(this::isOdd).sum();
    }

    public Map<Integer, Integer> sumByRemainder(int remainder, IntStream intStream) {
        return intStream.boxed()
                .collect(Collectors.groupingBy(i -> i % remainder, Collectors.summingInt(i -> i)));
    }

    public Map<Person, Double> totalScores(Stream<CourseResult> courseResultStream) {
        List<CourseResult> courseResults = courseResultStream.collect(Collectors.toList());
        long numberOfTasks = getNumberOfTasks(courseResults);

        return courseResults.stream()
                .collect(Collectors.toMap(CourseResult::getPerson,
                        c -> c.getTaskResults()
                                .values()
                                .stream()
                                .mapToInt(value -> value)
                                .sum() / (double) numberOfTasks));
    }

    public double averageTotalScore(Stream<CourseResult> courseResultStream) {
        List<CourseResult> courseResults = courseResultStream.collect(Collectors.toList());
        long numberOfTasks = getNumberOfTasks(courseResults);

        long numberOfPerson = getNumberOfPerson(courseResults);

        long allNumberOfResults = numberOfPerson * numberOfTasks;


        return courseResults.stream()
                .map(CourseResult::getTaskResults)
                .flatMapToDouble(taskResult -> taskResult.values().stream().mapToDouble(value -> value))
                .sum() / allNumberOfResults;
    }

    public Map<String, Double> averageScoresPerTask(Stream<CourseResult> courseResultStream) {
        List<CourseResult> courseResultList = courseResultStream.collect(Collectors.toList());
        long numberOfPerson = getNumberOfPerson(courseResultList);

        return courseResultList.stream()
                .flatMap(courseResult -> courseResult.getTaskResults().entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.summingDouble(value -> value.getValue() / (double) numberOfPerson)));
    }

    public Map<Person, String> defineMarks(Stream<CourseResult> courseResultStream) {
        List<CourseResult> courseResultList = courseResultStream.collect(Collectors.toList());

        return courseResultList.stream().collect(Collectors.toMap(CourseResult::getPerson, courseResult ->
                defineScore(averageResult(courseResult, courseResultList))));
    }

    public String easiestTask(Stream<CourseResult> courseResultStream) {
        List<CourseResult> courseResultList = courseResultStream.collect(Collectors.toList());

        return courseResultList.stream()
                .flatMap(courseResult -> courseResult.getTaskResults().entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingDouble(Map.Entry::getValue)))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(TASK_NOT_FOUND);
    }

    public Collector printableStringCollector() {
        return new Collector() {
            @Override
            public Supplier<Table> supplier() {
                return Table::new;
            }

            @Override
            public BiConsumer<Table, CourseResult> accumulator() {
                return Table::addCourseResult;
            }

            @Override
            public BinaryOperator<Table> combiner() {
                return null;
            }

            @Override
            public Function<Table, String> finisher() {
                return Table -> {
                    StringBuilder sb = new StringBuilder();
                    Table.createTable(sb);
                    return sb.toString();
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
    }

    private double averageResult(CourseResult courseResult, List<CourseResult> courseResultList) {
        return courseResult.getTaskResults().values().stream()
                .mapToDouble(value -> value)
                .sum() / getNumberOfTasks(courseResultList);
    }

    private long getNumberOfTasks(List<CourseResult> courseResultList) {
        return courseResultList.stream()
                .flatMap(courseResult -> courseResult.getTaskResults().keySet().stream())
                .distinct()
                .count();
    }

    private long getNumberOfPerson(List<CourseResult> courseResultList) {
        return courseResultList.stream()
                .map(CourseResult::getPerson)
                .distinct()
                .count();
    }

    public String defineScore(double score) {
        if (score > 90) {
            return A;
        } else if (score >= 83) {
            return B;
        } else if (score >= 75) {
            return C;
        } else if (score >= 68) {
            return D;
        } else if (score >= 60) {
            return E;
        } else {
            return F;
        }
    }

    private boolean isOdd(int number) {
        return number % 2 != 0;
    }
}