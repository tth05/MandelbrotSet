package com.github.tth05.mandelbrotset;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Settings {

    public static int iterations = 1000;
    public static int width = 1000;
    public static int height = 1000;
    public static int numThreads = 400;

    private static ThreadPoolExecutor EXECUTOR_SERVICE = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);

    public static int getSubSquareSize() {
        return (int) Math.round(Math.sqrt((double) (width * height) / numThreads));
    }

    public static ExecutorService getExecutorService() {
        if (EXECUTOR_SERVICE.getMaximumPoolSize() != numThreads) {
            EXECUTOR_SERVICE.shutdownNow();
            EXECUTOR_SERVICE = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
        }
        return EXECUTOR_SERVICE;
    }
}
