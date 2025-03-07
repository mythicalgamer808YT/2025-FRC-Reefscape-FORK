package frc.robot.commands;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.States.ClimbStates;
import frc.robot.subsystems.ClimbSubsystem;

public class ClimbCommand {
    private final ClimbSubsystem climb;

    public ClimbCommand(ClimbSubsystem climb) {
        this.climb = climb;
        setClimb(ClimbStates.STOP);
    }
    
    public Command setClimb(ClimbStates state) {
        SmartDashboard.putString("Climb State", state.toString());
        return climb.run(() -> climb.setClimb(state.speed));
    }

}
]