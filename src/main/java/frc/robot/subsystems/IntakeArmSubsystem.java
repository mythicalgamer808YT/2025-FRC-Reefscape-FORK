package frc.robot.subsystems;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;

import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.IntakeArmConsts;
import frc.robot.States.ElevatorStates;
import frc.robot.States.IntakeStates;
import frc.robot.commands.IntakeArmCommand;

public class IntakeArmSubsystem extends SubsystemBase{
    private final TalonFX armMotor;
    private final SparkMax intakeMotor;
    private final DutyCycleEncoder armEncoder;
    private final ArmFeedforward armFeedforward;
    private final PIDController armPID;

    //solution pairs (height, theta) calculated in states
    private boolean inBounds;
    private double motorOutput;
    private double armAngle;

    private IntakeArmCommand commands;

    public IntakeArmSubsystem() {
        armMotor = new TalonFX(IntakeArmConsts.armMotorID);
        intakeMotor = new SparkMax(IntakeArmConsts.suckMotorID, MotorType.kBrushless);
        armEncoder = new DutyCycleEncoder(IntakeArmConsts.encoderID);

        armFeedforward = new ArmFeedforward(IntakeArmConsts.kS, IntakeArmConsts.kG, IntakeArmConsts.kV);
        armPID = new PIDController(IntakeArmConsts.kP, IntakeArmConsts.kI, IntakeArmConsts.kD);

        armMotor.setNeutralMode(NeutralModeValue.Brake);
        commands = new IntakeArmCommand(this);
    }

    @Override
    public void periodic() {

        armAngle = getAngle();

        if(armAngle > ElevatorStates.MAX_ABS.angle) {
            armMotor.set(-0.1);
            return;
        }
        //armMotor.set(motorOutput);

        if(armAngle < ElevatorStates.MIN_ABS.angle) {
            armMotor.set(0.1);
            return; 
        }

        motorOutput = armPID.calculate(getAngle()) 
            + armFeedforward.calculate(Units.degreesToRadians(armAngle - 158), armMotor.getVelocity().getValueAsDouble());// (armPID.getSetpoint().velocity - lastSpeed) / (Timer.getFPGATimestamp() - timeStamp));
        //armMotor.set(motorOutput);
        setSmartdashboard();
    }

    public void setAngle(double angle) {
        armPID.setSetpoint(angle);
    }

    public void setIntakeStates(double speed) {
        intakeMotor.set(speed);
    }

    public void resetIntakeArm() {
        armPID.setSetpoint(getAngle());
    }

    public void resetIntake() {
        intakeMotor.set(0);
    }

    public boolean atAngle() {
        return armPID.atSetpoint();
    }

    public double getAngle() {
        return Units.rotationsToDegrees(armEncoder.get() - Units.degreesToRotations(87));
    }

    // used to disable everythinzg 
    private void disableSubsystem() {
        armMotor.disable();
        intakeMotor.disable();
    }

    private void setSmartdashboard() {
        SmartDashboard.putNumber("Arm Subsystem position", getAngle());
        SmartDashboard.putNumber("Arm Subsystem motor speed", motorOutput);
        SmartDashboard.putBoolean("Arm Subsystem inbounds", inBounds);
        SmartDashboard.putNumber("Arm Subsystem arm position goal", armPID.getSetpoint());
    }

    public IntakeArmCommand getCommands() {
        return commands;
    }
}
