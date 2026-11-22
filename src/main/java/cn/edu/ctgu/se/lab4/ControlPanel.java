package cn.edu.ctgu.se.lab4;

/**
 * B2 SafeHome 控制面板 ControlPanel 状态机
 *
 * 状态：READING(读取键入字符) / VERIFYING(密码验证) / SELECTING(系统功能选择)
 *       / LOCKED(锁定) / READY(就绪)
 * 规则：密码长度 4；连续输错 3 次锁定；锁定 120 秒后解除；密码正确进入功能选择。
 */
public class ControlPanel {

    public enum State { READING, VERIFYING, SELECTING, LOCKED, READY }

    public static final int PASSWORD_LENGTH = 4;
    public static final int MAX_ATTEMPTS = 3;
    public static final long LOCK_MILLIS = 120_000L;

    private State state = State.READING;
    private final String password;
    private final StringBuilder buffer = new StringBuilder();
    private int errorCount = 0;
    private long lockStartTime = 0L;

    /** 可注入的时钟，便于测试 */
    public interface Clock {
        long now();
    }

    private final Clock clock;

    public ControlPanel(String password, Clock clock) {
        this.password = password;
        this.clock = clock;
    }

    public State getState() { return state; }

    public int getErrorCount() { return errorCount; }

    /** 户主敲击一个按键 */
    public void pressKey(char c) {
        // 锁定状态：只有到时间才恢复
        if (state == State.LOCKED) {
            if (clock.now() - lockStartTime >= LOCK_MILLIS) {
                unlock();
            } else {
                return;                       // 锁定期间忽略按键
            }
        }
        if (state == State.READING) {
            buffer.append(c);
            if (buffer.length() >= PASSWORD_LENGTH) {
                state = State.VERIFYING;
                verify();
            }
        }
    }

    /** 清除已输入内容，回到读取状态 */
    public void reset() {
        buffer.setLength(0);
        if (state == State.READING || state == State.VERIFYING) {
            state = State.READING;
        }
    }

    private void verify() {
        boolean ok = password.contentEquals(buffer);
        buffer.setLength(0);
        if (ok) {
            errorCount = 0;
            state = State.SELECTING;          // 密码正确 → 进入系统功能选择
        } else {
            errorCount++;
            if (errorCount >= MAX_ATTEMPTS) {
                lock();                       // 错 3 次 → 锁定
            } else {
                state = State.READING;        // 错误但未满 3 次 → 重新输入
            }
        }
    }

    private void lock() {
        state = State.LOCKED;
        lockStartTime = clock.now();
    }

    private void unlock() {
        errorCount = 0;
        state = State.READY;                  // 解锁后先进入就绪
        state = State.READING;                // 就绪 → 允许输入
    }

    /** 户主退出系统 */
    public void exit() {
        if (state == State.SELECTING) {
            state = State.READY;
        }
    }

    /** 户主提交/清除当前操作，回到读取状态（相当于面板的"清除/取消"） */
    public void clear() {
        buffer.setLength(0);
        state = State.READING;
    }

    @Override
    public String toString() {
        return "ControlPanel{state=" + state + ", errors=" + errorCount + "}";
    }

    // ---------------- 测试 ----------------
    public static void main(String[] args) {
        final long[] now = { 0L };
        ControlPanel p = new ControlPanel("1234", () -> now[0]);

        System.out.println("--- 场景1：密码正确 ---");
        type(p, "1234");
        System.out.println(p);
        assertEquals(State.SELECTING, p.getState());

        System.out.println("\n--- 场景2：功能执行完清除后，输错一次，回到读取 ---");
        p.clear();
        type(p, "9999");
        System.out.println(p);
        assertEquals(State.READING, p.getState());
        assertEquals(1, p.getErrorCount());

        System.out.println("\n--- 场景3：再错两次，共 3 次 → 锁定 ---");
        type(p, "8888");
        type(p, "7777");
        System.out.println(p);
        assertEquals(State.LOCKED, p.getState());

        System.out.println("\n--- 场景4：锁定期间按键被忽略 ---");
        type(p, "1234");
        assertEquals(State.LOCKED, p.getState());

        System.out.println("\n--- 场景5：120 秒后解锁，可重新输入 ---");
        now[0] = 121_000L;
        p.pressKey('1');                      // 触发解锁
        type(p, "234");
        System.out.println(p);
        assertEquals(State.SELECTING, p.getState());

        System.out.println("\n全部用例通过");
    }

    private static void type(ControlPanel p, String s) {
        for (char c : s.toCharArray()) {
            p.pressKey(c);
        }
    }

    private static void assertEquals(Object expect, Object actual) {
        if (!expect.equals(actual)) {
            throw new AssertionError("期望 " + expect + " 实际 " + actual);
        }
    }
}
