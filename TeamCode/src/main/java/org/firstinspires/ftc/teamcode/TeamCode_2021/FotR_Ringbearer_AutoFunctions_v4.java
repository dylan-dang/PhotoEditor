package org.firstinspires.ftc.teamcode.TeamCode_2021;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Utilities.FotR_Controller;
import org.firstinspires.ftc.teamcode.Utilities.FotR_Pathing;

import static org.firstinspires.ftc.teamcode.Utilities.MathFunctions.angleWrap;
/*
Array Command Line Storage

0: MoveTo [Power (0.0~1.0)] [X Position (-75~75)] [Y Position (-75~75)] [Rotation (-180~180)] [Spline Motion (0~1)]
1: Turn [Rotation (-180~180)]
2: Flywheel Power [Toggle On/Off]
3: Shoot Rings [Deck Deg (20~48)] [Number of Shots (0~oo)]  //Fires rings and updates deck angle
4: Prep Shot [Bucket Pos Toggle] [Deck Deg (20~48)]  //Program lifts bucket and sets deck angle
5: Wobble Movement [Toggle Place/Grab]
6: Intake Drive [Toggle On/Off]
7: Stall [Wait Time (0~oo)]
8: Coded Starting Data: [Indicate PowerShotVolley (0~1)] [Starting Pole (0~3)]
*/

//Code that holds all of the functions needed to perform a manevuer in autonomous
public class FotR_Ringbearer_AutoFunctions_v4 extends LinearOpMode {

    FotR_Ringbearer_Hardware robot;
    FotR_Controller PID = new FotR_Controller();
    ElapsedTime runtime = new ElapsedTime();

    final double SP = 1.0; //Standard Power Level

    double TrueAngle = 0;

    //Constructor needs to grab the Hardware function from elsewhere so it can control the robot
    public FotR_Ringbearer_AutoFunctions_v4(FotR_Ringbearer_Hardware hardware) {
        robot = hardware;
    }

    //Joke because no one should be running this as it's own OpMode
    public void runOpMode() throws InterruptedException {
        telemetry.addData("What is it precious", "What is this nasty little Opmodes with no uses?");
        telemetry.update();
        sleep(5000);
    }

    //Move the robot laterally
    public void moveTo(double power, double x, double y, double angle, boolean finishTurn, boolean park) {

        boolean slideComplete = false;
        //If the manevuer needs to park, choose a small minimum distance to stop the robot from the point
        double minDist = 10;
        if (park) {minDist = 2;}

        while (!slideComplete && !isStopRequested()) {

            robot.bulkRead();
            TrueAngle = robot.imuRad;

            //Use Pathing helping method to find a vector to move along using sensor readings
            FotR_Pathing.goToPositionOneSpeed(robot.odoPod.returnFieldX(), robot.odoPod.returnFieldY(), TrueAngle, x, y, power, angle, park);
            //Use the static variables from the helper method and calculate the mecanum movements needed
            robot.mecanum.WheelCalcField(FotR_Pathing.moveX, FotR_Pathing.moveY, FotR_Pathing.moveTurn, 0.9, 1.0, TrueAngle);

            robot.setMecanumSpeeds(robot.mecanum);

            //Finds when we are close enough to the target to stop moving
            slideComplete = FotR_Pathing.distToTarget < minDist;

            if (Thread.interrupted()) {
                try {
                    throw new InterruptedException();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
        //If we need to complete a turn at the end of the maneveur, call a turn function
        if (finishTurn) {
            turn(power,angle,2);
        }
        //Stop the robot if parking is needed
        if (park) {
            robot.setDriveSpeeds(0,0,0,0);
        }
    }

    //Used to turn the robot on a point
    public void turn(double maxPower, double headingDeg, double angleDegTolerance) {

        double power = 0;
        double hdgError = 0;
        int CountCycles = 0;
        boolean turnComplete = false;

        double headingRad = Math.toRadians(headingDeg);

        while (!turnComplete && !isStopRequested()) {

            robot.bulkRead();
            TrueAngle = robot.imuRad;
            double relAngle;

            //Find the relative angle to our target, wrap the angle to avoid getting over 180 on the turn
            relAngle = angleWrap(TrueAngle - headingRad);
            hdgError = -Math.atan2(Math.sin(relAngle), Math.cos(relAngle));

            //Find the power of the turn via a PID helper method that only applies certain parts of the equation at certain angles
            power = PID.limitedPID(hdgError , maxPower,0.1,0.15, Math.toRadians(35), Math.toRadians(10), Math.toRadians(30));
            // power = PID.limitedPID(hdgError , maxPower,0.1,0.3, Math.toRadians(23), Math.toRadians(10), Math.toRadians(30));

            //Calculate the mecanum movements and set the power
            robot.mecanum.WheelCalcField(0,0, power, 0, 1.0, Math.toDegrees(TrueAngle));

            robot.setMecanumSpeeds(robot.mecanum);

            //Whenever the Heading Error is under the Tolerance level, count for a number of clock cycles to make sure we've settled on the target
            if (Math.abs(hdgError) < Math.toRadians(angleDegTolerance)) {
                CountCycles += 1;
                if (CountCycles > 3) {
                    turnComplete = true;
                }
            }

            if (Thread.interrupted()) {
                try {
                    throw new InterruptedException();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        robot.setDriveSpeeds(0,0,0,0);
    }

    //Set Flywheel speed
    public void setFlywheel(double velocity) {
        robot.aftShooter.setVelocity(velocity);
        robot.foreShooter.setVelocity(velocity);
    }

    //Ring shooter
    public void shootRings(double deckDeg, int fireCount, double velocity) {
        boolean shooting = false;
        int fired = 0;
        final double fireRate = 200;
        final double firePause = 160;
        double prevFireRate = 0.0;
        ElapsedTime fireInterrupt = new ElapsedTime();
        robot.flicker.setPosition(robot.flickDock);
        robot.bucketArm.setPosition(0.26);
        robot.shooterLift.setPosition(robot.degToServo(deckDeg));
        //Fire up the wheels as soon as this method is called, -1 is used to ignore and use the last value used
        if (velocity != -1) {
            setFlywheel(velocity);
        }

        while (fired < fireCount && !isStopRequested() ) {
            //Uses a slightly modified version of the shooting interrupter from TeleOp to trigger the firing at certain intervals, to a specified limit
            if (shooting) {
                robot.flicker.setPosition(robot.flickShoot);
            }
            else {robot.flicker.setPosition(robot.flickDock);}

            if (shooting && fireInterrupt.milliseconds() >= (prevFireRate + fireRate + firePause)) {
                prevFireRate = fireInterrupt.milliseconds();
                fired++;
            }

            shooting = (fireInterrupt.milliseconds() >= (prevFireRate + fireRate));

            if (Thread.interrupted()) {
                try {
                    throw new InterruptedException();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
        //Light delay at the end to make sure the last shot of the count is finished
        robot.flicker.setPosition(robot.flickDock);
        if (fireCount >= 1) {
            sleep((long) fireRate + 300);
        }


    }

    //Moves the wobble arm and claw
    public void wobbleMovement(double power, double position, boolean openClaw) {
        robot.bulkRead();
        int clawPlacement = 2;
        if (power != -1) {robot.wobbleArm.setPower(0.7);}
        if (openClaw) {clawPlacement = 0;}
        boolean goingUp = robot.wobbleArm.getCurrentPosition() > position;
        boolean clawMoving = (robot.wobbleClaw.getPosition() != robot.wobbleClawPos[clawPlacement]);

        //Checks to see if the claw needs to be moved, and if we're going up or down, by that, determine whether to move the claw now
        if (goingUp && clawMoving) {
            robot.wobbleClaw.setPosition(robot.wobbleClawPos[clawPlacement]);
            sleep(600);
        }

        //Set the position of the arm
        if (position != -1) {robot.wobbleArm.setTargetPosition((int) position);}

        runtime.reset();
        //Wait for the arm to finish moving, and move the claw if needed at that point.
        while (clawMoving && !isStopRequested() && !goingUp && runtime.milliseconds() < 3000) {
            robot.bulkRead();
            if (Math.abs(robot.wobbleArm.getCurrentPosition() - robot.wobbleArm.getTargetPosition()) < 10) {
                robot.wobbleClaw.setPosition(robot.wobbleClawPos[clawPlacement]);
                clawMoving = false;
            }
            if (Thread.interrupted()) {
                try {
                    throw new InterruptedException();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //Make sure to move the claw into position if the manevuer took over 3 seconds (the average time needed to bring the arm down
        if (runtime.milliseconds() >= 3000) {
            robot.wobbleClaw.setPosition(robot.wobbleClawPos[clawPlacement]);
        }

    }

    //Set the intake parameters based on what is given
    public void intakeDrive (boolean pullIn, double power) {
        robot.bucketArm.setPosition(0.04);
        if (power != -1) {
            if (pullIn) {
                robot.leftIntake.setDirection(CRServo.Direction.FORWARD);
                robot.rightIntake.setDirection(CRServo.Direction.REVERSE);
                robot.leftIntake.setPower(power);
                robot.rightIntake.setPower(power);
                robot.conveyor.setPower(power * 0.8);
            } else {
                robot.leftIntake.setDirection(CRServo.Direction.REVERSE);
                robot.rightIntake.setDirection(CRServo.Direction.FORWARD);
                robot.leftIntake.setPower(power);
                robot.rightIntake.setPower(power);
                robot.conveyor.setPower(-power * 0.8);
            }
        }
    }

    //Repeated calls of turn and shootRings to fire three rings into what should ideally be the Power Shots
    public void powerShotVolley(int startPos, Gamepad gamepad) {
        switch (startPos) {
            case 0: {
                shootRings(31,-1,1809);
                if (gamepad.right_stick_button) {break;}

                turn(SP,-11.4, 1);
                if (gamepad.right_stick_button) {break;}
                shootRings(29.5,3,1809);
                break;
            }
            case 1: {
                shootRings(31,-1,1600);//1690
                if (gamepad.right_stick_button) {break;}

                turn(SP,-5, 1);//-3
                if (gamepad.right_stick_button) {break;}
                shootRings(31.4,1,1600);
                if (gamepad.right_stick_button) {break;}

                turn(SP,-0.1, 1);//1.9
                if (gamepad.right_stick_button) {break;}
                shootRings(31.4,1,1542);//1642
                if (gamepad.right_stick_button) {break;}

                turn(SP,4.7, 1);//4.7
                if (gamepad.right_stick_button) {break;}
                shootRings(31.4,1,1542);
                break;
            }

            case 2: {
                shootRings(31,-1,1600);//1690
                if (gamepad.right_stick_button) {break;}

                turn(SP,3.3, 1);
                if (gamepad.right_stick_button) {break;}
                shootRings(31.4,1,1600);
                if (gamepad.right_stick_button) {break;}

                turn(SP,8.9, 1);
                if (gamepad.right_stick_button) {break;}
                shootRings(31.4,1,1542);//1642
                if (gamepad.right_stick_button) {break;}

                turn(SP,14.4, 1);
                if (gamepad.right_stick_button) {break;}
                shootRings(31.4,1,1542);
                break;
            }
            case 3: {
                shootRings(31,-1,1809);
                if (gamepad.right_stick_button) {break;}

                turn(SP,17.6, 1);
                if (gamepad.right_stick_button) {break;}
                shootRings(29.5,3,1809);
                break;
            }
        }
    }


}
