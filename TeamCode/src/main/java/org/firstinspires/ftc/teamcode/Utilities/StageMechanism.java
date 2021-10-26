package org.firstinspires.ftc.teamcode.Utilities;

import com.qualcomm.robotcore.hardware.DcMotor;

public class StageMechanism {
    private DcMotor stageReel = null;
    private int maxPosition = 4000;

    public StageMechanism(DcMotor MyStageMotor) {
        stageReel = MyStageMotor;
        stageReel.setDirection(DcMotor.Direction.FORWARD);
        stageReel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        stageReel.setTargetPosition(0);
        stageReel.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    public void MoveStagePosition(int stagePosition) {
        if (stagePosition < 0) {
            stagePosition = 0;
        } else if (stagePosition > maxPosition) {
            stagePosition = maxPosition;
        }
        stageReel.setTargetPosition(stagePosition);

        if (stageReel.getCurrentPosition() != stagePosition) {
            stageReel.setPower(1.0);
        } else {
            stageReel.setPower(0);
        }
    }

    public void MoveToTargetPosition(int stagePosition) {
        while (stageReel.getCurrentPosition() != stagePosition){
            if (stagePosition < 0) {
                stagePosition = 0;
            } else if (stagePosition > maxPosition) {
                stagePosition = maxPosition;
            }
            stageReel.setTargetPosition(stagePosition);
            stageReel.setPower(1.0);
        }
        stageReel.setPower(0);
    }

}
