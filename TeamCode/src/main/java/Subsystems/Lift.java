package Subsystems;


import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.controller.PIDController;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;

@Config
public class Lift {
    private DcMotorEx liftMotorR;
    private DcMotorEx liftMotorL;
    TouchSensor liftTouch;
    private boolean lift_is_reset = false;
    private double lift_target;
    public static double lift_target_change;
    public static double lift_max_position;
    private static PIDController lift_controller;
    public static double lift_kP = 0;
    public static double lift_kI = 0;
    public static double lift_kD = 0;
    public static double lift_kH = 0;

    FtcDashboard dashboard = FtcDashboard.getInstance();
    Telemetry dashboardTelemetry = dashboard.getTelemetry();

    public void init(@NonNull HardwareMap hwMap){
        this.liftMotorR = hwMap.get(DcMotorEx.class, "liftMotor");
        this.liftMotorR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.liftMotorR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        this.liftMotorR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        this.liftMotorL = hwMap.get(DcMotorEx.class, "liftMotor2");
        this.liftMotorL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.liftMotorL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        this.liftMotorL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.liftMotorL.setDirection(DcMotorSimple.Direction.REVERSE);

        liftTouch = hwMap.get(TouchSensor.class,"liftReset" );

    }

    public void liftReset(){
        if (!liftTouch.isPressed() & !lift_is_reset){
            this.liftMotorR.setPower(-0.2);
            this.liftMotorL.setPower(-0.2);
            dashboardTelemetry.addData("shoulder is reset", lift_is_reset);
            if (liftTouch.isPressed()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                this.liftMotorR.setPower(0);
                this.liftMotorL.setPower(0);
                lift_is_reset = true;
                dashboardTelemetry.addData("shoulder is reset", lift_is_reset);
            }
        }
        dashboardTelemetry.update();
    }

    public void Ascend(double direction){
        this.lift_target = lift_target + direction * lift_target_change;
        if (lift_target< 0){
            lift_target = 0;
        }else if (lift_target > lift_max_position){
            lift_target = lift_max_position;
        }

        lift_calc(lift_target);
    }

    public void lift_calc(double target){
        double output = 0.0;
        double shoulder_pos = this.liftMotorR.getCurrentPosition();
        if (Math.abs(target-shoulder_pos) > 5) {
            output = lift_controller.calculate(shoulder_pos, target);
            output = PID_TEst.limiter(output, 1.0);
        }

        output = output + lift_kH;

        this.liftMotorR.setPower(output);
        this.liftMotorL.setPower(output);
    }
}
