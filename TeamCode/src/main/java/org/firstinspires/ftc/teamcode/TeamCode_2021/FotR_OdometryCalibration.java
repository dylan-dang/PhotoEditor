package org.firstinspires.ftc.teamcode.TeamCode_2021;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.ReadWriteFile;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.Utilities.FotR_Controller;

import java.io.File;

@TeleOp(name = "Odometry Calibration", group = "Calibration")
public class FotR_OdometryCalibration extends LinearOpMode {

    FotR_Ringbearer_Hardware robot = new FotR_Ringbearer_Hardware();

    final double PIVOT_SPEED = 0.5;

    //The amount of encoder ticks for each inch the robot moves. THIS WILL CHANGE FOR EACH ROBOT AND NEEDS TO BE UPDATED HERE
    final double COUNTS_PER_INCH = 1742.971;

    double TrueAngle = 0;

    ElapsedTime timer = new ElapsedTime();
    FotR_Controller PID = new FotR_Controller();

    double k1, k2, k3;
    double A1Pos = 0, A2Pos = 0, A3Pos = 0;

    //Text files to write the values to. The files are stored in the robot controller under Internal Storage\FIRST\settings
    File K1 = null;
    File K2 = null;
    File K3 = null;

    @Override
    public void runOpMode() throws InterruptedException {
        robot.init(hardwareMap);


        K1 = AppUtil.getInstance().getSettingsFile("K1.txt");
        K2 = AppUtil.getInstance().getSettingsFile("K2.txt");
        K3 = AppUtil.getInstance().getSettingsFile("K3.txt");

        waitForStart();

        robot.setDriveSpeeds(-0.8,0.8,-0.8,0.8);

        robot.IMUangles();
        TrueAngle = robot.anglesDeg.secondAngle;

        while (TrueAngle < 160) {
            robot.IMUangles();
            TrueAngle = robot.anglesDeg.secondAngle;
            telemetry.addData("Angle", TrueAngle);
            telemetry.addData("Step 1", "Started");
            telemetry.update();
        }
        while (TrueAngle < -160) {
            robot.IMUangles();
            TrueAngle = robot.anglesDeg.secondAngle;
            telemetry.addData("Angle", TrueAngle);
            telemetry.addData("Step 2", "Started");
            telemetry.update();
        }
        telemetry.addData("Step 3", "Started");
        telemetry.update();
        turn(0.7,0);

        //Stop the robot
        robot.setDriveSpeeds(0, 0, 0, 0);

        A1Pos = -robot.odo1Pos.getCurrentPosition();
        A2Pos = robot.odo2Pos.getCurrentPosition();
        A3Pos = -robot.odo3Pos.getCurrentPosition();

        double avg = (A1Pos + A2Pos + A3Pos) / 3;

        k1 = A1Pos / avg;
        k2 = A2Pos / avg;
        k3 = A3Pos / avg;

        //Write the constants to text files
        ReadWriteFile.writeFile(K1, String.valueOf(k1));
        ReadWriteFile.writeFile(K2, String.valueOf(k2));
        ReadWriteFile.writeFile(K3, String.valueOf(k3));

        telemetry.addData("Calibration Complete", "");
        telemetry.update();
        sleep(500);
    }

    private void turn(double maxPower, double angle) {

        double power = 0;
        double hdgError = 0;
        int CountCycles = 0;
        boolean turnComplete = false;


        angle = Math.toRadians(angle);

        while (opModeIsActive() && !turnComplete && !isStopRequested()) {

            robot.IMUangles();
            TrueAngle = robot.anglesRad.secondAngle;
            double relAngle;

            relAngle = TrueAngle - angle;

            hdgError = -Math.atan2(Math.sin(relAngle), Math.cos(relAngle));

            power = PID.limitedPID(hdgError , maxPower,0.05,0.01, Math.toRadians(23), Math.toRadians(10), Math.toRadians(30));

            robot.mecanum.WheelCalcField(0,0, power, 0.8, 0.8, Math.toDegrees(TrueAngle));

            robot.setMecanumSpeeds(robot.mecanum);

            if (Math.abs(hdgError) < Math.toRadians(1)) {
                CountCycles += 1;
                if (CountCycles > 5) {
                    turnComplete = true;
                }
            }

            telemetry.update();

        }
        robot.setDriveSpeeds(0,0,0,0);
    }

}
