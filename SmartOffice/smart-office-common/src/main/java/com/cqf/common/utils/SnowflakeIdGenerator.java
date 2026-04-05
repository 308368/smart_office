package com.cqf.common.utils;

/**
 * 雪花算法 ID 生成器
 * 用于生成分布式唯一 ID
 */
public class SnowflakeIdGenerator {

    // 起始时间戳 (2024-01-01 00:00:00)
    private static final long START_TIMESTAMP = 1704067200000L;

    // 机器 ID 所占位数
    private static final long MACHINE_ID_BITS = 5L;

    // 数据中心 ID 所占位数
    private static final long DATA_CENTER_ID_BITS = 5L;

    // 序列号所占位数
    private static final long SEQUENCE_BITS = 12L;

    // 最大值计算
    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    // 左移位数
    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS + DATA_CENTER_ID_BITS;

    private final long machineId;
    private final long dataCenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     *
     * @param machineId     机器 ID (0-31)
     * @param dataCenterId  数据中心 ID (0-31)
     */
    public SnowflakeIdGenerator(long machineId, long dataCenterId) {
        if (machineId < 0 || machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException("Machine ID 必须在 0-" + MAX_MACHINE_ID + " 之间");
        }
        if (dataCenterId < 0 || dataCenterId > MAX_DATA_CENTER_ID) {
            throw new IllegalArgumentException("Data Center ID 必须在 0-" + MAX_DATA_CENTER_ID + " 之间");
        }
        this.machineId = machineId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * 生成下一个 ID
     *
     * @return 分布式唯一 ID
     */
    public synchronized long nextId() {
        long currentTimestamp = getCurrentTimestamp();

        // 时钟回拨检测
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成 ID");
        }

        // 同一毫秒内，序列号自增
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 序列号溢出，等待下一毫秒
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置为 0
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        // 组装 ID
        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (machineId << MACHINE_ID_SHIFT)
                | sequence;
    }

    /**
     * 等待下一毫秒
     *
     * @param lastTimestamp 上次生成 ID 的时间戳
     * @return 下一毫秒的时间戳
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳
     */
    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    // ==================== 静态工具方法 ====================

    private static volatile SnowflakeIdGenerator defaultGenerator;

    /**
     * 获取默认的雪花算法生成器（单例）
     * 默认机器 ID 和数据中心 ID 都为 0
     *
     * @return SnowflakeIdGenerator 实例
     */
    public static SnowflakeIdGenerator getDefaultGenerator() {
        if (defaultGenerator == null) {
            synchronized (SnowflakeIdGenerator.class) {
                if (defaultGenerator == null) {
                    defaultGenerator = new SnowflakeIdGenerator(0, 0);
                }
            }
        }
        return defaultGenerator;
    }

    /**
     * 生成下一个 ID（使用默认生成器）
     *
     * @return 分布式唯一 ID
     */
    public static long generateId() {
        return getDefaultGenerator().nextId();
    }

    /**
     * 生成下一个 ID 并转换为字符串（使用默认生成器）
     *
     * @return 分布式唯一 ID 字符串
     */
    public static String generateIdStr() {
        return String.valueOf(generateId());
    }
}
