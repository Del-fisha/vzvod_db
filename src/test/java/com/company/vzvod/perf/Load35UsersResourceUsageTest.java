package com.company.vzvod.perf;

import com.company.vzvod.entity.User;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Нагрузочный тест: 35 активных юзеров + печать RAM/CPU в консоль")
@EnabledIfSystemProperty(named = "runLoadTest", matches = "true")
public class Load35UsersResourceUsageTest {

    private static final int DEFAULT_ACTIVE_USERS = 35;
    private static final Duration DEFAULT_DURATION = Duration.ofSeconds(30);
    private static final Duration DEFAULT_WARMUP = Duration.ofSeconds(5);
    private static final Duration DEFAULT_METRICS_PERIOD = Duration.ofSeconds(1);

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Test
    void load35Users_printRamAndCpuToConsole() throws InterruptedException, ExecutionException {
        int users = Integer.getInteger("loadTest.users", DEFAULT_ACTIVE_USERS);
        Duration duration = Duration.ofMillis(Long.getLong("loadTest.durationMs", DEFAULT_DURATION.toMillis()));
        Duration warmup = Duration.ofMillis(Long.getLong("loadTest.warmupMs", DEFAULT_WARMUP.toMillis()));
        Duration metricsPeriod = Duration.ofMillis(Long.getLong("loadTest.metricsPeriodMs", DEFAULT_METRICS_PERIOD.toMillis()));

        long pid = ProcessHandle.current().pid();
        System.out.println("[нагрузка] старт"
                + " активныхПользователей=" + users
                + " прогревМс=" + warmup.toMillis()
                + " длительностьМс=" + duration.toMillis()
                + " периодМетрикМс=" + metricsPeriod.toMillis()
                + " pid=" + pid);

        AtomicBoolean stop = new AtomicBoolean(false);
        AtomicLong totalOps = new AtomicLong(0);
        AtomicLong totalErrors = new AtomicLong(0);

        ExecutorService pool = Executors.newFixedThreadPool(users);
        ScheduledExecutorService metrics = Executors.newSingleThreadScheduledExecutor();

        ResourceSampler sampler = new ResourceSampler(metricsPeriod, pid);
        metrics.scheduleAtFixedRate(() -> {
            try {
                sampler.sampleAndPrint(totalOps.get(), totalErrors.get());
            } catch (Exception e) {
                // не валим прогон из-за метрик
                e.printStackTrace(System.out);
            }
        }, 0, metricsPeriod.toMillis(), TimeUnit.MILLISECONDS);

        // прогрев
        sampler.setPhase("прогрев");
        runLoadFor(warmup, users, pool, stop, totalOps, totalErrors);

        // основной прогон
        totalOps.set(0);
        totalErrors.set(0);
        sampler.resetPeaks();
        sampler.setPhase("нагрузка");
        runLoadFor(duration, users, pool, stop, totalOps, totalErrors);

        stop.set(true);
        pool.shutdownNow();
        metrics.shutdownNow();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        metrics.awaitTermination(10, TimeUnit.SECONDS);

        sampler.printSummary(totalOps.get(), totalErrors.get());
    }

    private void runLoadFor(
            Duration runFor,
            int users,
            ExecutorService pool,
            AtomicBoolean stop,
            AtomicLong totalOps,
            AtomicLong totalErrors
    ) throws InterruptedException, ExecutionException {
        stop.set(false);
        Instant end = Instant.now().plus(runFor);

        List<Callable<Void>> tasks = new ArrayList<>(users);
        for (int i = 0; i < users; i++) {
            tasks.add(() -> {
                while (!stop.get() && Instant.now().isBefore(end)) {
                    try {
                        systemAuthenticator.begin();
                        doOneUserOperation();
                        totalOps.incrementAndGet();
                    } catch (Exception e) {
                        totalErrors.incrementAndGet();
                    } finally {
                        try {
                            systemAuthenticator.end();
                        } catch (Exception ignored) {
                            // ignore
                        }
                    }
                }
                return null;
            });
        }

        List<Future<Void>> futures = pool.invokeAll(tasks);
        for (Future<Void> f : futures) {
            f.get();
        }
    }

    private void doOneUserOperation() {
        User u = dataManager.create(User.class);
        PreTestEntities.updateUser(u);
        // PreTestEntities использует currentTimeMillis для username — под параллельной нагрузкой
        // это может коллидировать. Для нагрузочного теста делаем гарантированно уникальным.
        u.setUsername("u_" + UUID.randomUUID());
        User saved = dataManager.save(u);
        UUID id = saved.getId();
        dataManager.load(User.class).id(id).one();
        dataManager.remove(saved);
    }

    private static final class ResourceSampler {
        private final long periodMs;
        private final int cores = Runtime.getRuntime().availableProcessors();
        private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        private final long pid;
        private final boolean isWindows;
        private volatile String phase = "нагрузка";

        private volatile long lastWallNs = 0L;
        private volatile long lastCpuNs = 0L;

        private volatile double peakCpuPercent = 0.0;
        private volatile long peakHeapUsedBytes = 0L;
        private volatile long peakNonHeapUsedBytes = 0L;
        private volatile long peakWorkingSetBytes = 0L;
        private volatile long peakPrivateBytes = 0L;

        private final AtomicLong samples = new AtomicLong(0);
        private volatile double sumCpuPercent = 0.0;
        private volatile long sumHeapUsedBytes = 0L;
        private volatile long sumNonHeapUsedBytes = 0L;
        private volatile long sumWorkingSetBytes = 0L;
        private volatile long sumPrivateBytes = 0L;

        ResourceSampler(Duration period, long pid) {
            this.periodMs = Math.max(200, period.toMillis());
            this.pid = pid;
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            this.isWindows = os.contains("win");
        }

        void setPhase(String phase) {
            this.phase = phase;
            // чтобы на смене фазы напечатать шапку ещё раз
            this.lastWallNs = 0L;
            this.lastCpuNs = 0L;
            this.samples.set(0);
        }

        void resetPeaks() {
            peakCpuPercent = 0.0;
            peakHeapUsedBytes = 0L;
            peakNonHeapUsedBytes = 0L;
            peakWorkingSetBytes = 0L;
            peakPrivateBytes = 0L;
            lastWallNs = 0L;
            lastCpuNs = 0L;
            samples.set(0);
            sumCpuPercent = 0.0;
            sumHeapUsedBytes = 0L;
            sumNonHeapUsedBytes = 0L;
            sumWorkingSetBytes = 0L;
            sumPrivateBytes = 0L;
        }

        void sampleAndPrint(long ops, long errors) {
            long nowWallNs = System.nanoTime();
            long nowCpuNs = getProcessCpuTimeNs();

            double cpuPercent = Double.NaN;
            if (lastWallNs != 0L && lastCpuNs != 0L) {
                long wallDelta = nowWallNs - lastWallNs;
                long cpuDelta = nowCpuNs - lastCpuNs;
                if (wallDelta > 0) {
                    cpuPercent = (cpuDelta / (double) wallDelta) * 100.0 / Math.max(1, cores);
                }
            }
            lastWallNs = nowWallNs;
            lastCpuNs = nowCpuNs;

            MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeap = memoryMXBean.getNonHeapMemoryUsage();
            long heapUsed = heap.getUsed();
            long nonHeapUsed = nonHeap.getUsed();

            OsMemory mem = readOsMemory();
            long workingSet = mem.workingSetBytes;
            long privateBytes = mem.privateBytes;

            if (!Double.isNaN(cpuPercent)) {
                peakCpuPercent = Math.max(peakCpuPercent, cpuPercent);
                sumCpuPercent += cpuPercent;
            }
            peakHeapUsedBytes = Math.max(peakHeapUsedBytes, heapUsed);
            peakNonHeapUsedBytes = Math.max(peakNonHeapUsedBytes, nonHeapUsed);
            if (workingSet >= 0) {
                peakWorkingSetBytes = Math.max(peakWorkingSetBytes, workingSet);
                sumWorkingSetBytes += workingSet;
            }
            if (privateBytes >= 0) {
                peakPrivateBytes = Math.max(peakPrivateBytes, privateBytes);
                sumPrivateBytes += privateBytes;
            }
            sumHeapUsedBytes += heapUsed;
            sumNonHeapUsedBytes += nonHeapUsed;

            long n = samples.incrementAndGet();
            if (n == 1) {
                System.out.println("[нагрузка] " + phase + " |"
                        + " # | операций | ошибок | cpu%  | heapМб | nonHeapМб | workingSetМб | privateМб");
            }
            System.out.println("[нагрузка] " + padRight(phase, 7) + " |"
                    + " " + padLeft(Long.toString(n), 2)
                    + " | " + padLeft(Long.toString(ops), 7)
                    + " | " + padLeft(Long.toString(errors), 5)
                    + " | " + padLeft(Double.isNaN(cpuPercent) ? "н/д" : format1(cpuPercent), 5)
                    + " | " + padLeft(Long.toString(bytesToMb(heapUsed)), 6)
                    + " | " + padLeft(Long.toString(bytesToMb(nonHeapUsed)), 9)
                    + " | " + padLeft(workingSet < 0 ? "н/д" : Long.toString(bytesToMb(workingSet)), 12)
                    + " | " + padLeft(privateBytes < 0 ? "н/д" : Long.toString(bytesToMb(privateBytes)), 8));
        }

        void printSummary(long ops, long errors) {
            long n = Math.max(1, samples.get());
            String avgCpu = (n > 1 && sumCpuPercent > 0.0) ? format1(sumCpuPercent / (double) Math.max(1, n - 1)) : "н/д";
            long avgHeapMb = bytesToMb(sumHeapUsedBytes / n);
            long avgNonHeapMb = bytesToMb(sumNonHeapUsedBytes / n);
            String avgWsMb = (sumWorkingSetBytes > 0) ? Long.toString(bytesToMb(sumWorkingSetBytes / n)) : "н/д";
            String avgPrivateMb = (sumPrivateBytes > 0) ? Long.toString(bytesToMb(sumPrivateBytes / n)) : "н/д";

            System.out.println("[нагрузка] итог"
                    + " операций=" + ops
                    + " ошибок=" + errors
                    + " пикCpu%=" + format1(peakCpuPercent)
                    + " средCpu%=" + avgCpu
                    + " пикHeapМб=" + bytesToMb(peakHeapUsedBytes)
                    + " средHeapМб=" + avgHeapMb
                    + " пикNonHeapМб=" + bytesToMb(peakNonHeapUsedBytes)
                    + " средNonHeapМб=" + avgNonHeapMb
                    + " пикWorkingSetМб=" + (peakWorkingSetBytes == 0 ? "н/д" : bytesToMb(peakWorkingSetBytes))
                    + " средWorkingSetМб=" + avgWsMb
                    + " пикPrivateМб=" + (peakPrivateBytes == 0 ? "н/д" : bytesToMb(peakPrivateBytes))
                    + " средPrivateМб=" + avgPrivateMb
                    + " периодМетрикМс=" + periodMs);
        }

        private static long getProcessCpuTimeNs() {
            // com.sun.management is present on most JDKs; fall back to 0 if unavailable
            try {
                com.sun.management.OperatingSystemMXBean os =
                        (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                return os.getProcessCpuTime();
            } catch (Throwable t) {
                return 0L;
            }
        }

        private OsMemory readOsMemory() {
            if (isWindows) {
                return readWindowsProcessMemory(pid);
            }
            return readUnixProcessMemory(pid);
        }

        private static OsMemory readWindowsProcessMemory(long pid) {
            // Working Set: сколько физической памяти сейчас занимает процесс.
            // Private: приватная память процесса (без разделяемых страниц), ближе к "реально съедено процессом".
            // Реализовано через powershell, чтобы не тащить JNA/нативные либы.
            try {
                long workingSet = runAndParseLong(new String[]{
                        "powershell", "-NoProfile", "-Command",
                        "(Get-Process -Id " + pid + " | Select-Object -ExpandProperty WorkingSet64)"
                });
                long priv = runAndParseLong(new String[]{
                        "powershell", "-NoProfile", "-Command",
                        "(Get-Process -Id " + pid + " | Select-Object -ExpandProperty PrivateMemorySize64)"
                });
                return new OsMemory(workingSet, priv);
            } catch (Exception e) {
                return OsMemory.NA;
            }
        }

        private static OsMemory readUnixProcessMemory(long pid) {
            // Linux: /proc доступен почти всегда. Mac: попробуем ps (если нет /proc).
            try {
                java.nio.file.Path p = java.nio.file.Paths.get("/proc/self/statm");
                if (java.nio.file.Files.exists(p)) {
                    String s = java.nio.file.Files.readString(p).trim();
                    String[] parts = s.split("\\s+");
                    if (parts.length >= 2) {
                        long pageSize = 4096L;
                        try {
                            pageSize = (Long) Class.forName("sun.misc.Unsafe")
                                    .getMethod("pageSize")
                                    .invoke(null);
                        } catch (Throwable ignored) {
                            // keep default
                        }
                        long residentPages = Long.parseLong(parts[1]);
                        long rssBytes = residentPages * pageSize;
                        return new OsMemory(rssBytes, -1);
                    }
                }
            } catch (Exception ignored) {
            }
            try {
                long rssKb = runAndParseLong(new String[]{"ps", "-o", "rss=", "-p", Long.toString(pid)});
                return new OsMemory(rssKb * 1024L, -1);
            } catch (Exception e) {
                return OsMemory.NA;
            }
        }

        private static long runAndParseLong(String[] cmd) throws Exception {
            Process p = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start();
            byte[] out = p.getInputStream().readAllBytes();
            p.waitFor(5, TimeUnit.SECONDS);
            String s = new String(out).trim();
            // в powershell иногда могут проскочить пустые строки
            String[] lines = s.split("\\R+");
            String last = lines.length == 0 ? "" : lines[lines.length - 1].trim();
            if (last.isEmpty()) {
                throw new IllegalStateException("empty output");
            }
            return Long.parseLong(last);
        }

        private static long bytesToMb(long bytes) {
            return bytes / (1024L * 1024L);
        }

        private static String format1(double v) {
            return String.format(Locale.US, "%.1f", v);
        }

        private static String padLeft(String s, int width) {
            if (s.length() >= width) return s;
            return " ".repeat(width - s.length()) + s;
        }

        private static String padRight(String s, int width) {
            if (s.length() >= width) return s;
            return s + " ".repeat(width - s.length());
        }
    }

    private record OsMemory(long workingSetBytes, long privateBytes) {
        static final OsMemory NA = new OsMemory(-1, -1);
    }
}

