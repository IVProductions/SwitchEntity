package switchentity.switchlogic;


import java.util.HashMap;

import lejos.hardware.Button;
import lejos.hardware.Sound;

import com.bitreactive.library.mqtt.MQTTConfigParam;
import com.bitreactive.library.mqtt.MQTTMessage;
import com.bitreactive.library.mqtt.robustmqtt.RobustMQTT.Parameters;

import no.ntnu.item.arctis.runtime.Block;

public class SwitchLogic extends Block {

	//Static variables
	public static java.lang.String zoneController_Id = "zonecontroller_4";
	public static final String switch1_id = "switch_1";
	public static final String switch2_id = "switch_2";
	public static final String switch3_id = "switch_3";
	public static final String switch4_id = "switch_4";

	public static String switch_1_status = "";
	public static String switch_2_status = "";

	private int train_length = 10;
	private Boolean[] isInPosition1 = new Boolean[4];
	HashMap<String, Integer> train_to_track_mapping = new HashMap<String, Integer>();
	HashMap<String, Integer> train_to_last_track_mapping = new HashMap<String, Integer>();

	public static class SwitchAndPosition {
		public String switch_Id;
		public String position;
		public boolean success = false;

		public SwitchAndPosition(String switch_Id, String position) {
			this.switch_Id = switch_Id;
			this.position = position;
		}	
	}

	public static class SwitchStatus {
		public String switch_Id;
		public String status;

		public SwitchStatus(String switch_Id, String status) {
			this.switch_Id = switch_Id;
			this.status = status;
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

		public RequestObject(String trainId, String approachingSwitchId, String destinationId, String requestId, String goalPositionOfSwitch) {
			this.trainId = trainId;
			this.approachingSwitchId = approachingSwitchId;
			this.destinationId = destinationId;
			this.requestId = requestId;
			this.goalPositionOfSwitch = goalPositionOfSwitch;
		}
		public RequestObject(String switchId, String goalPositionOfSwitch) {
			this.approachingSwitchId = switchId;
			this.goalPositionOfSwitch = goalPositionOfSwitch;
		}
	}

	public static class TrackObject {
		public int trackId;
		public int trackLength;
		public HashMap<Integer, Boolean> trackMap;
		public String switchId;

		public TrackObject(int trackId, int trackLength, HashMap<Integer, Boolean> trackMap, String switchId) {
			this.trackId = trackId;
			this.trackLength = trackLength;
			this.trackMap = trackMap;
			this.switchId = switchId;
		}
	}

	public class NextTrackAndSwitchPosition {

		TrackObject nextTrack;
		String goalSwitchPosition;

		public NextTrackAndSwitchPosition(TrackObject nextTrack, String goalSwitchPosition) {
			this.nextTrack = nextTrack;
			this.goalSwitchPosition = goalSwitchPosition;
		}
	}

	int track_1_length = 47;
	int track_2_length = 41;
	int track_3_length = 21;
	int track_4_length = 16;
	int track_5_length = 18;
	int track_6_length = 90;
	int track_7_length = 18;
	int track_8_length = 16;
	int track_9_length = 11;
	int track_10_length = 16;
	int track_11_length = 40;
	int track_12_length = 16;
	int track_13_length = 18;
	int track_14_length = 20;
	int track_15_length = 22;
	int track_16_length = 17;
	int track_17_length = 73;
	int track_18_length = 27;
	int track_19_length = 27;
	int track_20_length = 16;
	int track_21_length = 18;
	int track_22_length = 62;
	int track_23_length = 32;


	//TRACK LAYOUT
	int sleeperCounter = 0;
	HashMap<Integer, Boolean> trackMap_1 = initTrackOverview(track_1_length);
	HashMap<Integer, Boolean> trackMap_2 = initTrackOverview(track_2_length);
	HashMap<Integer, Boolean> trackMap_3 = initTrackOverview(track_3_length);
	HashMap<Integer, Boolean> trackMap_4 = initTrackOverview(track_4_length);
	HashMap<Integer, Boolean> trackMap_5 = initTrackOverview(track_5_length);
	HashMap<Integer, Boolean> trackMap_6 = initTrackOverview(track_6_length);
	HashMap<Integer, Boolean> trackMap_7 = initTrackOverview(track_7_length);
	HashMap<Integer, Boolean> trackMap_8 = initTrackOverview(track_8_length);
	HashMap<Integer, Boolean> trackMap_9 = initTrackOverview(track_9_length);
	HashMap<Integer, Boolean> trackMap_10 = initTrackOverview(track_10_length);
	HashMap<Integer, Boolean> trackMap_11 = initTrackOverview(track_11_length);
	HashMap<Integer, Boolean> trackMap_12 = initTrackOverview(track_12_length);
	HashMap<Integer, Boolean> trackMap_13 = initTrackOverview(track_13_length);
	HashMap<Integer, Boolean> trackMap_14 = initTrackOverview(track_14_length);
	HashMap<Integer, Boolean> trackMap_15 = initTrackOverview(track_15_length);
	HashMap<Integer, Boolean> trackMap_16 = initTrackOverview(track_16_length);
	HashMap<Integer, Boolean> trackMap_17 = initTrackOverview(track_17_length);
	HashMap<Integer, Boolean> trackMap_18 = initTrackOverview(track_18_length);
	HashMap<Integer, Boolean> trackMap_19 = initTrackOverview(track_19_length);
	HashMap<Integer, Boolean> trackMap_20 = initTrackOverview(track_20_length);
	HashMap<Integer, Boolean> trackMap_21 = initTrackOverview(track_21_length);
	HashMap<Integer, Boolean> trackMap_22 = initTrackOverview(track_22_length);
	HashMap<Integer, Boolean> trackMap_23 = initTrackOverview(track_23_length);

	public HashMap<Integer, Boolean> initTrackOverview(int length) {
		HashMap<Integer, Boolean> track = new HashMap<Integer, Boolean>();
		for (int i = 1; i <= length; i++) {
			track.put(i, false);
		}
		sleeperCounter+=length;
		return track;
	}

	TrackObject track_1 = new TrackObject(1, track_1_length, trackMap_1, switch1_id);
	TrackObject track_2 = new TrackObject(2, track_2_length, trackMap_2, "");
	TrackObject track_3 = new TrackObject(3, track_3_length, trackMap_3, switch2_id);
	TrackObject track_4 = new TrackObject(4, track_4_length, trackMap_4, "");
	TrackObject track_5 = new TrackObject(5, track_5_length, trackMap_5, "");
	TrackObject track_6 = new TrackObject(6, track_6_length, trackMap_6, switch1_id);
	TrackObject track_7 = new TrackObject(7, track_7_length, trackMap_7, "");
	TrackObject track_8 = new TrackObject(8, track_8_length, trackMap_8, "");
	TrackObject track_9 = new TrackObject(9, track_9_length, trackMap_9, switch2_id);
	TrackObject track_10 = new TrackObject(10, track_10_length, trackMap_10, "");
	TrackObject track_11 = new TrackObject(11, track_11_length, trackMap_11, switch1_id);
	TrackObject track_12 = new TrackObject(12, track_12_length, trackMap_12, "");
	TrackObject track_13 = new TrackObject(13, track_13_length, trackMap_13, "");
	TrackObject track_14 = new TrackObject(14, track_14_length, trackMap_14, "");
	TrackObject track_15 = new TrackObject(15, track_15_length, trackMap_15, "");
	TrackObject track_16 = new TrackObject(16, track_16_length, trackMap_16, "");
	TrackObject track_17 = new TrackObject(17, track_17_length, trackMap_17, "");
	TrackObject track_18 = new TrackObject(18, track_18_length, trackMap_18, "");
	TrackObject track_19 = new TrackObject(19, track_19_length, trackMap_19, switch1_id);
	TrackObject track_20 = new TrackObject(20, track_20_length, trackMap_20, "");
	TrackObject track_21 = new TrackObject(21, track_21_length, trackMap_21, "");
	TrackObject track_22 = new TrackObject(22, track_22_length, trackMap_22, "");
	TrackObject track_23 = new TrackObject(23, track_23_length, trackMap_23, switch2_id);
	//TRACK LAYOUT END

	public void init() {
		System.out.println("MQTT ready...");
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
		if (!initialRequestString.contains(";")) {System.out.println("------"+initialRequestString+"------");return;}  //message only consists of one word -> ignore
		String[] requestList = initialRequestString.split(";");
		String train_Id = requestList[0];
		String sentToZoneController = requestList[1];
		String sentToOldZoneController = requestList[2];

		if (train_Id.equals(zoneController_Id)) return; //received message from itself -> ignore
		if (train_Id != null && sentToZoneController.equals(zoneController_Id) && sentToOldZoneController.equals("unsubscribe")) {
			//LOGIC FOR REMOVING TRAIN FROM ZONE AND CLEARING UP TRACKS
			//System.out.println(train_Id+" has unsubscribed!");
			int trackIdThatTrainIsLeaving = train_to_track_mapping.get(train_Id);
			TrackObject trackThatTrainIsLeaving = getTrackForNumber(trackIdThatTrainIsLeaving);
			if (trackThatTrainIsLeaving != null && trackThatTrainIsLeaving.trackMap != null) {
				//CLEAN UP SLEEPERS
				try {
					for (int i = 1; i <= trackThatTrainIsLeaving.trackLength; i++) {
						trackThatTrainIsLeaving.trackMap.put(i, false);
					}
				} catch (Exception e) {
					System.out.println("Could not unoccupy all sleepers in last track..");
				}
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
				TrackObject track = getTrackForNumber(trackId);
				for (int i = 1; i <= track.trackLength; i++) {
					if (track.trackMap.get(i)) {
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
			//System.out.println("Received requeststart: "+initialRequestString);
			int sleeperNumber = 0;
			double currentSpeed = 0.0;
			String collisionCommand = "";
			String destination = "";
			try {
				sleeperNumber = Integer.parseInt(requestList[4]);
				destination = requestList[5];
				currentSpeed = 0.4; //Set a bogus speed so that a small number of sleepers in front of train is checked
				Integer trackNumber = train_to_track_mapping.get(train_Id);
				if (trackNumber == null) {
					//System.out.println("Tracknumber not found for train");
					return;
				}
				TrackObject currentTrack = getTrackForNumber(trackNumber);
				if (sleeperNumber > currentTrack.trackLength) sleeperNumber = currentTrack.trackLength;
				if (currentTrack.trackLength == 0 ||sleeperNumber == 0 || currentSpeed == 0.0 || sleeperNumber > currentTrack.trackLength) {System.out.println("Something went wrong here..s");return;}
				collisionCommand = collisionDetection(sleeperNumber, currentSpeed,currentTrack, destination, train_Id); //run detection before updating sleepers, so that late messages don't screw things up
				//System.out.println("TrainId: "+train_Id+", trackNumber: "+trackNumber+", trackLength: "+trackLength+", Will send response  back: " + collisionCommand);
				if (collisionCommand.equals("")) unoccupySleepers(sleeperNumber, train_Id, currentTrack); //the train can start up again, make three sleepers behind the train free
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
			String destination = requestList[5];

			train_to_track_mapping.put(train_Id, Integer.parseInt(currentTrack_Id));
			//System.out.println("New Zone:      "+initialRequestString);

			//SWITCH LOGIC
			int currentTrackNumber = 0;
			try {
				currentTrackNumber = Integer.parseInt(currentTrack_Id);
			} catch (Exception e) {
				System.out.println("Could not parse trackId: "+currentTrack_Id);
				return;
			}
			TrackObject currentTrack = getTrackForNumber(currentTrackNumber);    //the track that the train just entered
			if (currentTrack == null) return;
			NextTrackAndSwitchPosition nextTrack = getNextTrackForTrain(currentTrackNumber, destination); //the track that train is entering next, based on destination
			if (!currentTrack.switchId.equals("") || !nextTrack.goalSwitchPosition.equals("")) {  //only run logic if track has switch
				RequestObject request = new RequestObject(train_Id, currentTrack.switchId, destination, "", nextTrack.goalSwitchPosition);
				//System.out.println("Sending request to HandleTrainTraffic block - from new zone logic");
				sendToBlock("HANDLETRAINTRAFFIC", request);
			}
			//SWITCH LOGIC END
			return;
		}
		else if (messageContext.equals("colorsleeper")) { //TRAIN PASSED COLOR SLEEPER INSIDE ZONE
			String sleeperColor = requestList[4];
			String destination = requestList[5];
			String request_Id = requestList[6];
			//if (sleeperColor == null || destination_Id == null) return;

			//UPDATE STATUS OF THE TRACK THAT THE TRAIN IS ON => FIND THE NEW TRACK OF THE TRAIN BASED ON THE SLEEPERCOLOR AND THE LAST KNOWN TRACK OF THE TRAIN
			int currentTrack_Id = train_to_track_mapping.get(train_Id);
			if (currentTrack_Id == 0) return;
			int newTrackId = getNewTrackIdFromColor(currentTrack_Id, sleeperColor); //find the track id the train just entered
			if (newTrackId == 0) {System.out.println("Ghost track sleeper read -> current track: "+currentTrack_Id+", color: "+sleeperColor);return;}   //GHOST reading of color => ignore
			train_to_last_track_mapping.put(train_Id,currentTrack_Id);  //keep an overview of what track the train came from
			train_to_track_mapping.put(train_Id, newTrackId);           //keep an overview of which trains occupy what tracks

			//CLEAN UP SLEEPERS
			try {
				TrackObject lastTrack = getTrackForNumber(currentTrack_Id);
				for (int i = 1; i <= lastTrack.trackLength; i++) {
					lastTrack.trackMap.put(i, false);
				}
			} catch (Exception e) {
				System.out.println("Could not unoccupy all sleepers in last track..");
			}
			//CLEAN UP SLEEPERS END

			String regulateSpeedString = (getTrackLength(newTrackId) < 25 && !sleeperColor.equals("BLUE")) ? "slowspeed" : "normalspeed";  //tells the train to speed up/down given length of track

			//NEW RESPONSE MESSAGE TO TRAIN
			String responseString = zoneController_Id+";"+train_Id+";currenttrack;"+newTrackId+";"+regulateSpeedString;   
			byte[] bytes = responseString.getBytes();
			MQTTMessage response = new MQTTMessage(bytes, zoneController_Id);
			response.setQoS(0);
			sendToBlock("SENDMESSAGETOTRAIN", response);
			//NEW RESPONSE MESSAGE TO TRAIN END

			//SWITCH LOGIC
			if (sleeperColor.equals("GREEN")) return; //do not send switch request to "old" zone controller when train is about to enter new zone
			TrackObject newTrack = getTrackForNumber(newTrackId);    //the track that the train just entered
			NextTrackAndSwitchPosition nextTrack = getNextTrackForTrain(newTrackId, destination); //the track that train is entering next, based on destination
			if (!newTrack.switchId.equals("") || !nextTrack.goalSwitchPosition.equals("")) {  //only run logic if track has switch
				RequestObject request = new RequestObject(train_Id, newTrack.switchId, destination, request_Id, nextTrack.goalSwitchPosition);
				//System.out.println("Sending request to HandleTrainTraffic block..");
				sendToBlock("HANDLETRAINTRAFFIC", request);
			}
			//else send message to let train know it's ok to continue
			return;
		}
		else if (messageContext.equals("speedposition")) {
			int sleeperNumber = 0;
			double currentSpeed = 0.0;
			String destination = "";
			String collisionCommand = "";
			try {
				sleeperNumber = Integer.parseInt(requestList[4]);
				currentSpeed = Double.parseDouble(requestList[5]);
				destination = requestList[6];
				Integer trackNumber = train_to_track_mapping.get(train_Id);
				TrackObject currentTrack = getTrackForNumber(trackNumber);
				if (trackNumber == null) {
					System.out.println("Tracknumber not found for train");return;
				}
				if (currentTrack.trackLength == 0 ||sleeperNumber == 0 || currentSpeed == 0.0 || sleeperNumber > currentTrack.trackLength) return;
				collisionCommand = collisionDetection(sleeperNumber, currentSpeed,currentTrack, destination, train_Id); //run detection before updating sleepers, so that late messages don't screw things up
				if (sleeperNumber != 0) {
					updateOccupiedSleepers(sleeperNumber, train_Id, currentTrack);
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
		else if (collisionCommand.equals("waitforswitch")) {
			String commandString = zoneController_Id+";"+train_Id+";waitforswitch";
			byte[] bytes = commandString.getBytes();
			MQTTMessage command = new MQTTMessage(bytes, zoneController_Id);
			command.setQoS(0);
			sendToBlock("SENDMESSAGETOTRAIN", command);
			return;
		}
		else if (collisionCommand.equals("switchclear")) {
			String commandString = zoneController_Id+";"+train_Id+";switchclear";
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

	public String collisionDetection(int sleeperNumber, double speed, TrackObject currentTrack, String destination, String train_Id) {
		int stoppingDistance = getStoppingDistance(speed);
		int stopBuffer = 10;
		int stoppingDistanceNeeded = stoppingDistance+stopBuffer;

		int slowdownBuffer = 10;
		int slowdownDistanceNeeded = stoppingDistance+slowdownBuffer;

		if (sleeperNumber+slowdownDistanceNeeded <= currentTrack.trackLength) {
			//System.out.println("CollisionCheck1: sleeperNumber: "+sleeperNumber + ", trackNumber: "+trackNumber+", Speed: "+speed+ ", stoppingDistanceNeeded: "+stoppingDistanceNeeded+", slowDownDistanceNeeded: "+slowdownDistanceNeeded+", start: "+(sleeperNumber+1)+", end: "+(sleeperNumber+slowdownDistanceNeeded));
			for (int i = sleeperNumber+1; i <= sleeperNumber+slowdownDistanceNeeded; i++) { //for loop is safer, BUT slower
				if (currentTrack.trackMap.get(i)) {
					if (i > sleeperNumber+stoppingDistanceNeeded) {
						//System.out.println("SLOWDOWN - TrainSleeper:" + sleeperNumber + ", Sleeper that's occupied: "+i);
						return "slowspeed";
					}
					//System.out.println("STOP - TrainSleeper:" + sleeperNumber + ", Sleeper that's occupied: "+i);
					return "stop";
				}
			}	
		}
		else { //CONSIDER NEXT TRACK AS WELL
			//System.out.println("CollisionCheck2: sleeperNumber: "+sleeperNumber + ", trackNumber: "+trackNumber+", Speed: "+speed+ ", stoppingDistanceNeeded: "+stoppingDistanceNeeded+", slowDownDistanceNeeded: "+slowdownDistanceNeeded+", start: "+(sleeperNumber+1)+", end: "+trackLength);
			if (!currentTrack.switchId.equals("")){  //this track has a switch
				String switchStatus = getSwitchStatusForSwitchId(currentTrack.switchId); 	//check if it's clear for this train to proceed through switch
				//System.out.println("switchStatus for track_"+currentTrack.trackId+" = "+switchStatus);
				if (!switchStatus.equals("")) { //switch is occupied
					//System.out.println(currentTrack.switchId+" is occupied...");
					if (!switchStatus.equals(train_Id)) { //the switch is occupied by another train than this train!
						//System.out.println("by another train. Send waitforswitchmessage!!");
						return "waitforswitch";		//TELL TRAIN TO STOP AND WAIT FOR CLEARANCE
					}
					else {
						//System.out.println("by this train: "+train_Id);
					}
				}
			}
			int sleepersLeftInCurrentTrack = currentTrack.trackLength - (sleeperNumber);
			for (int i = (sleeperNumber+1); i <= currentTrack.trackLength; i++) {
				if (currentTrack.trackMap.get(i)) {
					if (i > sleeperNumber+stoppingDistanceNeeded) {
						//System.out.println("SLOWDOWN - TrainSleeper:" + sleeperNumber + ", Sleeper that's occupied: "+i); 
						return "slowspeed";
					}
					//System.out.println("STOP - TrainSleeper:" + sleeperNumber + ", Sleeper that's occupied: "+i); 
					return "stop";
				}
			}

			NextTrackAndSwitchPosition nextTrackAndSwitchPos = getNextTrackForTrain(currentTrack.trackId, destination);
			if (nextTrackAndSwitchPos == null) return "stop";
			boolean checkNextTrack = true;
			while(checkNextTrack){
				int sleepersInNewTrack = slowdownDistanceNeeded - sleepersLeftInCurrentTrack;
				if(sleepersInNewTrack <= nextTrackAndSwitchPos.nextTrack.trackLength){
					// This is the final track
					//System.out.println("CollisionCheck3: sleeperNumber: "+sleeperNumber + ", trackNumber: "+nextTrackId+", Speed: "+speed+ ", stoppingDistanceNeeded: "+stoppingDistanceNeeded+", slowDownDistanceNeeded: "+slowdownDistanceNeeded+", start: "+1+", end: "+sleepersInNewTrack);
					for (int i = 1; i <= sleepersInNewTrack; i++) {
						if (nextTrackAndSwitchPos.nextTrack.trackMap.get(i)) {
							if (i + sleepersLeftInCurrentTrack > stoppingDistanceNeeded) {
								//System.out.println("SLOWDOWN - TrainSleeper:" + sleeperNumber + ", Sleeper that's occupied: "+i);
								return "slowspeed";
							}
							//System.out.println("STOP - TrainSleeper:" + sleeperNumber + ", Sleeper that's occupied: "+i);
							return "stop";
						}
					}
					checkNextTrack = false;
				}
				else {
					// Another track needs to be checked
					//System.out.println("CollisionCheck4: sleeperNumber: "+sleeperNumber + ", trackNumber: "+nextTrackId+", Speed: "+speed+ ", stoppingDistanceNeeded: "+stoppingDistanceNeeded+", slowDownDistanceNeeded: "+slowdownDistanceNeeded+", start: "+1+", end: "+nextTrack.size());
					for (int i = 1; i <= nextTrackAndSwitchPos.nextTrack.trackLength; i++) {
						if (nextTrackAndSwitchPos.nextTrack.trackMap.get(i)) {
							if (i + sleepersLeftInCurrentTrack > stoppingDistanceNeeded) {
								//System.out.println("SLOWDOWN - TrainSleeper:" + sleeperNumber + ", Sleeper that's occupied: "+i);
								return "slowspeed";
							}
							//System.out.println("STOP - TrainSleeper:" + sleeperNumber + ", Sleeper that's occupied: "+i);
							return "stop";
						}
					}
					sleepersLeftInCurrentTrack += nextTrackAndSwitchPos.nextTrack.trackLength;
					nextTrackAndSwitchPos = getNextTrackForTrain(nextTrackAndSwitchPos.nextTrack.trackId, destination);
					if (nextTrackAndSwitchPos == null) return "stop";
				}
			}
		}
		return "";
	}

	public String getSwitchStatusForSwitchId(String switchId) {
		switch(switchId) {
		case "switch_1": return switch_1_status;
		case "switch_2": return switch_2_status;
		default: return "";
		}
	}

	public void setSwitchStatus(String switchId, String status) {
		switch(switchId) {
		case "switch_1": switch_1_status = status;
		case "switch_2": switch_2_status = status;
		}
	}

	//returns the next track that the train will enter
	public NextTrackAndSwitchPosition getNextTrackForTrain(int currentTrackId, String destination) {
		if (destination.equals("")) {System.out.println("Destination cannot be emtpy string!"); return null;}
		switch (currentTrackId) {
		case 1: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_3, "position1");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_3,"position1");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_3, "position1");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_3, "position1");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_2, "position2");
		case 2: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_18, "");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_18, "");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_18, "");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_18, "");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_18, "");
		case 3: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_5, "position2");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_4, "position1");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_4, "position1");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_4, "position1");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_4, "position1");
		case 4: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_6, "");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_6, "");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_6, "");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_6, "");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_6, "");
		case 5: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_6, "");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_6, "");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_6, "");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_6, "");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_6, "");
		case 6: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_8, "position1");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_7, "position2");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_8, "position1");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_8, "position1");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_8, "position1");
		case 7: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_9, "");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_9, "");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_9, "");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_9, "");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_9, "");
		case 8:
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_9, "");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_9, "");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_9, "");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_9, "");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_9, "");
		case 9:
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_11, "position1");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_11, "position1");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_11, "position1");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_10, "position2");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_11, "position1");
		case 10: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_23, "");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_23, "");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_23, "");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_23, "");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_23, "");
		case 11:
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_14, "position1");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_14, "position1");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_15, "position2");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_14, "position1");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_14, "position1");	
		case 12: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_16, "");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_16, "");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_16, "");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_16, "");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_16, "");
		case 13: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_16, "");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_16, "");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_16, "");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_16, "");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_16, "");
		case 14: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_17, "");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_17, "");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_17, "");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_17, "");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_17, "");
		case 15: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_17, "");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_17, "");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_17, "");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_17, "");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_17, "");
		case 16: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_18, "");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_18, "");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_18, "");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_18, "");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_18, "");
		case 17: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_19, "");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_19, "");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_19, "");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_19, "");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_19, "");
		case 18:
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_19, "");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_19, "");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_19, "");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_19, "");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_19, "");
		case 19:
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_20, "position1");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_20, "position1");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_20, "position1");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_20, "position1");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_21, "position2");
		case 20:
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_22, "");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_22, "");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_22, "");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_22, "");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_22, "");
		case 21: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_22, "");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_22, "");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_22, "");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_22, "");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_22, "");
		case 22: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_1, "");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_1, "");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_1, "");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_1, "");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_1, "");
		case 23: 
			if(destination.equals("station_1")) return new NextTrackAndSwitchPosition(track_12, "position1");
			else if (destination.equals("station_2")) return new NextTrackAndSwitchPosition(track_12, "position1");
			else if (destination.equals("station_3")) return new NextTrackAndSwitchPosition(track_12, "position1");
			else if (destination.equals("station_4")) return new NextTrackAndSwitchPosition(track_13, "position2");
			else if (destination.equals("station_5")) return new NextTrackAndSwitchPosition(track_12, "position1");
		}
		System.out.println("Invalid station id: "+destination);
		return null;
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

	public void updateOccupiedSleepers(int sleeperNumber, String train_Id, TrackObject currentTrack) {

		if (sleeperNumber >= train_length) {  //the train is completely within the limits of the track
			int lastSleeper = (sleeperNumber-train_length);
			if (lastSleeper >= 1) {
				currentTrack.trackMap.put(lastSleeper, false); //MAKE SLEEPER BEHIND TRAIN UNOCCUPIED
			}
			for (int i = sleeperNumber-train_length+1; i < (sleeperNumber-2); i++) { //only update sleepers from tail to almost front of the train as occupied (removed <= sign and subtracted -2)
				currentTrack.trackMap.put(i, true);
			}
			if (sleeperNumber == train_length) {
				TrackObject lastTrack =  getTrackForNumber(train_to_last_track_mapping.get(train_Id));
				lastTrack.trackMap.put(lastTrack.trackLength, false); //unoccupy last sleeper of previous track
				if (!lastTrack.switchId.equals("")) { //if train just passed a switch, send message to controller that switch is unoccupied
					//System.out.println(train_Id+" passed switch. Handling next request in queue..");
					sendToBlock("HANDLENEXTREQ", lastTrack.switchId);//free the switch and handle next request in Traffic Handler block
				}
			}
		}
		else { //update previous track hashmap as well
			if (sleeperNumber >= 4) {
				for (int i = 1; i < (sleeperNumber-2); i++) {
					currentTrack.trackMap.put(i, true);
				}	
			}		
			int numberOfOldSleepers = train_length - sleeperNumber;
			TrackObject lastTrackMapping =  getTrackForNumber(train_to_last_track_mapping.get(train_Id));
			lastTrackMapping.trackMap.put(lastTrackMapping.trackLength - numberOfOldSleepers, false);
		}

	}

	//This method frees up the three first sleepers behind the train
	public void unoccupySleepers(int sleeperNumber, String train_Id, TrackObject currentTrack) {
		if (sleeperNumber >= train_length) {  //the train is completely within the limits of the track
			int lastSleeper = (sleeperNumber-train_length);
			if (lastSleeper >= 3) {
				currentTrack.trackMap.put(lastSleeper, false); //MAKE SLEEPER BEHIND TRAIN UNOCCUPIED
				return;
			}
			if(sleeperNumber == train_length+2) {
				TrackObject lastTrack =  getTrackForNumber(train_to_last_track_mapping.get(train_Id));
				lastTrack.trackMap.put(lastTrack.trackLength, false); //unoccupy last sleeper of previous track
				currentTrack.trackMap.put(1, false); //MAKE SLEEPER BEHIND TRAIN UNOCCUPIED
				currentTrack.trackMap.put(2, false); //MAKE SLEEPER BEHIND TRAIN UNOCCUPIED
				return;
			}
			if(sleeperNumber == train_length+1) {
				TrackObject lastTrack =  getTrackForNumber(train_to_last_track_mapping.get(train_Id));
				lastTrack.trackMap.put(lastTrack.trackLength-1, false); //unoccupy last sleeper of previous track
				lastTrack.trackMap.put(lastTrack.trackLength, false); //unoccupy last sleeper of previous track
				currentTrack.trackMap.put(1, false); //MAKE SLEEPER BEHIND TRAIN UNOCCUPIED
				return;
			}
			if (sleeperNumber == train_length) {
				TrackObject lastTrack =  getTrackForNumber(train_to_last_track_mapping.get(train_Id));
				lastTrack.trackMap.put(lastTrack.trackLength-2, false); //unoccupy last sleeper of previous track
				lastTrack.trackMap.put(lastTrack.trackLength-1, false); //unoccupy last sleeper of previous track
				lastTrack.trackMap.put(lastTrack.trackLength, false); //unoccupy last sleeper of previous track
				return;
			}
		}
		else { //update previous track hashmap		
			int numberOfOldSleepers = train_length - sleeperNumber;
			TrackObject lastTrack =  getTrackForNumber(train_to_last_track_mapping.get(train_Id));
			lastTrack.trackMap.put(lastTrack.trackLength - numberOfOldSleepers-2, false);
			lastTrack.trackMap.put(lastTrack.trackLength - numberOfOldSleepers-1, false);
			lastTrack.trackMap.put(lastTrack.trackLength - numberOfOldSleepers, false);
		}
	}

	public static int getNewTrackIdFromColor(int currentTrackId, String color) { //THIS METHOD IS COMPLETE
		switch(currentTrackId) {
		case 1: 
			if (color.equals("GREEN")) {
				return 2;
			}
			else if (color.equals("YELLOW")) { //in case yellow is conceived as red
				return 3;
			}
			else return 0;
		case 2: 
			if (color.equals("YELLOW")) {
				return 18;
			}
			else return 0;
		case 3: 
			if (color.equals("YELLOW")) {
				return 4;
			}
			else if (color.equals("BLUE")) {
				return 5;
			}
			else return 0;
		case 4: 
			if (color.equals("GREEN")) {
				return 6;
			}
			else return 0;
		case 5: 
			if (color.equals("GREEN")) {
				return 6;
			}
			else return 0;
		case 6: 
			if (color.equals("YELLOW")) {
				return 8;
			}
			else if (color.equals("BLUE")) {
				return 7;
			}
			else return 0;
		case 7: 
			if (color.equals("YELLOW")) {
				return 9;
			}
			else return 0;
		case 8: 
			if (color.equals("YELLOW")) {
				return 9;
			}
			else return 0;
		case 9: 
			if (color.equals("GREEN")) {
				return 11;
			}
			else if (color.equals("YELLOW")) {
				return 10;
			}
			else return 0;
		case 10: 
			if (color.equals("GREEN")) {
				return 23;
			}
			else return 0;
		case 11: 
			if (color.equals("YELLOW")) {
				return 14;
			}
			else if (color.equals("BLUE")) {
				return 15;
			}
			else return 0;
		case 12: 
			if (color.equals("GREEN")) {
				return 16;
			}
			else return 0;
		case 13: 
			if (color.equals("GREEN")) {
				return 16;
			}
			else return 0;
		case 14: 
			if (color.equals("GREEN")) {
				return 17;
			}
			else return 0;
		case 15: 
			if (color.equals("GREEN")) {
				return 17;
			}
			else return 0;
		case 16: 
			if (color.equals("YELLOW")) {
				return 18;
			}
			else return 0;
		case 17: 
			if (color.equals("YELLOW")) {
				return 19;
			}
			else return 0;
		case 18: 
			if (color.equals("YELLOW") || color.equals("RED")) {
				return 19;
			}
			else return 0;
		case 19: 
			if (color.equals("YELLOW")) {
				return 20;
			}
			else if (color.equals("BLUE")) {
				return 21;
			}
			else return 0;
		case 20: 
			if (color.equals("GREEN")) {
				return 22;
			}
			else return 0;
		case 21: 
			if (color.equals("GREEN")) {
				return 22;
			}
			else return 0;
		case 22: 
			if (color.equals("RED") || color.equals("YELLOW")) {
				return 1;
			}
			else return 0;
		case 23: 
			if (color.equals("YELLOW")) {
				return 12;
			}
			else if (color.equals("BLUE")) {
				return 13;
			}
			else return 0;
		default: return 0;
		}
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


	public TrackObject getTrackForNumber(int trackNumber) {
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
		case 23: return track_23;
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
		case 23: return track_23_length;
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

	//finds out if switch x is in position 1
	public void currentPositionOfSwitchIsInPos1(RequestObject request) {
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


	public void updateSwitchOccupiedStatus(SwitchStatus switchStatus) {
		setSwitchStatus(switchStatus.switch_Id, switchStatus.status);
	}


}
