package org.firstinspires.ftc.teamcode;

import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE;
import static org.apache.commons.math3.linear.MatrixUtils.createRealMatrix;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

@TeleOp
public class MecanumOp extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        double magic = 0.3413125;
        double wheelRadius = 0.05;

        RealMatrix transformation = createRealMatrix(new double[][] {
                {1, 1, 1, 1},
                {-1, 1, 1, -1},
                {-magic, magic, -magic, magic}
        }).scalarMultiply(1 / wheelRadius);

        DcMotor motorFrontLeft = hardwareMap.dcMotor.get("port3");
        DcMotor motorBackLeft = hardwareMap.dcMotor.get("port0");
        DcMotor motorFrontRight = hardwareMap.dcMotor.get("port2");
        DcMotor motorBackRight = hardwareMap.dcMotor.get("port1");

        motorFrontRight.setDirection(REVERSE);
        motorBackRight.setDirection(REVERSE);

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {
            double y = gamepad1.left_stick_y;
            double x = gamepad1.left_stick_x;
            double rx = gamepad1.right_stick_x;

            RealVector wheelVelocities = transformation.preMultiply(new ArrayRealVector(new double[] {y, x, rx}));
            wheelVelocities.mapDivide(26.82625d);

            motorFrontLeft.setPower(wheelVelocities.getEntry(0));
            motorFrontRight.setPower(wheelVelocities.getEntry(1));
            motorBackLeft.setPower(wheelVelocities.getEntry(2));
            motorBackRight.setPower(wheelVelocities.getEntry(3));
        }
    }
}
