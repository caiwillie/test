package com.brandnewdata.mop.poc.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class CollectorsUtil {

    public static <T> Collector<T,?, List<T>> toSortedList(Comparator<? super T> comparator) {
        return Collectors.collectingAndThen(
                Collectors.toCollection(()->new TreeSet<>(comparator)), ArrayList::new);
    }
}
