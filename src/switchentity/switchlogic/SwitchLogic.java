package switchentity.switchlogic;

import java.io.Console;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

import com.bitreactive.library.mqtt.MQTTConfigParam;
import com.bitreactive.library.mqtt.MQTTMessage;
import com.bitreactive.library.mqtt.robustmqtt.RobustMQTT.Parameters;

import no.ntnu.item.arctis.runtime.Block;

public class SwitchLogic extends Block {

	public static class SwitchAndPosition {
		public String switch_Id;
		public String position;
		public boolean success = false;
	
		public SwitchAndPosition(String switch_Id, String position) {
			this.switch_Id = switch_Id;
			this.position = position;
		}	
	}
	
	public static final String switch1_id = "switch_1";
	public static final String switch2_id = "switch_2";
	public static final String switch3_id = "switch_3";
	public static final String switch4_id = "switch_4";
	
	//public static EV3TouchSensor switch1_sensor;
	//public static EV3TouchSensor switch2_sensor;

	public static java.lang.String zoneController_Id = "zonecontroller_1";
	private Boolean[] isInPosition1 = new Boolean[4]; //TESTING


	public void init() {
		isInPosition1[0] = true;isInPosition1[1] = true;isInPosition1[2] = true;isInPosition1[3] = true; //TESTING

		//switch1_sensor = new EV3TouchSensor(SensorPort.S1);
		//switch2_sensor = new EV3TouchSensor(SensorPort.S2);
		/*int sampleSize = switch1_sensor.sampleSize();
		float[] sample = new float[sampleSize];
		switch1_sensor.fetchSample(sample, 0);

		if(sample[0] == 0.0){
			// Touch sensor is NOT pressed
			System.out.println("Sensor 1 is NOT Pressed");
		}
		else {
			// Touch sensor is pressed
			System.out.println("Sensor 1 is Pressed");
		}

		sampleSize = switch2_sensor.sampleSize();
		sample = new float[sampleSize];
		switch2_sensor.fetchSample(sample, 0);

		if(sample[0] == 0.0){
			// Touch sensor is NOT pressed
			System.out.println("Sensor 2 is NOT Pressed");
		}
		else {
			// Touch sensor is pressed
			System.out.println("Sensor 2 is Pressed");
		}*/
		System.out.println("MQTT ready...");
	}

	public Parameters initMQTTParam() {		
		MQTTConfigParam m = new MQTTConfigParam("dev.bitreactive.com");
		m.addSubscribeTopic(zoneController_Id);
		Parameters p = new Parameters(m);
		return p;
	}

	public void handleMessage(MQTTMessage mqttMessage) {
		System.out.println("Received command from controller");
		String initialRequestString = new String(mqttMessage.getPayload());
		System.out.println(initialRequestString);
		String[] requestList = initialRequestString.split(";");
		String train_Id = requestList[0];
		String sentToZoneController = requestList[1];
		String switch_Id = requestList[2];
		String destination_Id = requestList[3]; //used to be 2
		if (train_Id == null || !sentToZoneController.equals(zoneController_Id) || switch_Id == null || destination_Id == null) return;

		int index = getIntForSwitchId(switch_Id);
		boolean switchIsInPosition1 = isInPosition1[index];
		
		//SWITCH CONTROLLER HANDLING
		if (train_Id.equals("controller") && (destination_Id.equals("position1") || destination_Id.equals("position2"))) {
			if ((destination_Id.equals("position1") && switchIsInPosition1) || (destination_Id.equals("position2") && !switchIsInPosition1)) return;
			sendToBlock("SETSWITCHTOPOSITION",new SwitchAndPosition(switch_Id, destination_Id));
			return;
		}
		if (train_Id.equals("controller") && destination_Id.equals("terminate")) {
			sendToBlock("TERMINATE","position1");
			return;
		}
		//SWITCH CONTROLLER HANDLING END

		/*
			EV3TouchSensor relevantSensor = findSensorForSwitchId(switch_id);		
			if (relevantMotor == null) {
				System.out.println("The MOTOR for a switch with Id = "+switch_id+" could not be found!");
				return;
			}

			if (relevantSensor == null) {
				System.out.println("The SENSOR for a switch with Id = "+switch_id+" could not be found!");
				return;
			}
			int sampleSize = relevantSensor.sampleSize();
			float[] sample = new float[sampleSize];
			relevantSensor.fetchSample(sample, 0);

			if(sample[0] == 0.0){
				// Touch sensor is NOT pressed
				System.out.println("Switch "+switch_id+" has been commanded to switch to position 1");			
				relevantMotor.rotateTo(0);	 //0
			}
			else {
				// Touch sensor is pressed
				System.out.println("Switch "+switch_id+" has been commanded to switch to position 2");			
				relevantMotor.rotateTo(-180);	 //-45		
			}
		 */

		
		boolean setToPos1 = setSwitchToPosition1(switch_Id, destination_Id);

		if (setToPos1 && switchIsInPosition1) return;
		if ((setToPos1 && !switchIsInPosition1)) {
			sendToBlock("SETSWITCHTOPOSITION",new SwitchAndPosition(switch_Id, "position1"));
			return;
		}
		if (!setToPos1 && !switchIsInPosition1) return;
		if ((!setToPos1 && switchIsInPosition1)) {
			sendToBlock("SETSWITCHTOPOSITION",new SwitchAndPosition(switch_Id, "position2"));
			return;
		}
	}

	/*private EV3TouchSensor findSensorForSwitchId(String switch_id) {
		switch(switch_id) {
		case "switch_1":
			return switch1_sensor;
		case "switch_2":
			return switch2_sensor;
		}
		return null;
	}*/

	public static boolean setSwitchToPosition1(String switch_id, String destination_Id) {
		switch(switch_id) {
		case "switch_1":
			switch(destination_Id) {
			case "station_1": return true;
			case "station_2": return false;
			case "station_3": return false;
			}
		case "switch_2":
			switch(destination_Id) {
			case "station_1": return true;
			case "station_2": return false;
			case "station_3": return true;
			}
		case "switch_3":
			switch(destination_Id) {
			case "station_1": return false;
			case "station_2": return true;
			case "station_3": return true;
			}
		case "switch_4":
			switch(destination_Id) {
			case "station_1": return true;
			case "station_2": return true;
			case "station_3": return false;
			}
		}
		return true;
	}


	public static int getIntForSwitchId(String switchId) {
		switch(switchId) {
		case "switch_1":
			return 0;
		case "switch_2":
			return 1;
		case "switch_3":
			return 2;
		case "switch_4": 
			return 3;
		}
		return -1;
	}

	public void updateSwitchPosition(SwitchAndPosition switchAndPos) {
		if (switchAndPos.success == false) return;
		String switch_Id = switchAndPos.switch_Id;
		String position = switchAndPos.position;
		int index = getIntForSwitchId(switch_Id);
		isInPosition1[index] = position.equals("position1") ? true : false;
	}

	public void initOk() {
		System.out.println("-------------------------------");
		System.out.println("-> "+zoneController_Id + " IS READY <-");
		System.out.println("-------------------------------");
	}

}
