package com.example.yoyo_data.util;

import org.apache.poi.ss.formula.functions.T;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    public static int size(Map<?, ?> map) {
        return map == null ? 0 : map.size();
    }

    public static int size(Object[] array) {
        return array == null ? 0 : array.length;
    }

    public static <T> List<T> emptyList() {
        return Collections.emptyList();
    }

    public static <T> Set<T> emptySet() {
        return Collections.emptySet();
    }

    public static <K, V> Map<K, V> emptyMap() {
        return Collections.emptyMap();
    }

    public static <T> List<T> newArrayList() {
        return new ArrayList<>();
    }

    public static <T> List<T> newArrayList(int initialCapacity) {
        return new ArrayList<>(initialCapacity);
    }

    public static <T> List<T> newArrayList(Collection<? extends T> collection) {
        return collection == null ? newArrayList() : new ArrayList<>(collection);
    }

    public static <T> List<T> newArrayList(T... elements) {
        if (elements == null || elements.length == 0) {
            return newArrayList();
        }
        List<T> list = newArrayList(elements.length);
        Collections.addAll(list, elements);
        return list;
    }

    public static <T> Set<T> newHashSet() {
        return new HashSet<>();
    }

    public static <T> Set<T> newHashSet(int initialCapacity) {
        return new HashSet<>(initialCapacity);
    }

    public static <T> Set<T> newHashSet(Collection<? extends T> collection) {
        return collection == null ? newHashSet() : new HashSet<>(collection);
    }

    public static <T> Set<T> newHashSet(T... elements) {
        if (elements == null || elements.length == 0) {
            return newHashSet();
        }
        Set<T> set = newHashSet(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    public static <K, V> Map<K, V> newHashMap() {
        return new HashMap<>();
    }

    public static <K, V> Map<K, V> newHashMap(int initialCapacity) {
        return new HashMap<>(initialCapacity);
    }

    public static <K, V> Map<K, V> newHashMap(Map<? extends K, ? extends V> map) {
        return map == null ? newHashMap() : new HashMap<>(map);
    }

    public static <T> T first(Collection<T> collection) {
        if (isEmpty(collection)) {
            return null;
        }
        return collection.iterator().next();
    }

    public static <T> T last(List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    public static <T> T get(List<T> list, int index) {
        if (isEmpty(list) || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    public static <T> List<T> subList(List<T> list, int fromIndex, int toIndex) {
        if (isEmpty(list)) {
            return newArrayList();
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (toIndex > list.size()) {
            toIndex = list.size();
        }
        if (fromIndex >= toIndex) {
            return newArrayList();
        }
        return new ArrayList<>(list.subList(fromIndex, toIndex));
    }

    public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return newArrayList();
        }
        return collection.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public static <T, R> List<R> map(Collection<T> collection, Function<T, R> mapper) {
        if (isEmpty(collection)) {
            return newArrayList();
        }
        return collection.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    public static <T> List<T> distinct(Collection<T> collection) {
        if (isEmpty(collection)) {
            return newArrayList();
        }
        return collection.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    public static <T> List<T> sort(List<T> list, Comparator<? super T> comparator) {
        if (isEmpty(list)) {
            return newArrayList();
        }
        List<T> result = newArrayList(list);
        result.sort(comparator);
        return result;
    }

    public static <T> List<T> reverse(List<T> list) {
        if (isEmpty(list)) {
            return newArrayList();
        }
        List<T> result = newArrayList(list);
        Collections.reverse(result);
        return result;
    }

    public static <T> List<List<T>> partition(List<T> list, int size) {
        if (isEmpty(list) || size <= 0) {
            return newArrayList();
        }
        List<List<T>> result = newArrayList();
        for (int i = 0; i < list.size(); i += size) {
            result.add(new ArrayList<>(list.subList(i, Math.min(i + size, list.size()))));
        }
        return result;
    }

    public static <T> boolean contains(Collection<T> collection, T element) {
        return isNotEmpty(collection) && collection.contains(element);
    }

    public static <T> boolean containsAny(Collection<T> collection, Collection<T> elements) {
        if (isEmpty(collection) || isEmpty(elements)) {
            return false;
        }
        for (T element : elements) {
            if (collection.contains(element)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean containsAll(Collection<T> collection, Collection<T> elements) {
        return isNotEmpty(collection) && isNotEmpty(elements) && collection.containsAll(elements);
    }

    public static <T> List<T> intersection(Collection<T> coll1, Collection<T> coll2) {
        if (isEmpty(coll1) || isEmpty(coll2)) {
            return newArrayList();
        }
        List<T> result = newArrayList(coll1);
        result.retainAll(coll2);
        return result;
    }

    public static <T> List<T> union(Collection<T> coll1, Collection<T> coll2) {
        if (isEmpty(coll1) && isEmpty(coll2)) {
            return newArrayList();
        }
        if (isEmpty(coll1)) {
            return newArrayList(coll2);
        }
        if (isEmpty(coll2)) {
            return newArrayList(coll1);
        }
        Set<T> set = newHashSet(coll1);
        set.addAll(coll2);
        return newArrayList(set);
    }

    public static <T> List<T> difference(Collection<T> coll1, Collection<T> coll2) {
        if (isEmpty(coll1)) {
            return newArrayList();
        }
        if (isEmpty(coll2)) {
            return newArrayList(coll1);
        }
        List<T> result = newArrayList(coll1);
        result.removeAll(coll2);
        return result;
    }

    public static <T> List<T> addAll(Collection<T> collection, Collection<T> toAdd) {
        List<T> result = newArrayList(collection);
        if (isNotEmpty(toAdd)) {
            result.addAll(toAdd);
        }
        return result;
    }

    public static <T> List<T> removeAll(Collection<T> collection, Collection<T> toRemove) {
        List<T> result = newArrayList(collection);
        if (isNotEmpty(toRemove)) {
            result.removeAll(toRemove);
        }
        return result;
    }

    public static <T> T findFirst(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return null;
        }
        return collection.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    public static <T> List<T> findAll(Collection<T> collection, Predicate<T> predicate) {
        return filter(collection, predicate);
    }

    public static <T> boolean anyMatch(Collection<T> collection, Predicate<T> predicate) {
        return isNotEmpty(collection) && collection.stream().anyMatch(predicate);
    }

    public static <T> boolean allMatch(Collection<T> collection, Predicate<T> predicate) {
        return isNotEmpty(collection) && collection.stream().allMatch(predicate);
    }

    public static <T> boolean noneMatch(Collection<T> collection, Predicate<T> predicate) {
        return isEmpty(collection) || collection.stream().noneMatch(predicate);
    }

    public static <T> T max(Collection<T> collection, Comparator<? super T> comparator) {
        if (isEmpty(collection)) {
            return null;
        }
        return collection.stream().max(comparator).orElse(null);
    }

    public static <T> T min(Collection<T> collection, Comparator<? super T> comparator) {
        if (isEmpty(collection)) {
            return null;
        }
        return collection.stream().min(comparator).orElse(null);
    }

    public static <T> List<T> shuffle(List<T> list) {
        if (isEmpty(list)) {
            return newArrayList();
        }
        List<T> result = newArrayList(list);
        Collections.shuffle(result);
        return result;
    }

    public static <T> List<T> swap(List<T> list, int i, int j) {
        if (isEmpty(list) || i < 0 || j < 0 || i >= list.size() || j >= list.size()) {
            return list;
        }
        List<T> result = newArrayList(list);
        Collections.swap(result, i, j);
        return result;
    }

    public static <T> List<T> rotate(List<T> list, int distance) {
        if (isEmpty(list)) {
            return newArrayList();
        }
        List<T> result = newArrayList(list);
        Collections.rotate(result, distance);
        return result;
    }

    public static <T> List<T> fill(List<T> list, T obj) {
        if (isEmpty(list)) {
            return newArrayList();
        }
        List<T> result = newArrayList(list);
        Collections.fill(result, obj);
        return result;
    }

    public static <T> List<T> nCopies(int n, T obj) {
        if (n <= 0) {
            return newArrayList();
        }
        return new ArrayList<>(Collections.nCopies(n, obj));
    }

    public static <T> List<T> singletonList(T obj) {
        return new ArrayList<>(Collections.singletonList(obj));
    }

    public static <T> Set<T> singletonSet(T obj) {
        return new HashSet<>(Collections.singleton(obj));
    }

    public static <K, V> Map<K, V> singletonMap(K key, V value) {
        return new HashMap<>(Collections.singletonMap(key, value));
    }

    public static <T> String join(Collection<T> collection, String separator) {
        if (isEmpty(collection)) {
            return "";
        }
        return collection.stream()
                .map(Object::toString)
                .collect(Collectors.joining(separator));
    }

    public static <T> String join(Collection<T> collection, String separator, String prefix, String suffix) {
        if (isEmpty(collection)) {
            return "";
        }
        return collection.stream()
                .map(Object::toString)
                .collect(Collectors.joining(separator, prefix, suffix));
    }

    public static <K, V> Map<K, V> toMap(Collection<T> collection, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        if (isEmpty(collection)) {
            return newHashMap();
        }
        return collection.stream()
                .collect(Collectors.toMap(keyMapper, valueMapper, (v1, v2) -> v1));
    }

    public static <K, T> Map<K, List<T>> groupBy(Collection<T> collection, Function<T, K> classifier) {
        if (isEmpty(collection)) {
            return newHashMap();
        }
        return collection.stream()
                .collect(Collectors.groupingBy(classifier));
    }

    public static <T> long count(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return 0;
        }
        return collection.stream()
                .filter(predicate)
                .count();
    }

    public static <T extends Comparable<? super T>> T max(Collection<T> collection) {
        if (isEmpty(collection)) {
            return null;
        }
        return collection.stream().max(Comparator.naturalOrder()).orElse(null);
    }

    public static <T extends Comparable<? super T>> T min(Collection<T> collection) {
        if (isEmpty(collection)) {
            return null;
        }
        return collection.stream().min(Comparator.naturalOrder()).orElse(null);
    }

    public static <T> List<T> flatten(List<List<T>> lists) {
        if (isEmpty(lists)) {
            return newArrayList();
        }
        return lists.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public static <T> List<T> limit(List<T> list, int maxSize) {
        if (isEmpty(list) || maxSize <= 0) {
            return newArrayList();
        }
        return list.stream()
                .limit(maxSize)
                .collect(Collectors.toList());
    }

    public static <T> List<T> skip(List<T> list, int n) {
        if (isEmpty(list) || n <= 0) {
            return newArrayList(list);
        }
        return list.stream()
                .skip(n)
                .collect(Collectors.toList());
    }
}
