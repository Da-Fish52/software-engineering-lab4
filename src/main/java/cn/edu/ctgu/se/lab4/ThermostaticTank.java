package cn.edu.ctgu.se.lab4;

/**
 * B1 恒温水箱状态机（20℃～100℃）
 *
 * 状态：CHECKING(检查) / BOILING(烧水) / IDLE(停止加热) / SLEEPING(休眠)
 *       / BROKEN(故障) / REPAIRING(维修)
 * 事件：START / TIMER(小时) / TEMP(温度) / WATER(有无水) / BREAKDOWN / REPAIRED
 */
public class ThermostaticTank {

    /** 状态枚举 */
    public enum State { CHECKING, BOILING, IDLE, SLEEPING, BROKEN, REPAIRING }

    /** 加热继电器 */
    public interface Relay {
        void on();
        void off();
    }

    private State state = State.CHECKING;
    private final Relay relay;

    public ThermostaticTank(Relay relay) {
        this.relay = relay;
        relay.off();
    }

    public State getState() { return state; }

    /** 读传感器并驱动状态机：每次调用相当于一个"检查"周期 */
    public void tick(double temperature, boolean hasWater) {
        switch (state) {
            case CHECKING:
            case IDLE:
                if (temperature < 20.0) {
                    if (hasWater) {
                        enterBoiling();          // 有水：打开电源
                    } else {
                        enterIdle();             // 无水：不打开电源
                    }
                }
                break;
            case BOILING:
                if (temperature >= 100.0) {
                    enterIdle();                 // 达到 100℃ 断电
                }
                break;
            case SLEEPING:
            case BROKEN:
            case REPAIRING:
                break;
            default:
                break;
        }
    }

    /** 时钟事件：23:00 休眠，07:00 恢复 */
    public void onClock(int hour) {
        if (hour == 23 && state != State.SLEEPING && state != State.BROKEN) {
            enterSleeping();
        } else if (hour == 7 && state == State.SLEEPING) {
            enterChecking();
        }
    }

    /** 水箱烧坏 */
    public void onBreakdown() {
        enterBroken();
    }

    /** 维修完成 */
    public void onRepaired() {
        if (state == State.BROKEN) {
            state = State.REPAIRING;
            enterChecking();
        }
    }

    private void enterChecking() {
        relay.off();
        state = State.CHECKING;
    }

    private void enterBoiling() {
        relay.on();
        state = State.BOILING;
    }

    private void enterIdle() {
        relay.off();
        state = State.IDLE;
    }

    private void enterSleeping() {
        relay.off();
        state = State.SLEEPING;
    }

    private void enterBroken() {
        relay.off();
        state = State.BROKEN;
    }

    @Override
    public String toString() {
        return "ThermostaticTank{state=" + state + "}";
    }

    // ---------------- 测试 ----------------
    public static void main(String[] args) {
        StringBuilder log = new StringBuilder();
        Relay r = new Relay() {
            public void on()  { log.append("[继电器 ON] "); }
            public void off() { log.append("[继电器 OFF] "); }
        };
        ThermostaticTank t = new ThermostaticTank(r);

        System.out.println("--- 场景1：无水，温度低，不应加热 ---");
        t.tick(15, false);
        System.out.println(t + "  " + log);
        assertEquals(State.IDLE, t.getState());

        System.out.println("\n--- 场景2：加水后温度仍低，开始烧水 ---");
        t.tick(15, true);
        System.out.println(t + "  " + log);
        assertEquals(State.BOILING, t.getState());

        System.out.println("\n--- 场景3：升温到 100℃ 断电 ---");
        t.tick(100, true);
        System.out.println(t + "  " + log);
        assertEquals(State.IDLE, t.getState());

        System.out.println("\n--- 场景4：23 点自动休眠 ---");
        t.tick(10, true);              // 先让它烧水
        t.onClock(23);
        System.out.println(t + "  " + log);
        assertEquals(State.SLEEPING, t.getState());

        System.out.println("\n--- 场景5：7 点恢复 ---");
        t.onClock(7);
        System.out.println(t + "  " + log);
        assertEquals(State.CHECKING, t.getState());

        System.out.println("\n--- 场景6：烧坏->维修->复位 ---");
        t.tick(50, true);
        t.onBreakdown();
        System.out.println(t + "  " + log);
        assertEquals(State.BROKEN, t.getState());
        t.onRepaired();
        System.out.println(t + "  " + log);
        assertEquals(State.CHECKING, t.getState());

        System.out.println("\n全部用例通过");
    }

    private static void assertEquals(State expect, State actual) {
        if (expect != actual) {
            throw new AssertionError("期望 " + expect + " 实际 " + actual);
        }
    }
}
