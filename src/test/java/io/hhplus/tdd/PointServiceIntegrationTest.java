package io.hhplus.tdd;

import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;


    @Test
    void 충전_100번_테스트() throws InterruptedException {

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        int point = 100;

        for(int i = 0; i< threadCount; i++){
            executorService.submit(() -> {
                try {
                    pointService.charge(1L, point);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        //then
        UserPoint userPoint = pointService.selectById(1L);
        Assertions.assertEquals(threadCount * point, userPoint.point())  ;
    }
}
