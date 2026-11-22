# 软件工程实验四：面向对象的系统建模 B

学号：202410120205    姓名：韩达    班级：B1-119    日期：2026-11-22

## 目录结构

```
se-assignment-lab4/
├── README.md
├── src/main/java/cn/edu/ctgu/se/lab4/
│   ├── ThermostaticTank.java     B1 恒温水箱状态机（20℃~100℃）
│   ├── ControlPanel.java         B2 SafeHome 控制面板状态机
│   └── StringExtractor.java      B3 源代码字符串提取状态机
└── uml/
    ├── B1_恒温水箱状态图.puml
    ├── B2_ControlPanel状态图.puml
    └── B3_字符串提取状态图.puml
```

## 编译与运行

JDK 11 及以上：

```bash
javac -encoding UTF-8 -d out src/main/java/cn/edu/ctgu/se/lab4/*.java
java -cp out cn.edu.ctgu.se.lab4.ThermostaticTank
java -cp out cn.edu.ctgu.se.lab4.ControlPanel
java -cp out cn.edu.ctgu.se.lab4.StringExtractor
```

每个类都带 main 方法和断言，运行输出“全部用例通过”即表示测试成功。

## 状态机要点

| 题目 | 状态 | 关键规则 |
|---|---|---|
| B1 | Checking / Boiling / Idle / Sleeping / Broken / Repairing | 温度<20℃且**有水**才加热；≥100℃断电；23:00 休眠，07:00 恢复 |
| B2 | Reading / Verifying / Selecting / Locked / Ready | 密码 4 位；错 3 次锁 120 秒；正确则进入功能选择 |
| B3 | Scanning / InString / Escaped | 反斜杠转义下一个字符，`\"` 不作为字符串结束 |

## UML 图

`uml/*.puml` 为 PlantUML 源码，可粘贴到 <https://www.plantuml.com/plantuml> 在线渲染，
也可在 Visual Paradigm / IDEA 的 PlantUML 插件中打开。
