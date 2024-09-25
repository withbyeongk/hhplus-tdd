package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint selectById(long id) {
        return userPointTable.selectById(id);
    }

    public List<PointHistory> selectAllByUserId(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    public UserPoint charge(long id, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        UserPoint userPoint = userPointTable.selectById(id);

        UserPoint chargedUserPoint = userPointTable.insertOrUpdate(id, userPoint.point() + amount);

        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return chargedUserPoint;
    }

    public UserPoint use(long id, long amount) {
        UserPoint userPoint = userPointTable.selectById(id);

        long remainingPoints = userPoint.point() - amount;

        if (remainingPoints < 0) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        UserPoint usedUserPoint = userPointTable.insertOrUpdate(id, remainingPoints);

        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

        return usedUserPoint;
    }
}
