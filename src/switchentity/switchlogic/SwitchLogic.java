package switchentity.switchlogic;


import java.util.HashMap;

import lejos.hardware.Button;
import lejos.hardware.Sound;

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

	public static class RequestObject {
		public String trainId;
		public String approachingSwitchId;
		public String destinationId;
		public String requestId;
		public boolean switchIsInPos1;
		public String goalPositionOfSwitch;
		public boolean success;

		public RequestObject(String trainId, String approachingSwitchId, String destinationId, String requestId) {
			this.trainId = trainId;
			this.approachingSwitchId = approachingSwitchId;
			this.destinationId = destinationId;
			this.requestId = requestId;
		}
		public RequestObject(String switchId, String goalPositionOfSwitch) {
			this.approachingSwitchId = switchId;
			this.goalPositionOfSwitch = goalPositionOfSwitch;
		}
	}

	public static java.lang.String zoneController_Id = "zonecontroller_4";
	public static final String switch1_id = "switch_1";
	public static final String switch2_id = "switch_2";
	public static final String switch3_id = "switch_3";
	public static final String switch4_id = "switch_4";

	private int train_length = 10;
	private Boolean[] isInPosition1 = new Boolean[4];
	HashMap<String, Integer> train_to_track_mapping = new HashMap<String, Integer>();
	HashMap<String, Integer> train_to_last_track_mapping = new HashMap<String, Integer>();

	int track_1_length = 47;
	int track_2_length = 41;
	int track_3_length = 21;
	int track_4_length = 16;
	int track_5_length = 18;
	int track_6_length = 90;
	int track_7_length = 18;
	int track_8_length = 16;
	int track_9_length = 11;
	int track_10_length = 34;
	int track_11_length = 40;
	int track_12_length = 16;
	int track_13_length = 18;
	int track_14_length = 20;
	int track_15_length = 22;
	int track_16_length = 33;
	int track_17_length = 73;
	int track_18_length = 27;
	int track_19_length = 27;
	int track_20_length = 16;
	int track_21_length = 18;
	int track_22_length = 62;


	//TRACK LAYOUT
	int sleeperCounter = 0;
	HashMap<Integer, Boolean> track_1 = initTrackOverview(track_1_length);
	HashMap<Integer, Boolean> track_2 = initTrackOverview(track_2_length);
	HashMap<Integer, Boolean> track_3 = initTrackOverview(track_3_length);
	HashMap<Integer, Boolean> track_4 = initTrackOverview(track_4_length);
	HashMap<Integer, Boolean> track_5 = initTrackOverview(track_5_length);
	HashMap<Integer, Boolean> track_6 = initTrackOverview(track_6_length);
	HashMap<Integer, Boolean> track_7 = initTrackOverview(track_7_length);
	HashMap<Integer, Boolean> track_8 = initTrackOverview(track_8_length);
	HashMap<Integer, Boolean> track_9 = initTrackOverview(track_9_length);
	HashMap<Integer, Boolean> track_10 = initTrackOverview(track_10_length);
	HashMap<Integer, Boolean> track_11 = initTrackOverview(track_11_length);
	HashMap<Integer, Boolean> track_12 = initTrackOverview(track_12_length);
	HashMap<Integer, Boolean> track_13 = initTrackOverview(track_13_length);
	HashMap<Integer, Boolean> track_14 = initTrackOverview(track_14_length);
	HashMap<Integer, Boolean> track_15 = initTrackOverview(track_15_length);
	HashMap<Integer, Boolean> track_16 = initTrackOverview(track_16_length);
	HashMap<Integer, Boolean> track_17 = initTrackOverview(track_17_length);
	HashMap<Integer, Boolean> track_18 = initTrackOverview(track_18_length);
	HashMap<Integer, Boolean> track_19 = initTrackOverview(track_19_length);
	HashMap<Integer, Boolean> track_20 = initTrackOverview(track_20_length);
	HashMap<Integer, Boolean> track_21 = initTrackOverview(track_21_length);
	HashMap<Integer, Boolean> track_22 = initTrackOverview(track_22_length);
	//TRACK LAYOUT END

	public void init() {
		System.out.println("MQTT ready...");
	}

	public HashMap<Integer, Boolean> initTrackOverview(int length) {
		HashMap<Integer, Boolean> track = new HashMap<Integer, Boolean>();
		for (int i = 1; i <= length; i++) {
			track.put(i, false);
		}
		sleeperCounter+=length;
		return track;
	}

	public Parameters initMQTTParam() {		
		isInPosition1[0] = true;isInPosition1[1] = true;isInPosition1[2] = true;isInPosition1[3] = true; //TESTING

		MQTTConfigParam m = new MQTTConfigParam("192.168.0.100");
		m.addSubscribeTopic(zoneController_Id);
		Parameters p = new Parameters(m);
		return p;
	}

	public void handleMessage(MQTTMessage mqttMessage) {
		String initialRequestString = new String(mqttMessage.getPayload());
		//System.out.println(initialRequestString);
		String[] requestList = initialRequestString.split(";");
		String train_Id = requestList[0];
		String sentToZoneController = requestList[1];
		String sentToOldZoneController = requestList[2];

		if (train_Id.equals(zoneController_Id)) return; //received message from itself -> ignore
		if (train_Id != null && sentToZoneController.equals(zoneController_Id) && sentToOldZoneController.equals("unsubscribe")) {
			//LOGIC FOR REMOVING TRAIN FROM ZONE AND CLEARING UP TRACKS
			System.out.println(train_Id+" has unsubscribed!");
			int trackIdThatTrainIsLeaving = train_to_track_mapping.get(train_Id);
			HashMap<Integer, Boolean> trackThatTrainIsLeaving = getTrackMapForNumber(trackIdThatTrainIsLeaving);
			if (trackThatTrainIsLeaving != null) {
				//CLEAN UP SLEEPERS
				try {
					for (int i = 1; i <= trackThatTrainIsLeaving.size(); i++) {
						trackThatTrainIsLeaving.put(i, false);
					}
				} catch (Exception e) {
					System.out.println("Could not unoccupy all sleepers in last track..");
				}
				//CLEAN UP SLEEPERS END
				/*for (int i = trackThatTrainIsLeaving.size() - 10; i <= trackThatTrainIsLeaving.size(); i++) {
					try {
						trackThatTrainIsLeaving.put(i, false);
					} catch (Exception e) {
						System.out.println("Unsubscribing - Could not set sleeper "+i+" in track "+trackIdThatTrainIsLeaving +" unoccupied");
					}
				}*/
			}
			train_to_track_mapping.remove(train_Id);
			if (train_to_track_mapping.isEmpty()) System.out.println("There are currently no trains in this zone.");
			return;
		}

		String messageContext = requestList[3];
		boolean relevantCtrl = false;
		if (sentToZoneController.equals(zoneController_Id) || sentToOldZoneController.equals(zoneController_Id)) relevantCtrl = true;
		if (train_Id == null || !relevantCtrl || messageContext == null) return;

		//SWITCH CONTROLLER HANDLING 
		if (train_Id.equals("controller") && sentToZoneController.equals(zoneController_Id) && requestList[2].equals("gettrackstatus") && requestList[3] != null) {
			String result = "";
			int trackId = 0;
			try {
				trackId = Integer.parseInt(requestList[3]);
				HashMap<Integer, Boolean> track = getTrackMapForNumber(trackId);
				for (int i = 1; i <= track.size(); i++) {
					if (track.get(i)) {
						result+="Sleeper_"+i+" --- ";
					}
				}
			} catch (Exception e) {
				System.out.println("Could not handle track status request from controller");
				result = "Could not handle track status request from controller";
			}
			if (result.equals("")) result = "Track "+ trackId +" is unoccupied for "+zoneController_Id;
			String commandString = zoneController_Id+";controller;"+result;
			byte[] bytes = commandString.getBytes();
			MQTTMessage command = new MQTTMessage(bytes, "controller");
			command.setQoS(0);
			sendToBlock("SENDMESSAGETOTRAIN", command);
			return;
		}
		if (train_Id.equals("controller") && (requestList[3].equals("position1") || requestList[3].equals("position2"))) {
			String switch_Id = requestList[2];
			String destination = requestList[3];
			int index = getIntForSwitchId(requestList[2]); //switch id 
			boolean switchIsInPosition1 = isInPosition1[index];
			if ((destination.equals("position1") && switchIsInPosition1) || (destination.equals("position2") && !switchIsInPosition1)) return;
			sendToBlock("SETSWITCHTOPOSITION",new RequestObject(switch_Id, destination));
			return;
		}
		if (train_Id.equals("controller") && requestList[3].equals("terminate")) {
			sendToBlock("TERMINATE","position1");
			return;
		}
		//SWITCH CONTROLLER HANDLING END

		if (messageContext.equals("requeststart")) {
			System.out.println("Received requeststart: "+initialRequestString);
			int sleeperNumber = 0;
			double currentSpeed = 0.0;
			String collisionCommand = "";
			try {
				sleeperNumber = Integer.parseInt(requestList[4]);
				currentSpeed = 0.2; //Set a bogus speed so that a small number of sleepers in front of train is checked
				Integer trackNumber = train_to_track_mapping.get(train_Id);
				int trackLength = getTrackLength(trackNumber);
				if (trackNumber == null) {
					System.out.println("Tracknumber not found for train");return;
				}
				if (sleeperNumber > trackLength) sleeperNumber = trackLength;
				if (trackLength == 0 ||sleeperNumber == 0 || currentSpeed == 0.0 || sleeperNumber > trackLength) {System.out.println("Something went wrong here..s");return;}
				HashMap<Integer, Boolean> currentTrackMapping = getTrackMapForNumber(trackNumber);
				collisionCommand = collisionDetection(trackNumber, trackLength, sleeperNumber, currentSpeed,currentTrackMapping); //run detection before updating sleepers, so that late messages don't screw things up
				System.out.println("TrainId: "+train_Id+", trackNumber: "+trackNumber+", trackLength: "+trackLength+", Will send response  back: " + collisionCommand);
				if (collisionCommand.equals("")) unoccupySleepers(trackNumber, sleeperNumber, train_Id, currentTrackMapping); //the train can start up again, make three sleepers behind the train free
			}
			catch(Exception e) {
				System.out.println("Could not parse the sleeper number or the current speed");
				return;
			}
			sendClearanceResponse(collisionCommand, train_Id);
			return;
		}
		
		if (messageContext.equals("newzone")) {  //TRAIN ENTERED NEW ZONE
			String currentTrack_Id = requestList[4];
			String destination_Id = requestList[5];
			//if (currentTrack_Id == null || destination_Id == null) return;

			//UPDATE STATUS OF THE TRACK THAT THE TRAIN IS ON => PLACE THE TRAIN IN THE GIVEN TRACK IN A GIVEN POSITION
			//int newTrack = getNewTrackIdUponZoneSwitch(Integer.parseInt(oldTrack_Id));
			train_to_track_mapping.put(train_Id, Integer.parseInt(currentTrack_Id));
			System.out.println("New Zone:      "+initialRequestString);
			/*String request_id = requestList[5];
			RequestObject request = new RequestObject(train_Id, switch_Id, destination_Id, request_id);
			System.out.println("Sending request to HandleTrainTraffic block..");
			sendToBlock("HANDLETRAINTRAFFIC", request);*/
			return;
		}
		else if (messageContext.equals("colorsleeper")) { //TRAIN PASSED COLOR SLEEPER INSIDE ZONE
			String sleeperColor = requestList[4];
			String destination_Id = requestList[5];
			String request_Id = requestList[6];
			//if (sleeperColor == null || destination_Id == null) return;

			//UPDATE STATUS OF THE TRACK THAT THE TRAIN IS ON => FIND THE NEW TRACK OF THE TRAIN BASED ON THE SLEEPERCOLOR AND THE LAST KNOWN TRACK OF THE TRAIN
			int currentTrack_Id = train_to_track_mapping.get(train_Id);
			if (currentTrack_Id == 0) return;
			int newTrackId = getNewTrackIdFromColor(currentTrack_Id, sleeperColor);
			if (newTrackId == 0) return;
			train_to_last_track_mapping.put(train_Id,currentTrack_Id);
			train_to_track_mapping.put(train_Id, newTrackId);

			System.out.println(initialRequestString + "         new track: "+newTrackId);
			
			//CLEAN UP SLEEPERS
			try {
				HashMap<Integer, Boolean> lastTrack = getTrackMapForNumber(currentTrack_Id);
				for (int i = 1; i <= lastTrack.size(); i++) {
					lastTrack.put(i, false);
				}
			} catch (Exception e) {
				System.out.println("Could not unoccupy all sleepers in last track..");
			}
			//CLEAN UP SLEEPERS END
			
			String regulateSpeedString = getTrackLength(newTrackId) < 25 ? "slowspeed" : "normalspeed";

			String responseString = zoneController_Id+";"+train_Id+";currenttrack;"+newTrackId+";"+regulateSpeedString;
			byte[] bytes = responseString.getBytes();
			MQTTMessage response = new MQTTMessage(bytes, zoneController_Id);
			response.setQoS(0);
			sendToBlock("SENDMESSAGETOTRAIN", response);

			//IF TRAIN PASSED A GREEN SLEEPER, DONT BOTHER TO THINK ABOUT SWITCH LOGIC -> SAVE THIS FOR THE NEW ZONE THE TRAIN ENTERS

			/*String request_id = requestList[5];
			RequestObject request = new RequestObject(train_Id, switch_Id, destination_Id, request_id);
			System.out.println("Sending request to HandleTrainTraffic block..");
			sendToBlock("HANDLETRAINTRAFFIC", request);*/
			return;
		}
		else if (messageContext.equals("speedposition")) {
			int sleeperNumber = 0;
			double currentSpeed = 0.0;
			String collisionCommand = "";
			try {
				sleeperNumber = Integer.parseInt(requestList[4]);
				currentSpeed = Double.parseDouble(requestList[5]);
				Integer trackNumber = train_to_track_mapping.get(train_Id);
				int trackLength = getTrackLength(trackNumber);
				if (trackNumber == null) {
					System.out.println("Tracknumber not found for train");return;
				}
				if (trackLength == 0 ||sleeperNumber == 0 || currentSpeed == 0.0 || sleeperNumber > trackLength) return;
				HashMap<Integer, Boolean> currentTrackMapping = getTrackMapForNumber(trackNumber);
				collisionCommand = collisionDetection(trackNumber, trackLength, sleeperNumber, currentSpeed,currentTrackMapping); //run detection before updating sleepers, so that late messages don't screw things up
				if (sleeperNumber != 0) {
					updateOccupiedSleepers(trackNumber, sleeperNumber, train_Id, currentTrackMapping);
				}
			}
			catch(Exception e) {
				System.out.println("Could not parse the sleeper number or the current speed");
				return;
			}
			
			sendClearanceResponse(collisionCommand, train_Id);
			return;
		}
	}
	
	public void sendClearanceResponse(String collisionCommand, String train_Id) {
		if (collisionCommand.equals("stop")) {
			String commandString = zoneController_Id+";"+train_Id+";stop";
			byte[] bytes = commandString.getBytes();
			MQTTMessage command = new MQTTMessage(bytes, zoneController_Id);
			command.setQoS(0);
			sendToBlock("SENDMESSAGETOTRAIN", command);
			return;
		}			
		else if (collisionCommand.equals("slowspeed")) {
			String commandString = zoneController_Id+";"+train_Id+";slowspeed";
			byte[] bytes = commandString.getBytes();
			MQTTMessage command = new MQTTMessage(bytes, zoneController_Id);
			command.setQoS(0);
			sendToBlock("SENDMESSAGETOTRAIN", command);
			return;
		}
		String commandString = zoneController_Id+";"+train_Id+";ok";
		byte[] bytes = commandString.getBytes();
		MQTTMessage command = new MQTTMessage(bytes, zoneController_Id);
		command.setQoS(0);
		sendToBlock("SENDMESSAGETOTRAIN", command);
		//IF CLEAR SEND BACK OK, IF ANOTHER TRAIN IS CLOSER THAN 10 SLEEPERS BUT FURTHER THAN 5 SLOW DOWN, ELSE STOP TRAIN
	}

	public String collisionDetection(int trackNumber, int trackLength, int sleeperNumber, double speed, HashMap<Integer,Boolean> currentTrackMapping) {
		int stoppingDistance = getStoppingDistance(speed);
		int stopBuffer = 5;
		int stoppingDistanceNeeded = stoppingDistance+stopBuffer;

		int slowdownBuffer = 10;
		int slowdownDistanceNeeded = stoppingDistance+slowdownBuffer;

		if (sleeperNumber+slowdownDistanceNeeded <= trackLength) {
			//System.out.println("CollisionCheck1: sleeperNumber: "+sleeperNumber + ", trackNumber: "+trackNumber+", Speed: "+speed+ ", stoppingDistanceNeeded: "+stoppingDistanceNeeded+", slowDownDistanceNeeded: "+slowdownDistanceNeeded+", start: "+(sleeperNumber+1)+", end: "+(sleeperNumber+slowdownDistanceNeeded));
			for (int i = sleeperNumber+1; i <= sleeperNumber+slowdownDistanceNeeded; i++) { //for loop is safer, BUT slower
				if (currentTrackMapping.get(i)) {
					if (i > sleeperNumber+stoppingDistanceNeeded) {
						System.out.println("SLOWDOWN - TrainSleeper:" + sleeperNumber + ", Sleeper that's occupied: "+i);
						return "slowspeed";
					}
					System.out.println("STOP - TrainSleeper:" + sleeperNumber + ", Sleeper that's occupied: "+i);return "stop";
				}
			}	
		}
		else { //CONSIDER NEXT TRACK AS WELL
			//System.out.println("CollisionCheck2: sleeperNumber: "+sleeperNumber + ", trackNumber: "+trackNumber+", Speed: "+speed+ ", stoppingDistanceNeeded: "+stoppingDistanceNeeded+", slowDownDistanceNeeded: "+slowdownDistanceNeeded+", start: "+(sleeperNumber+1)+", end: "+trackLength);
			int sleepersLeftInCurrentTrack = trackLength - (sleeperNumber);
			for (int i = (sleeperNumber+1); i <= trackLength; i++) {
				if (currentTrackMapping.get(i)) {
					if (i > sleeperNumber+stoppingDistanceNeeded) {System.out.println("SLOWDOWN - TrainSleeper:" + sleeperNumber + ", Sleeper that's occupied: "+i); return "slowspeed";}
					System.out.println("STOP - TrainSleeper:" + sleeperNumber + ", Sleeper that's occupied: "+i); return "stop";
				}
			}
			
			HashMap<Integer,Boolean> nextTrack = getNextTrackForTrain(trackNumber, "");
			boolean checkNextTrack = true;
			int nextTrackId = getNextTrackIdForTrain(trackNumber);
			while(checkNextTrack){
				int sleepersInNewTrack = slowdownDistanceNeeded - sleepersLeftInCurrentTrack;
				if(sleepersInNewTrack <= nextTrack.size()){
					// This is the final track
					//System.out.println("CollisionCheck3: sleeperNumber: "+sleeperNumber + ", trackNumber: "+nextTrackId+", Speed: "+speed+ ", stoppingDistanceNeeded: "+stoppingDistanceNeeded+", slowDownDistanceNeeded: "+slowdownDistanceNeeded+", start: "+1+", end: "+sleepersInNewTrack);
					for (int i = 1; i <= sleepersInNewTrack; i++) {
						if (nextTrack.get(i)) {
							if (i + sleepersLeftInCurrentTrack > stoppingDistanceNeeded) {System.out.println("SLOWDOWN - TrainSleeper:" + sleeperNumber + ", Sleeper that's occupied: "+i);return "slowspeed";}
							System.out.println("STOP - TrainSleeper:" + sleeperNumber + ", Sleeper that's occupied: "+i);return "stop";
						}
					}
					checkNextTrack = false;
				}
				else {
					// Another track needs to be checked
					//System.out.println("CollisionCheck4: sleeperNumber: "+sleeperNumber + ", trackNumber: "+nextTrackId+", Speed: "+speed+ ", stoppingDistanceNeeded: "+stoppingDistanceNeeded+", slowDownDistanceNeeded: "+slowdownDistanceNeeded+", start: "+1+", end: "+nextTrack.size());
					for (int i = 1; i <= nextTrack.size(); i++) {
						if (nextTrack.get(i)) {
							if (i + sleepersLeftInCurrentTrack > stoppingDistanceNeeded) {System.out.println("SLOWDOWN - TrainSleeper:" + sleeperNumber + ", Sleeper that's occupied: "+i);return "slowspeed";}
							System.out.println("STOP - TrainSleeper:" + sleeperNumber + ", Sleeper that's occupied: "+i);return "stop";
						}
					}
					sleepersLeftInCurrentTrack += nextTrack.size();
					nextTrackId = getNextTrackIdForTrain(nextTrackId);
					nextTrack = getNextTrackForTrain(nextTrackId, "");
				}
			}
		}
		return "";
	}

	public HashMap<Integer, Boolean> getNextTrackForTrain(int currentTrackId, String destination) {
		switch (currentTrackId) {
			case 1: if(destination.equals("")) {
				return track_3;
			}
			case 3: if(destination.equals("")) {
				return track_4;
			}
			case 4: if(destination.equals("")) {
				return track_6;
			}
			case 6: if(destination.equals("")) {
				return track_8;
			}
			case 8: if(destination.equals("")) {
				return track_9;
			}
			case 9: if(destination.equals("")) {
				return track_11;
			}
			case 11: if(destination.equals("")) {
				return track_14;
			}			
			case 14: if(destination.equals("")) {
				return track_17;
			}
			case 17: if(destination.equals("")) {
				return track_19;
			}
			case 19: if(destination.equals("")) {
				return track_20;
			}
			case 20: if(destination.equals("")) {
				return track_22;
			}
			case 22: if(destination.equals("")) {
				return track_1;
			}
		}
		return null;
	}
	public int getNextTrackIdForTrain(int trackId) {
		switch (trackId) {
			case 1:  {
				return 3;
			}
			case 3: {
				return 4;
			}
			case 4: {
				return 6;
			}
			case 6: {
				return 8;
			}
			case 8:  {
				return 8;
			}
			case 9: {
				return 11;
			}
			case 11: {
				return 14;
			}			
			case 14: {
				return 17;
			}
			case 17: {
				return 19;
			}
			case 19: {
				return 20;
			}
			case 20: {
				return 22;
			}
			case 22: {
				return 1;
			}
		}
		return -1;
	}

	public int getStoppingDistance(double speed) {
		int stoppingDistance = 0;
		try {
			double kineticFrictionCoefficient = 0.04685; //This variable was calculated through actual testing
			double metricStoppingDistance = (0.5*(speed*speed))/(9.81*kineticFrictionCoefficient);
			stoppingDistance = (int)(Math.ceil(metricStoppingDistance / 0.0325));
		} catch (Exception e) {
			System.out.println("Could not calculate stopping distance");
		}
		return stoppingDistance;
	}

	public void updateOccupiedSleepers(int trackNumber, int sleeperNumber, String train_Id, HashMap<Integer,Boolean> currentTrackMapping) {

		if (sleeperNumber >= train_length) {  //the train is completely within the limits of the track
			int lastSleeper = (sleeperNumber-train_length);
			if (lastSleeper >= 1) {
				currentTrackMapping.put(lastSleeper, false); //MAKE SLEEPER BEHIND TRAIN UNOCCUPIED
			}
			for (int i = sleeperNumber-train_length+1; i < (sleeperNumber-2); i++) { //only update sleepers from tail to almost front of the train as occupied (removed <= sign and subtracted -2)
				currentTrackMapping.put(i, true);
			}
			if (sleeperNumber == train_length) {
				HashMap<Integer, Boolean> lastTrackMapping =  getTrackMapForNumber(train_to_last_track_mapping.get(train_Id));
				lastTrackMapping.put(lastTrackMapping.size(), false); //unoccupy last sleeper of previous track
			}
		}
		else { //update previous track hashmap as well
			if (sleeperNumber >= 4) {
				for (int i = 1; i < (sleeperNumber-2); i++) {
					currentTrackMapping.put(i, true);
				}	
			}		
			int numberOfOldSleepers = train_length - sleeperNumber;
			HashMap<Integer, Boolean> lastTrackMapping =  getTrackMapForNumber(train_to_last_track_mapping.get(train_Id));
			lastTrackMapping.put(lastTrackMapping.size() - numberOfOldSleepers, false);
		}

	}
	
	//This method frees up the three first sleepers behind the train
	public void unoccupySleepers(int trackNumber, int sleeperNumber, String train_Id, HashMap<Integer,Boolean> currentTrackMapping) {
		if (sleeperNumber >= train_length) {  //the train is completely within the limits of the track
			int lastSleeper = (sleeperNumber-train_length);
			if (lastSleeper >= 3) {
				currentTrackMapping.put(lastSleeper, false); //MAKE SLEEPER BEHIND TRAIN UNOCCUPIED
				return;
			}
			if(sleeperNumber == train_length+2) {
				HashMap<Integer, Boolean> lastTrackMapping =  getTrackMapForNumber(train_to_last_track_mapping.get(train_Id));
				lastTrackMapping.put(lastTrackMapping.size(), false); //unoccupy last sleeper of previous track
				currentTrackMapping.put(1, false); //MAKE SLEEPER BEHIND TRAIN UNOCCUPIED
				currentTrackMapping.put(2, false); //MAKE SLEEPER BEHIND TRAIN UNOCCUPIED
				return;
			}
			if(sleeperNumber == train_length+1) {
				HashMap<Integer, Boolean> lastTrackMapping =  getTrackMapForNumber(train_to_last_track_mapping.get(train_Id));
				lastTrackMapping.put(lastTrackMapping.size()-1, false); //unoccupy last sleeper of previous track
				lastTrackMapping.put(lastTrackMapping.size(), false); //unoccupy last sleeper of previous track
				currentTrackMapping.put(1, false); //MAKE SLEEPER BEHIND TRAIN UNOCCUPIED
				return;
			}
			if (sleeperNumber == train_length) {
				HashMap<Integer, Boolean> lastTrackMapping =  getTrackMapForNumber(train_to_last_track_mapping.get(train_Id));
				lastTrackMapping.put(lastTrackMapping.size()-2, false); //unoccupy last sleeper of previous track
				lastTrackMapping.put(lastTrackMapping.size()-1, false); //unoccupy last sleeper of previous track
				lastTrackMapping.put(lastTrackMapping.size(), false); //unoccupy last sleeper of previous track
				return;
			}
		}
		else { //update previous track hashmap		
			int numberOfOldSleepers = train_length - sleeperNumber;
			HashMap<Integer, Boolean> lastTrackMapping =  getTrackMapForNumber(train_to_last_track_mapping.get(train_Id));
			lastTrackMapping.put(lastTrackMapping.size() - numberOfOldSleepers-2, false);
			lastTrackMapping.put(lastTrackMapping.size() - numberOfOldSleepers-1, false);
			lastTrackMapping.put(lastTrackMapping.size() - numberOfOldSleepers, false);
		}
	}

	public static int getNewTrackIdFromColor(int currentTrackId, String color) { //FINISH THIS METHOD WHEN ALL COLORS ARE PRESENT
		if (zoneController_Id.equals("zonecontroller_1")) {
			switch(currentTrackId) {
			case 1: 
				/*if (color.equals("GREEN")) {
					return 2;
				}
				else if (color.equals("BLUE")) {
					return 3;
				}*/
				return 3;
			case 3: 
				/*if (color.equals("YELLOW")) {
					return 4;
				}
				else if (color.equals("BLUE")) {
					return 5;
				}*/
				return 4;
			case 4: return 6;
			case 5: return 6;
			case 22: return 1;
			}
		}
		else if (zoneController_Id.equals("zonecontroller_2")) {
			switch(currentTrackId) {
			case 6: 
				/*if (color.equals("RED")) {
					return 7;
				}
				else if (color.equals("BLUE")) {
					return 8;
				}*/
				return 8;
			case 7: return 9;
			case 8: return 9;
			case 9: 
				/*if (color.equals("GREEN")) {
					return 11;
				}
				else if (color.equals("YELLOW")) {
					return 10;
				}*/
				return 11;
			}
		}
		else if (zoneController_Id.equals("zonecontroller_3")) {
			switch(currentTrackId) {
			case 10: //THIS IS NOT FINISHED BECAUSE NO COLOR
				if (color.equals("XXX")) {
					return 12;
				}
				else if (color.equals("YYY")) {
					return 13;
				}
			case 11: 
				/*if (color.equals("RED")) {
					return 14; //THIS NEEDS TO BE FIXED
				}
				else if (color.equals("YELLOW")) {
					return 14;
				}*/
				return 14;
			case 12: return 16;
			case 13: return 16;
			case 14: return 17;
			case 15: return 17;
			}
		}
		else if (zoneController_Id.equals("zonecontroller_4")) {
			switch(currentTrackId) {
			case 2: return 18;
			case 16: return 18;
			case 17: return 19;
			case 18: return 19;
			case 19: 
				/*if (color.equals("YELLOW")) {
					return 20;
				}
				else if (color.equals("BLUE")) { //FINISH THIS
					return 21;
				}*/
				return 20;
			case 20: return 22;
			case 21: return 22;
			}
		}
		return 0;
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


	public HashMap<Integer, Boolean> getTrackMapForNumber(int trackNumber) {
		switch(trackNumber) {
		case 1: return track_1;
		case 2: return track_2;
		case 3: return track_3;
		case 4: return track_4;
		case 5: return track_5;
		case 6: return track_6;
		case 7: return track_7;
		case 8: return track_8;
		case 9: return track_9;
		case 10: return track_10;
		case 11: return track_11;
		case 12: return track_12;
		case 13: return track_13;
		case 14: return track_14;
		case 15: return track_15;
		case 16: return track_16;
		case 17: return track_17;
		case 18: return track_18;
		case 19: return track_19;
		case 20: return track_20;
		case 21: return track_21;
		case 22: return track_22;
		}
		return null;
	}

	public int getTrackLength(int trackNumber) {
		switch(trackNumber) {
		case 1: return track_1_length;
		case 2: return track_2_length;
		case 3: return track_3_length;
		case 4: return track_4_length;
		case 5: return track_5_length;
		case 6: return track_6_length;
		case 7: return track_7_length;
		case 8: return track_8_length;
		case 9: return track_9_length;
		case 10: return track_10_length;
		case 11: return track_11_length;
		case 12: return track_12_length;
		case 13: return track_13_length;
		case 14: return track_14_length;
		case 15: return track_15_length;
		case 16: return track_16_length;
		case 17: return track_17_length;
		case 18: return track_18_length;
		case 19: return track_19_length;
		case 20: return track_20_length;
		case 21: return track_21_length;
		case 22: return track_22_length;
		}
		return 0;
	}

	public RequestObject updateSwitchPosition(RequestObject request) {
		String switch_Id = request.approachingSwitchId;
		String position = request.goalPositionOfSwitch;
		int index = getIntForSwitchId(switch_Id);
		isInPosition1[index] = position.equals("position1") ? true : false;
		return request;
	}

	public void initOk() {
		System.out.println("-------------------------------");
		System.out.println("-> "+zoneController_Id + " IS READY <-");
		System.out.println("-------------------------------");
		/*for (int i = 75; i <= 85; i++) { //TESTING
			track_6.put(i,true);
		}*/
		System.out.println("Number of sleepers: "+sleeperCounter);
		Button.LEDPattern(2);
	}

	public void currentPositionOfSwitchIsInPos1(RequestObject request) {
		System.out.println("Finding current pos for switch");
		int index = getIntForSwitchId(request.approachingSwitchId);
		request.switchIsInPos1 = isInPosition1[index];
		sendToBlock("RETURNSWITCHPOS",request);
	}

	public void handleTrainTrafficInitOk() {
		System.out.println("-------------------------------");
		System.out.println("->  TRAIN TRAFFIC HANDLER IS READY <-");
		System.out.println("-------------------------------");
	}

	public void motorInitOk() {
		System.out.println("-------------------------------");
		System.out.println("->  MOTOR BLOCK IS READY <-");
		System.out.println("-------------------------------");
	}


}
