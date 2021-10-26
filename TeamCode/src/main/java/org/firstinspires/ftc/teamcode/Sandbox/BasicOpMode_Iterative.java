/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.Sandbox;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Utilities.StageMechanismPID;

/**
 * This file contains an example of an iterative (Non-Linear) "OpMode".
 * An OpMode is a 'program' that runs in either the autonomous or the teleop period of an FTC match.
 * The names of OpModes appear on the menu of the FTC Driver Station.
 * When an selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all iterative OpModes contain.
 *
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

//@TeleOp(name="Basic: Iterative OpMode", group="Iterative Opmode")

public class BasicOpMode_Iterative extends OpMode {
    private DcMotor stageReel = null;
    private Servo myServo = null;
    private CRServo stageServo = null;
    private int stagePosition = 0;
    private StageMechanismPID myStage = null;

    /*
     * Code to run ONCE when the driver hits INIT
     */
    @Override
    public void init() {
        stageReel = hardwareMap.get(DcMotor.class, "stage_reel");
        myStage = new StageMechanismPID(stageReel,  0.005, 0,0.005, .00005);
        myServo = hardwareMap.get(Servo.class, "lfh");
        myServo.setPosition(0);
        stageServo = hardwareMap.get(CRServo.class, "mini_reel");
        stageServo.setDirection(CRServo.Direction.FORWARD);
    }

    /*
     * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
     */
    //@Override
    /*public void init_loop() {
    }*/

    /*
     * Code to run ONCE when the driver hits PLAY
     */
    @Override
    public void start() {

    }

    /*
     * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
     */
    @Override
    public void loop() {
        /*This is the implementation of our vertical stage movement class*/
        /*stagePosition = stagePosition + (int) ((-gamepad1.left_stick_y) * 20);
        /*myStage.MoveStagePosition(stagePosition);*/

        /*This is the implementation of vertical stage PID movement class*/
        /*started with p=0.03,i=0,d=0*/
        /*myStage.MoveStagePID(-gamepad1.left_stick_y, stageReel.getCurrentPosition(),  0.005, 0,0.005);*/
        myStage.MoveStagePIDF(-gamepad1.left_stick_y, stageReel.getCurrentPosition());

        /*This is how to move a servo porportional to a stick input*/
        /*myServo.setPosition((gamepad1.right_stick_y + 1.0)/2.0);*/

        /*This is how to move a servo to fixed positions using buttons*/
        if (gamepad1.a) {
            myServo.setPosition(0);
        }
        if (gamepad1.y) {
            myServo.setPosition(1);
        }

        /*This is for moving the stage servo*/
        if (gamepad1.right_stick_y != 0) {
            stageServo.setPower(gamepad1.right_stick_y);
        } else {
            stageServo.setPower(0);
        }

    }

    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }

}
