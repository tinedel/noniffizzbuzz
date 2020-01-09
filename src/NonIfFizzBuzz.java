import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NonIfFizzBuzz {
    public static void main(String[] args) {
        Stream<Optional<String>> fizz = repeat(Optional.empty(), Optional.empty(), Optional.of("Fizz"));
        Stream<Optional<String>> buzz = repeat(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of("Buzz"));
        Stream<Optional<String>> fizzBuzz = zipWith(fizz, buzz, NonIfFizzBuzz::merge);

        Stream<String> numbers = IntStream.range(1, 200).mapToObj(value -> "" + value);

        Stream<String> result = zipWith(numbers, fizzBuzz, (s, s2) -> s2.orElse(s));

        result.forEach(System.out::println);

    }

    private static Optional<String> merge(Optional<String> left, Optional<String> right) {
        final Supplier<Optional<? extends String>> emptyOptional = () -> Optional.of("");
        return left.or(emptyOptional).flatMap(z -> right.or(emptyOptional).map(r -> z + r)).filter(c -> !c.isBlank());
    }

    private static <T, K, V> Stream<V> zipWith(Stream<T> left, Stream<K> right, BiFunction<T, K, V> biFunction) {
        Objects.requireNonNull(biFunction);
        Spliterator<T> aSpliterator = Objects.requireNonNull(left).spliterator();
        Spliterator<K> bSpliterator = Objects.requireNonNull(right).spliterator();

        // Zipping looses DISTINCT and SORTED characteristics
        int characteristics = aSpliterator.characteristics() & bSpliterator.characteristics() &
                ~(Spliterator.DISTINCT | Spliterator.SORTED);

        long zipSize = -1;

        Iterator<T> aIterator = Spliterators.iterator(aSpliterator);
        Iterator<K> bIterator = Spliterators.iterator(bSpliterator);
        Iterator<V> cIterator = new Iterator<V>() {
            @Override
            public boolean hasNext() {
                return aIterator.hasNext() && bIterator.hasNext();
            }

            @Override
            public V next() {
                return biFunction.apply(aIterator.next(), bIterator.next());
            }
        };

        Spliterator<V> split = Spliterators.spliterator(cIterator, zipSize, characteristics);
        return StreamSupport.stream(split, false);
    }

    private static class RepeatingIterator<T> implements Iterator<T> {

        private final T[] repeated;
        int i = 0;

        public RepeatingIterator(T... toRepeat) {
            repeated = toRepeat;
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public T next() {
            T toReturn = repeated[i];
            i = (i + 1) % repeated.length;
            return toReturn;
        }
    }

    private static <T> Stream<T> repeat(T... toRepeat) {
        Iterable<T> iterable = () -> new RepeatingIterator<>(toRepeat);
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
