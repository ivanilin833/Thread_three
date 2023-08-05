import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class Generator {
    private static final BlockingQueue<String> blockingQueueWithA = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<String> blockingQueueWithB = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<String> blockingQueueWithC = new ArrayBlockingQueue<>(100);

    public static void main(String[] args) throws InterruptedException {
        AtomicReference<String> maxA = new AtomicReference<>("");
        AtomicReference<String> maxB = new AtomicReference<>("");
        AtomicReference<String> maxC = new AtomicReference<>("");
        int a = 0;
        int b = 0;
        int c = 0;

        Thread th = new Thread(() -> {
            for (int i = 0; i < 10_000; i++) {
                String text = generateText("abc", 100_000);
                try {
                    blockingQueueWithA.put(text);
                    blockingQueueWithB.put(text);
                    blockingQueueWithC.put(text);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                blockingQueueWithA.put("END");
                blockingQueueWithB.put("END");
                blockingQueueWithC.put("END");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        });
        th.start();


        Thread threadMaxA = new Thread(() -> search(blockingQueueWithA, maxA, a, 'a'));

        Thread threadMaxB = new Thread(() -> search(blockingQueueWithB, maxB, b, 'b'));
        Thread threadMaxC = new Thread(() -> search(blockingQueueWithC, maxC, c, 'c'));

        List<Thread> threads = List.of(threadMaxA, threadMaxB, threadMaxC);
        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Строка с максимальным количесвтом символов 'a'\n" + maxA);
        System.out.println();
        System.out.println("Строка с максимальным количесвтом символов 'b'\n" + maxB);
        System.out.println();
        System.out.println("Строка с максимальным количесвтом символов 'c'\n" + maxC);
        System.out.println();
    }

    private static void search(BlockingQueue<String> queue, AtomicReference<String> max, int sum, char simvol) {
        String text = "";
        while (!text.equals("END")) {
            try {
                text = queue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            int newSum = (int) text.chars().filter((ch) -> ch == simvol).count();
            if (newSum > sum) {
                sum = newSum;
                max.set(text);
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
