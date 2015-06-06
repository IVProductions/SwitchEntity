package switchentity.handletraintraffic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.bitreactive.library.mqtt.MQTTMessage;

import switchentity.switchlogic.SwitchLogic.RequestObject;
import switchentity.switchlogic.SwitchLogic.IntersectionStatus;
import no.ntnu.item.arctis.runtime.Block;

public class HandleTrainTraffic extends Block {

	public java.util.ArrayList<RequestObject> switch_1_queue;
	public java.util.ArrayList<RequestObject> switch_2_queue;
	public java.lang.String zoneController_Id;
	public static boolean switch_1_is_busy = false;
	public static boolean switch_2_is_busy = false;
	public java.util.ArrayList<RequestObject> merge_1_queue;
	public java.util.ArrayList<RequestObject> merge_2_queue;
	public java.util.ArrayList<RequestObject> merge_3_queue;
	public static boolean merge_1_is_busy = false;
	public static boolean merge_2_is_busy = false;
	public static boolean merge_3_is_busy = false;

	public static BufferedWriter writer;
	
	public static void writeToFile(String value) throws IOException{
		writer.write(value+"\n");
	}
	
	public void init() {
		try {
			writer = new BufferedWriter(new FileWriter("computation_time_2.txt"));
		}
		catch(IOException e) {}
		
		switch_1_queue = new ArrayList<RequestObject>();
		switch_2_queue = new ArrayList<RequestObject>();
		merge_1_queue = new ArrayList<RequestObject>();
		merge_2_queue = new ArrayList<RequestObject>();
		merge_3_queue = new ArrayList<RequestObject>();
		sendToBlock("INITOK");
	}

	public void addRequestToQueue(RequestObject request) {
		//System.out.println("Adding request with id: "+request.requestId+" to list");
		if (request.intersectionId.equals("switch_1")) switch_1_queue.add(request);
		else if (request.intersectionId.equals("switch_2")) switch_2_queue.add(request);
	}

	long merge_start = 0;
	public void handleNextRequest() {
		//System.out.println("Handle next request...");
		if (!switch_1_is_busy) {
			if (switch_1_queue.size() == 0) {
				//System.out.println("There were no requests in queue1.");
				sendToBlock("UPDATESWITCHSTATUS", new IntersectionStatus("switch_1", "")); //make switch_1 status free
			}
			else {
				if (!switch_1_is_busy) {										
					switch_1_is_busy = true;
					//System.out.println("There was a request in queue1! Switch_1 is now busy.");
					RequestObject request = switch_1_queue.get(0);   //get next request from switch queue
					switch_1_queue.remove(0);						 //remove it from switch queue
					sendToBlock("HANDLEREQUEST", request);
					sendToBlock("UPDATESWITCHSTATUS", new IntersectionStatus(request.intersectionId, request.trainId));
				}
				else {/*System.out.println("Switch_1 is already attending to a train. ");*/}
			}
		}
		if (!switch_2_is_busy) {
			if (switch_2_queue.size() == 0) {
				//System.out.println("There were no requests in queue2.");
				sendToBlock("UPDATESWITCHSTATUS", new IntersectionStatus("switch_2", "")); //make switch_2 status free
			}
			else {
				if (!switch_2_is_busy) {
					switch_2_is_busy = true;
					//System.out.println("There was a request in queue2! Switch_2 is now busy.");
					RequestObject request = switch_2_queue.get(0);  
					switch_2_queue.remove(0);
					sendToBlock("HANDLEREQUEST", request);
					sendToBlock("UPDATESWITCHSTATUS", new IntersectionStatus(request.intersectionId, request.trainId));
				}
				else {/*System.out.println("Switch_2 is already attending to a train. ");*/}
			}
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


	public void sendResponse(RequestObject request) { //intersection is reserved for train
		if (request.intersectionId.contains("switch") && !request.success) { //switch operation that failed
			System.out.println("Failed to make a successfull switch with "+request.intersectionId+".TrainID: "+request.trainId+", switchIsInPos1: "+request.switchIsInPos1+", goalposOfSwitch: "+request.goalPositionOfSwitch+", destination: "+request.destinationId);	
			return;
		}
		String commandString = zoneController_Id+";"+request.trainId+";intersectionclear";
		byte[] bytes = commandString.getBytes();
		MQTTMessage command = new MQTTMessage(bytes, zoneController_Id);
		command.setQoS(0);
		sendToBlock("SEND",command);

		try {
			writeToFile("Switch_Computation_Time: "+(System.currentTimeMillis()-merge_start));
			writer.close();
			merge_start = 0;
		} catch (Exception e) {
			System.out.println("Could not write merge time to file!");
		}
		
		return;
	}


	public void terminating() {
		System.out.println("Terminating block..");
	}

	public void isStopping() {
		//System.out.println("Traffic handler block is stopping..");
		sendToBlock("STOPPING");
	}

	public void setIntersectionFree(String intersection_id) {
		System.out.println("Freeing intersection: "+intersection_id);
		switch(intersection_id) {
		case "switch_1": switch_1_is_busy = false; sendToBlock("NEXTSWITCH"); break;
		case "switch_2": switch_2_is_busy = false; sendToBlock("NEXTSWITCH"); break;
		case "merge_1": merge_1_is_busy = false; sendToBlock("NEXTMERGE"); break;
		case "merge_2": merge_2_is_busy = false; sendToBlock("NEXTMERGE"); break;
		case "merge_3": merge_3_is_busy = false; sendToBlock("NEXTMERGE"); break;
		}
	}

	public void addMergeRequestToQueue(RequestObject request) {
		if (request.intersectionId.equals("merge_1")) merge_1_queue.add(request);
		else if (request.intersectionId.equals("merge_2")) merge_2_queue.add(request);
		else if (request.intersectionId.equals("merge_3")) merge_3_queue.add(request);
	}

	public void handleNextMerge() {
		System.out.println("Handle next merge....");
		if (!merge_1_is_busy) {
			if (merge_1_queue.size() == 0) {
				//System.out.println("There were no requests in queue1.");
				sendToBlock("UPDATESWITCHSTATUS", new IntersectionStatus("merge_1", "")); //make merge_1 status free
			}
			else {
				if (!merge_1_is_busy) {
					merge_start = System.currentTimeMillis();
					merge_1_is_busy = true;
					RequestObject request = merge_1_queue.get(0);   //get next request from merge queue
					merge_1_queue.remove(0);						 //remove it from merge queue
					sendToBlock("HANDLEMERGE", request);
					sendToBlock("UPDATESWITCHSTATUS", new IntersectionStatus(request.intersectionId, request.trainId));
				}
				else {/*System.out.println("Switch_1 is already attending to a train. ");*/}
			}
		}
		if (!merge_2_is_busy) {
			if (merge_2_queue.size() == 0) {
				//System.out.println("There were no requests in queue2.");
				sendToBlock("UPDATESWITCHSTATUS", new IntersectionStatus("merge_2", "")); //make merge_2 status free
			}
			else {
				if (!merge_2_is_busy) {
					merge_2_is_busy = true;
					RequestObject request = merge_2_queue.get(0);  
					merge_2_queue.remove(0);
					sendToBlock("HANDLEMERGE", request);
					sendToBlock("UPDATESWITCHSTATUS", new IntersectionStatus(request.intersectionId, request.trainId));
				}
				else {/*System.out.println("Switch_2 is already attending to a train. ");*/}
			}
		}
		if (!merge_3_is_busy) {
			if (merge_3_queue.size() == 0) {
				sendToBlock("UPDATESWITCHSTATUS", new IntersectionStatus("merge_3", "")); //make merge_3 status free
			}
			else {
				if (!merge_3_is_busy) {
					merge_3_is_busy = true;
					RequestObject request = merge_3_queue.get(0);  
					merge_3_queue.remove(0);
					sendToBlock("HANDLEMERGE", request);
					sendToBlock("UPDATESWITCHSTATUS", new IntersectionStatus(request.intersectionId, request.trainId));
				}
				else {/*System.out.println("Switch_2 is already attending to a train. ");*/}
			}
		}
	}


}
