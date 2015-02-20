package switchentity.handletraintraffic;

import java.util.ArrayList;

import com.bitreactive.library.mqtt.MQTTMessage;

import switchentity.switchlogic.SwitchLogic.RequestObject;
import switchentity.switchlogic.SwitchLogic.SwitchAndPosition;
import no.ntnu.item.arctis.runtime.Block;

public class HandleTrainTraffic extends Block {

	public java.util.ArrayList<RequestObject> requestQueue;
	public java.lang.String zoneController_Id;
	
	public void init() {
		requestQueue = new ArrayList<RequestObject>();
		sendToBlock("INITOK");
	}

	public void addRequestToQueue(RequestObject request) {
		System.out.println("Adding request with id: "+request.requestId+" to list");
		requestQueue.add(request);
	}

	public void handleNextRequest() {
		System.out.println("Handle next request...");
		if (requestQueue.size() == 0) {
			System.out.println("There were no requests. Time for a loop then check again..");
			return;
		}
		else {
			System.out.println("There was a request! Lets handle it.");
			RequestObject request = requestQueue.get(0);
			requestQueue.remove(0);
			sendToBlock("HANDLEREQUEST", request);
		}
	}

	public void checkMotorStatus(RequestObject request) {
		//NEEDS TO USE ZONE STATES, OTHER TRAINS IN SYSTEM ETC TO FIGURE OUT WHAT RESPONSE TO SEND
		//Command motor to switch correct switch, upon response, send message
		System.out.println("Checking motor status..");
		boolean shouldBeSetToPos1 = switchShouldBeSetToPosition1(request.approachingSwitchId, request.destinationId);

		if (shouldBeSetToPos1 && request.switchIsInPos1) {
			request.success = true;
			sendToBlock("SENDRESPONSE",request);
			return;
		}
		if ((shouldBeSetToPos1 && !request.switchIsInPos1)) {
			request.goalPositionOfSwitch = "position1";
			sendToBlock("COMMANDMOTOR",request);
			return;
		}
		if (!shouldBeSetToPos1 && !request.switchIsInPos1) {
			request.success = true;
			sendToBlock("SENDRESPONSE",request);
			return;
		}
		if ((!shouldBeSetToPos1 && request.switchIsInPos1)) {
			request.goalPositionOfSwitch = "position2";
			sendToBlock("COMMANDMOTOR",request);
			return;
		}
	}
		
	
	public void sendResponse(RequestObject request) {
		String response;
		if (request.success) {
			response = zoneController_Id+";"+request.trainId+";ok;"+request.requestId;   //continue
		}
		else {
			response = zoneController_Id+";"+request.trainId+";stop;"+request.requestId; //stop train
		}
		byte[] bytes = response.getBytes();
	    String topic = zoneController_Id;
		MQTTMessage message = new MQTTMessage(bytes, topic);
		message.setQoS(0);
		System.out.println("Sending response to train: "+response);
		sendToBlock("SEND",message);
	}
	
	public static boolean switchShouldBeSetToPosition1(String switch_id, String destination_Id) {
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

	public void terminating() {
		System.out.println("Terminating block..");
	}

	public void isStopping() {
		System.out.println("Traffic handler block is stopping..");
		sendToBlock("STOPPING");
	}


}
