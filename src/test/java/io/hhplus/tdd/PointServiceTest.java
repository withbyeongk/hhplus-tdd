package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @InjectMocks
    private PointService pointService;
    @Mock
    private UserPointTable userPointTable;
    @Mock
    private PointHistoryTable pointHistoryTable;

    @Test
    @DisplayName("id로 포인트 조회")
    void 포인트_조회() {
        long id = 1L;
        long amount = 100L;
        long updateMillis = 0L;

        UserPoint userPoint = new UserPoint(id, amount, updateMillis);

        doReturn(new UserPoint(id, amount, updateMillis)).when(userPointTable).selectById(id);

        UserPoint returnUserPoint = pointService.selectById(id);

        assertEquals(returnUserPoint.id(), userPoint.id());
        assertEquals(returnUserPoint.point(), userPoint.point());
        assertEquals(returnUserPoint.updateMillis(), userPoint.updateMillis());
    }

    @Test
    @DisplayName("특정 id에 포인트 충전 시 내역이 저장되고 포인트도 충전된다.")
    void 포인트_충전() {

        long id = 1L;
        long beforeAmount = 100L;
        long chargeAmount = 50L;
        long beforeUpdateMillis = 0L;
        long afterUpdateMillis = 1L;

        // 충전 내역
        PointHistory insertedPointHistory = new PointHistory(1L, id, chargeAmount, TransactionType.CHARGE, System.currentTimeMillis());
        List<PointHistory> userHistories = new ArrayList<>();
        userHistories.add(insertedPointHistory);

        // 포인트
        UserPoint beforeUserPoint = new UserPoint(id, beforeAmount, beforeUpdateMillis);
        UserPoint chargedUserPoint = new UserPoint(id, beforeAmount + chargeAmount, afterUpdateMillis);

        // 충전 내역
        doReturn(userHistories).when(pointHistoryTable).selectAllByUserId(id);

        // 포인트
        doReturn(beforeUserPoint).when(userPointTable).selectById(id);
        doReturn(chargedUserPoint).when(userPointTable).insertOrUpdate(id, beforeAmount + chargeAmount);

        // 충전 내역
        List<PointHistory> returnPointHistories = pointService.selectAllByUserId(1L);

        assertEquals(1, returnPointHistories.size());
        assertEquals(insertedPointHistory.id(), returnPointHistories.get(0).id());
        assertEquals(insertedPointHistory.userId(), returnPointHistories.get(0).userId());
        assertEquals(insertedPointHistory.amount(), returnPointHistories.get(0).amount());
        assertEquals(insertedPointHistory.type(), returnPointHistories.get(0).type());
        assertEquals(insertedPointHistory.updateMillis(), returnPointHistories.get(0).updateMillis());

        // 포인트
        UserPoint returnUserPoint = pointService.charge(id, chargeAmount);

        assertEquals(returnUserPoint.id(), chargedUserPoint.id());
        assertEquals(returnUserPoint.point(), chargedUserPoint.point());
        assertEquals(returnUserPoint.updateMillis(), chargedUserPoint.updateMillis());
    }



}
