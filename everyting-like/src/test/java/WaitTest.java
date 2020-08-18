

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 *latch.countDown();对数量进行--操作，await进行阻塞直到i= 0
 */
public class WaitTest {
    private static int COUNT = 5;
    private static CountDownLatch latch = new CountDownLatch(COUNT);
    private static Semaphore semaphore = new Semaphore(0);
    public static void main(String[] args) throws InterruptedException {
        for(int i = 0;i<COUNT;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName());
//                    latch.countDown();
                    semaphore.release();//颁发许可证，无参表示一个
                }
            }).start();
        }
//        latch.await();
        semaphore.acquire(5);//无参代表请求资源为1，请求许可证，请求不到就阻塞
        System.out.println(Thread.currentThread().getName());
    }
}
