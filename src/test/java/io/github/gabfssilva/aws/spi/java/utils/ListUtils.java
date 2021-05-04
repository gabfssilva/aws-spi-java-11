package io.github.gabfssilva.aws.spi.java.utils;

import software.amazon.awssdk.utils.Pair;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ListUtils {
    public static <T> List<Pair<Integer, T>> zipWithIndex(List<T> list) {
        return IntStream.range(0, list.size())
                .boxed()
                .map(i -> Pair.of(i, list.get(i)))
                .collect(Collectors.toList());
    }
}
