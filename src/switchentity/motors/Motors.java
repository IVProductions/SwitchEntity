package switchentity.motors;

import switchentity.switchlogic.SwitchLogic.SwitchAndPosition;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import no.ntnu.item.arctis.runtime.Block;

public class Motors extends Block {

	public static EV3LargeRegulatedMotor switch1_motor;
	public static EV3LargeRegulatedMotor switch2_motor;
	public static EV3LargeRegulatedMotor switch3_motor;
	public static EV3LargeRegulatedMotor switch4_motor;
	
	public void initMotors() {
		Port a = LocalEV3.get().getPort("A");
		Port b = LocalEV3.get().getPort("B");
		Port c = LocalEV3.get().getPort("C");
		Port d = LocalEV3.get().getPort("D");
		switch1_motor = new EV3LargeRegulatedMotor(a);
		switch2_motor = new EV3LargeRegulatedMotor(b);
		switch3_motor = new EV3LargeRegulatedMotor(c);
		switch4_motor = new EV3LargeRegulatedMotor(d);
		sendToBlock("INITOK");
	}
	
	
	public SwitchAndPosition setMotorToPosition(SwitchAndPosition switchAndPos) {
		String switch_Id = switchAndPos.switch_Id;
		String position = switchAndPos.position;
		EV3LargeRegulatedMotor motor = findMotorForSwitchId(switch_Id);
		if (motor == null)  return switchAndPos;
		if (position.equals("position1")) {
			motor.rotateTo(0);
		}
		else {
			motor.rotateTo(-180);
		}
		switchAndPos.success = true;
		return switchAndPos;
	}
	
	public static EV3LargeRegulatedMotor findMotorForSwitchId(String switchId){
		switch(switchId) {
		case "switch_1":
			return switch1_motor;
		case "switch_2":
			return switch2_motor;
		case "switch_3":
			return switch3_motor;
		case "switch_4": 
			return switch4_motor;
		}
		return null;
	}

	public void setAllMotorsToPosition(String position) {
		int angle = position.equals("position1") ? 0 : -180;
		try {
			switch1_motor.rotateTo(angle);
		}
		catch(Exception e) {
			System.out.println("Motor in port A could not be found");
		}
		try {
			switch2_motor.rotateTo(angle);
		}
		catch(Exception e) {
			System.out.println("Motor in port B could not be found");
		}
		try {
			switch3_motor.rotateTo(angle);
		}
		catch(Exception e) {
			System.out.println("Motor in port C could not be found");
		}
		try {
			switch4_motor.rotateTo(angle);
		}
		catch(Exception e) {
			System.out.println("Motor in port D could not be found");
		}
	}

}
