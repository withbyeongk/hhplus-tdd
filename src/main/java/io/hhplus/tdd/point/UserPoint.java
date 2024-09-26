package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {
    public UserPoint(long id, long point, long updateMillis) {
        if (id <= 0) {
            throw new IllegalArgumentException("id는 양수여야 합니다.");
        }
        this.id = id;
        this.point = point;
        this.updateMillis = updateMillis;
    }
    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }
}
