import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class Generator {
    private static final AtomicInteger sumCriterionOne = new AtomicInteger(0);
    private static final AtomicInteger sumCriterionTwo = new AtomicInteger(0);
    private static final AtomicInteger sumCriterionTree = new AtomicInteger(0);

    public static void main(String[] args) {
        Random random = new Random();
        String[] texts = new String[100_000];
        for (int i = 0; i < texts.length; i++) {
            texts[i] = generateText("abc", 3 + random.nextInt(3));
        }

        Predicate<String> criterion = (t) -> {
            if (t.length() == 4) {
                if (t.startsWith(String.valueOf(t.charAt(0) + t.charAt(1))) ==
                        t.endsWith(String.valueOf(t.charAt(0) + t.charAt(1)))) {
                    return true;
                }
            }

            char[] simvols = t.toCharArray();
            int check = 0;
            for (int i = 1; i < simvols.length; i++) {
                if (simvols[i - 1] == simvols[i]) {
                    check = 1;
                } else check = 0;
            }
            if (check == 1) return true;

            for (int i = 1; i < simvols.length; i++) {
                if (simvols[i - 1] < simvols[i]) {
                    check = 1;
                } else check = 0;
            }
            return check == 1;
        };

        Thread threadOne = new Thread(() -> runSum(texts, 3, criterion, sumCriterionOne));

        Thread threadTwo = new Thread(() -> runSum(texts, 4, criterion, sumCriterionTwo));

        Thread threadTree = new Thread(() -> runSum(texts, 5, criterion, sumCriterionTree));

        List<Thread> threads = List.of(threadOne, threadTwo, threadTree);
        threads.forEach(Thread::start);
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        System.out.printf("""
                        Красивых слов с длиной 3: %s шт
                        Красивых слов с длиной 4: %s шт
                        Красивых слов с длиной 5: %s шт
                        """,
                sumCriterionOne, sumCriterionTwo, sumCriterionTree);
    }

    private static void runSum(String[] texts, int x, Predicate<String> criterion, AtomicInteger integer) {
        for (String text : texts) {
            if (text.length() == x) {
                if (criterion.test(text)) {
                    integer.incrementAndGet();
                }
            }
        }
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}
