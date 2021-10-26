package org.firstinspires.ftc.teamcode.Utilities;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;


@TeleOp(name="led jank3", group="Iterative Opmode")
@Disabled
public class LED_test3 extends OpMode {

    RevBlinkinLedDriver blinkinLedDriver;
    RevBlinkinLedDriver.BlinkinPattern pattern;

    ElapsedTime ledtime = new ElapsedTime();

    /**
     * Note to self 30 > 1:40 > 30
     */
    @Override
    public void init() {
        blinkinLedDriver = hardwareMap.get(RevBlinkinLedDriver.class, "blinkin");
        pattern = RevBlinkinLedDriver.BlinkinPattern.BLACK;
        blinkinLedDriver.setPattern(pattern);
        ledtime.reset();
    }

    public void loop() {

       if (ledtime.milliseconds() == 0) {

            pattern = RevBlinkinLedDriver.BlinkinPattern.CP1_END_TO_END_BLEND_TO_BLACK;
            blinkinLedDriver.setPattern(pattern);

        } else if (ledtime.milliseconds() >= 84000) {

            pattern = RevBlinkinLedDriver.BlinkinPattern.CP1_2_COLOR_GRADIENT;
            blinkinLedDriver.setPattern(pattern);

        }
        else if(ledtime.milliseconds() >= 30000) {

            pattern = RevBlinkinLedDriver.BlinkinPattern.CP2_LIGHT_CHASE;
            blinkinLedDriver.setPattern(pattern);
        }
        else {
            pattern = RevBlinkinLedDriver.BlinkinPattern.BLACK;
            blinkinLedDriver.setPattern(pattern);
        }
    }
}
