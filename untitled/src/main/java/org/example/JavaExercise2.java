package org.example;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;

public class JavaExercise2 {
    private static class CachingPrimeChecker {
        private final Map<Long, Boolean> cache = new ConcurrentHashMap<>();

        public boolean isPrime(final long x) {
            return cache.computeIfAbsent(x, this::computeIfIsPrime);
        }

        private boolean computeIfIsPrime(long x) {
            final String currentThreadName = Thread.currentThread().getName();
            System.out.printf("\t[%s] Running computation for: %d%n", currentThreadName, x);
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (x < 2) {
                return false;
            }
            for (long i = 2; i * i <= x; i++) {
                if (x % i == 0) {
                    return false;
                }
            }
            return true;
        }
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        CyclicBarrier barrier = new CyclicBarrier(4);
        CachingPrimeChecker primeChecker = new CachingPrimeChecker();
        Scanner scanner = new Scanner(System.in);

        boolean isRunning = true;

        while (isRunning) {
            String[] inputs = new String[4];
            for (int i = 0; i < 4; i++) {
                System.out.print("Enter a number: ");
                inputs[i] = scanner.next();
                if ("q".equalsIgnoreCase(inputs[i])) {
                    isRunning = false;
                    executorService.shutdown();
                    return;
                }
            }

            Future<Boolean>[] futures = new Future[4];
            for (int i = 0; i < 4; i++) {
                long number = Long.parseLong(inputs[i]);
                futures[i] = executorService.submit(() -> {
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                    return primeChecker.isPrime(number);
                });
            }

            for (int i = 0; i < 4; i++) {
                try {
                    boolean isPrime = futures[i].get();
                    System.out.printf("Number %d is prime: %b%n", Long.parseLong(inputs[i]), isPrime);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}