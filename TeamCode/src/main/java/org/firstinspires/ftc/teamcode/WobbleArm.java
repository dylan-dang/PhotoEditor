package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp
public class WobbleArm extends LinearOpMode {

    @Override public void runOpMode() throws InterruptedException {

        DcMotor wobbleDrive = hardwareMap.dcMotor.get("idk");
        wobbleDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        wobbleDrive.setTargetPosition(0);
        wobbleDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        wobbleDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        boolean du = false;
        boolean dd = false;
        double wobbleVal = 0;

        waitForStart();
        if (isStopRequested()) return;
        while (opModeIsActive()) {

            du = gamepad2.dpad_up;
            dd = gamepad2.dpad_down;

            if (du && wobbleVal < 1499){
                wobbleVal++;
            }
            if (dd && wobbleVal > 0){
                wobbleVal--;
            }


        }
    }
}
