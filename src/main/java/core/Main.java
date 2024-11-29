package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        List<String> fragments = readNumbersFromFile("src/main/resources/fragments.txt");

        Map<String, List<String>> connections = prepareConnections(fragments);

        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Future<List<String>>> futures = new ArrayList<>();
        for (String fragment : fragments) {
            futures.add(executor.submit(() -> {
                Set<String> used = new HashSet<>();
                List<String> currentChain = new ArrayList<>();
                currentChain.add(fragment);
                used.add(fragment);
                List<String> longestChain = new ArrayList<>();
                buildChainWithMap(connections, used, currentChain, longestChain);
                return longestChain;
            }));
        }

        List<String> longestChain = new ArrayList<>();
        for (Future<List<String>> future : futures) {
            List<String> chain = future.get();
            if (chain.size() > longestChain.size()) {
                longestChain = chain;
            }
        }
        executor.shutdown();

        System.out.println("Найдовший ланцюжок: " + String.join("", longestChain));
        System.out.println("Довжина ланцюжка: " + longestChain.size());
    }

    private static List<String> readNumbersFromFile(String filename) throws IOException {
        List<String> fragments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fragments.add(line.trim());
            }
        }
        return fragments;
    }

    private static void buildChainWithMap(
            Map<String, List<String>> connections,
            Set<String> used,
            List<String> currentChain,
            List<String> longestChain
    ) {
        String lastFragment = currentChain.get(currentChain.size() - 1);
        String lastSuffix = lastFragment.substring(lastFragment.length() - 2);

        List<String> nextFragments = connections.getOrDefault(lastSuffix, Collections.emptyList());

        for (String nextFragment : nextFragments) {
            if (!used.contains(nextFragment)) {
                used.add(nextFragment);
                currentChain.add(nextFragment);

                buildChainWithMap(connections, used, currentChain, longestChain);

                currentChain.remove(currentChain.size() - 1);
                used.remove(nextFragment);
            }
        }

        if (currentChain.size() > longestChain.size()) {
            longestChain.clear();
            longestChain.addAll(currentChain);
        }
    }

    private static Map<String, List<String>> prepareConnections(List<String> fragments) {
        Map<String, List<String>> connections = new HashMap<>();
        for (String fragment : fragments) {
            String prefix = fragment.substring(0, 2);
            connections.computeIfAbsent(prefix, k -> new ArrayList<>()).add(fragment);
        }
        return connections;
    }
}