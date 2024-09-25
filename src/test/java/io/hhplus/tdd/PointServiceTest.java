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
import static org.junit.jupiter.api.Assertions.assertThrows;
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


        UserPoint beforeUserPoint = new UserPoint(id, beforeAmount, beforeUpdateMillis);
        UserPoint chargedUserPoint = new UserPoint(id, beforeAmount + chargeAmount, afterUpdateMillis);


        doReturn(beforeUserPoint).when(userPointTable).selectById(id);
        doReturn(chargedUserPoint).when(userPointTable).insertOrUpdate(id, beforeAmount + chargeAmount);


        UserPoint returnUserPoint = pointService.charge(id, chargeAmount);

        assertEquals(returnUserPoint.id(), chargedUserPoint.id());
        assertEquals(returnUserPoint.point(), chargedUserPoint.point());
        assertEquals(returnUserPoint.updateMillis(), chargedUserPoint.updateMillis());
    }

    @Test
    @DisplayName("특정 id에 포인트 충전/사용 시 내역이 저장된다.")
    void 포인트_충전_시_내역_저장() {

        long id = 1L;
        long chargeAmount = 50L;

        PointHistory insertedPointHistory = new PointHistory(1L, id, chargeAmount, TransactionType.CHARGE, System.currentTimeMillis());
        List<PointHistory> userHistories = new ArrayList<>();
        userHistories.add(insertedPointHistory);

        doReturn(userHistories).when(pointHistoryTable).selectAllByUserId(id);

        List<PointHistory> returnPointHistories = pointService.selectAllByUserId(1L);

        assertEquals(1, returnPointHistories.size());
        assertEquals(insertedPointHistory.id(), returnPointHistories.get(0).id());
        assertEquals(insertedPointHistory.userId(), returnPointHistories.get(0).userId());
        assertEquals(insertedPointHistory.amount(), returnPointHistories.get(0).amount());
        assertEquals(insertedPointHistory.type(), returnPointHistories.get(0).type());
        assertEquals(insertedPointHistory.updateMillis(), returnPointHistories.get(0).updateMillis());
    }

    @Test
    @DisplayName("회원 포인트 사용시 잔여 포인트가 부족할 때 에러 발생")
    void 잔여_포인트_없어서_에러_발생() {
        long id = 1L;
        long amount = 100L;
        long updateMillis = 0L;

        UserPoint userPoint = new UserPoint(id, amount, updateMillis);

        doReturn(userPoint).when(userPointTable).selectById(1L);

        assertThrows(IllegalArgumentException.class, () -> pointService.use(id, amount + 1L));
    }

    @Test
    @DisplayName("특정 id로 포인트 충전/사용 이력 조회")
    void 포인트_사용_충전_이력_조회() {
        long historyId = 1L;
        long userId = 2L;
        long amount1 = 200L;
        long amount2 = 100L;
        long updateMillis1 = 1L;
        long updateMillis2 = 2L;


        PointHistory insertedPointHistory1 = new PointHistory(historyId, userId, amount1, TransactionType.CHARGE, updateMillis1);
        PointHistory insertedPointHistory2 = new PointHistory(historyId + 1, userId, amount2, TransactionType.USE, updateMillis2);
        List<PointHistory> userHistories = new ArrayList<>();
        userHistories.add(insertedPointHistory1);
        userHistories.add(insertedPointHistory2);

        doReturn(userHistories).when(pointHistoryTable).selectAllByUserId(userId);

        List<PointHistory> returnPointHistories = pointService.selectAllByUserId(userId);

        assertEquals(2, returnPointHistories.size());
        assertEquals(insertedPointHistory1.id(), returnPointHistories.get(0).id());
        assertEquals(insertedPointHistory1.userId(), returnPointHistories.get(0).userId());
        assertEquals(insertedPointHistory1.amount(), returnPointHistories.get(0).amount());
        assertEquals(insertedPointHistory1.type(), returnPointHistories.get(0).type());
        assertEquals(insertedPointHistory1.updateMillis(), returnPointHistories.get(0).updateMillis());

        assertEquals(insertedPointHistory2.id(), returnPointHistories.get(1).id());
        assertEquals(insertedPointHistory2.userId(), returnPointHistories.get(1).userId());
        assertEquals(insertedPointHistory2.amount(), returnPointHistories.get(1).amount());
        assertEquals(insertedPointHistory2.type(), returnPointHistories.get(1).type());
        assertEquals(insertedPointHistory2.updateMillis(), returnPointHistories.get(1).updateMillis());
    }

    @Test
    @DisplayName("id가 양수인지 확인하는 테스트")
    void 사용자포인트_id_양수_아니면_에러() {
        long id1 = -1L;
        long id2 = 0L;
        long amount = 100L;
        long updateMillis = 0L;

        assertThrows(IllegalArgumentException.class, () -> new UserPoint(id1, amount, updateMillis));
        assertThrows(IllegalArgumentException.class, () -> new UserPoint(id2, amount, updateMillis));
    }

    @Test
    @DisplayName("충전 시 포인트가 양수가 아닐 때 에러 발생")
    void 충전_시_포인트는_양수_아닐때_에러() {
        long id = 1L;
        long amount = -1L;
        long updateMillis = 0L;

        assertThrows(IllegalArgumentException.class, () -> new UserPoint(id, amount, updateMillis));
    }


}
