package task;

import java.io.File;
import java.io.FileReader;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FileScanner {

    /**
     * 参数1：表示核心线程数
     * 参数2：最大线程数，有新任务并且当前线程数小于最大线程数的时候就会创建新的线程处理任务
     * 参数3+4：表示超过这个时间，除了核心线程数的其他线程都会关闭
     * 参数5：阻塞队列
     * 参数6：处理策略：超出队列的长度，任务处理的方式。1.AbortPolicy：默认方式，处理不过来，就会抛出异常2.CallerRunsPolicy主线程自己去执行这个任务
     * 3.DiscardOldestPolicy()丢掉最老的任务，最后尝试加入
     * 4.DiscardPolicy直接丢掉最新的任务
     */
    //private ThreadPoolExecutor pool = new ThreadPoolExecutor(3, 3, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.AbortPolicy());

    private ExecutorService pool = Executors.newFixedThreadPool(4);
    //方法1
    private Object lock = new Object();
    //方法2
    private CountDownLatch latch = new CountDownLatch(1);
    //方法3
    private Semaphore semaphore = new Semaphore(0);
    private volatile AtomicInteger count = new AtomicInteger();


    private ScanCallback callback;
    public FileScanner(ScanCallback callback) {
        this.callback = callback;
    }


    /**
     * 进行文件扫描：多线程扫描
     *
     * @param path
     */
    public void scan(String path) {
        count.incrementAndGet();
        File root = new File(path);
        doScan(root);
    }

    private void doScan(File root) {
        callback.callback(root);
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    File[] files = root.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.isDirectory()) {
                                //System.out.println("文件夹" + file.getPath());
                                count.incrementAndGet();//记录线程数
                                doScan(file);
                            } else{
                                //System.out.println("文件" + file.getPath());
                            }
                        }
                    }
                }finally {//不管是否出现异常还是能进行减操作
                    int r = count.decrementAndGet();
                    if (r == 0) {
                        //1
//                        synchronized (lock) {
//                            lock.notify();
//                        }
                        //2
//                        latch.countDown();
                        //3
                        semaphore.release();
                    }
                }
            }
        });
    }

    /**
     * 关闭线程池
     */
    public void shutdown(){
        pool.shutdownNow();//内部通过thread.interrupt()来中断
    }
    /**
     * 等待扫描任务结束
     * join()
     * wait()
     */
    public void waitFinish() throws InterruptedException {
        //1
//        synchronized (lock) {
//            lock.wait();
//        }
        //2
//        latch.await();
        //3
        try{
            semaphore.acquire();
        }finally {
            pool.shutdown();
        }
        shutdown();
    }
}
