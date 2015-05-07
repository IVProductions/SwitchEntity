package switchentity.handletraintraffic;

import java.util.ArrayList;

import com.bitreactive.library.mqtt.MQTTMessage;

import switchentity.switchlogic.SwitchLogic.RequestObject;
import switchentity.switchlogic.SwitchLogic.SwitchStatus;
import no.ntnu.item.arctis.runtime.Block;

public class HandleTrainTraffic extends Block {

	public java.util.ArrayList<RequestObject> switch_1_queue;
	public java.util.ArrayList<RequestObject> switch_2_queue;
	public java.lang.String zoneController_Id;
	public static boolean switch_1_is_busy = false;
	public static boolean switch_2_is_busy = false;

	public void init() {
		switch_1_queue = new ArrayList<RequestObject>();
		switch_2_queue = new ArrayList<RequestObject>();
		sendToBlock("INITOK");
	}

	public void addRequestToQueue(RequestObject request) {
		//System.out.println("Adding request with id: "+request.requestId+" to list");
		if (request.approachingSwitchId.equals("switch_1")) switch_1_queue.add(request);
		else if (request.approachingSwitchId.equals("switch_2")) switch_2_queue.add(request);
	}

	public void handleNextRequest() {
		//System.out.println("Handle next request...");
		if (switch_1_queue.size() == 0) {
			//System.out.println("There were no requests in queue1.");
			sendToBlock("UPDATESWITCHSTATUS", new SwitchStatus("switch_1", "")); //make switch_1 status free
		}
		else {
			if (!switch_1_is_busy) {
				switch_1_is_busy = true;
				//System.out.println("There was a request in queue1! Switch_1 is now busy.");
				RequestObject request = switch_1_queue.get(0);   //get next request from switch queue
				switch_1_queue.remove(0);						 //remove it from switch queue
				sendToBlock("HANDLEREQUEST", request);
				sendToBlock("UPDATESWITCHSTATUS", new SwitchStatus(request.approachingSwitchId, request.trainId));
			}
			else {/*System.out.println("Switch_1 is already attending to a train. ");*/}
		}
		if (switch_2_queue.size() == 0) {
			//System.out.println("There were no requests in queue2.");
			sendToBlock("UPDATESWITCHSTATUS", new SwitchStatus("switch_2", "")); //make switch_2 status free
		}
		else {
			if (!switch_2_is_busy) {
				switch_2_is_busy = true;
				//System.out.println("There was a request in queue2! Switch_2 is now busy.");
				RequestObject request = switch_2_queue.get(0);  
				switch_2_queue.remove(0);
				sendToBlock("HANDLEREQUEST", request);
				sendToBlock("UPDATESWITCHSTATUS", new SwitchStatus(request.approachingSwitchId, request.trainId));
			}
			else {/*System.out.println("Switch_2 is already attending to a train. ");*/}
		}
	}

	public void checkMotorStatus(RequestObject request) {
		//NEEDS TO USE ZONE STATES, OTHER TRAINS IN SYSTEM ETC TO FIGURE OUT WHAT RESPONSE TO SEND
		//Command motor to switch correct switch, upon response, send message
		//System.out.println("Checking motor status..");

		if (request.goalPositionOfSwitch.equals("position1") && request.switchIsInPos1) {
			request.success = true;
			sendToBlock("SENDRESPONSE",request);
			return;
		}
		if ((request.goalPositionOfSwitch.equals("position1") && !request.switchIsInPos1)) {
			request.goalPositionOfSwitch = "position1";
			sendToBlock("COMMANDMOTOR",request);
			return;
		}
		if (!request.goalPositionOfSwitch.equals("position1") && !request.switchIsInPos1) {
			request.success = true;
			sendToBlock("SENDRESPONSE",request);
			return;
		}
		if ((!request.goalPositionOfSwitch.equals("position1") && request.switchIsInPos1)) {
			request.goalPositionOfSwitch = "position2";
			sendToBlock("COMMANDMOTOR",request);
			return;
		}
	}


	public void sendResponse(RequestObject request) {		//String response to train -> switch is now ready to be passed
		if (request.success) {
			String commandString = zoneController_Id+";"+request.trainId+";switchclear";
			byte[] bytes = commandString.getBytes();
			MQTTMessage command = new MQTTMessage(bytes, zoneController_Id);
			command.setQoS(0);
			//System.out.println(request.approachingSwitchId+" is ready to be passed: "+commandString);
			sendToBlock("SEND",command);
		}
		else {
			System.out.println("Failed to make a successfull switch with "+request.approachingSwitchId+".TrainID: "+request.trainId+", switchIsInPos1: "+request.switchIsInPos1+", goalposOfSwitch: "+request.goalPositionOfSwitch+", destination: "+request.destinationId);
		}
	}


	public void terminating() {
		System.out.println("Terminating block..");
	}

	public void isStopping() {
		//System.out.println("Traffic handler block is stopping..");
		sendToBlock("STOPPING");
	}

	public void setSwitchFree(String switch_Id) {
		//System.out.println("Freeing "+switch_Id+"!");
		switch(switch_Id) {
		case "switch_1": switch_1_is_busy = false;
		case "switch_2": switch_2_is_busy = false;
		}
	}


}
