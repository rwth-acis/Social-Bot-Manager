package i5.las2peer.services.socialBotManagerService.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import javax.websocket.DeploymentException;

import i5.las2peer.services.socialBotManagerService.chat.ChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.chat.RocketChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.SlackChatMediator;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.RasaNlu;
import i5.las2peer.services.socialBotManagerService.parser.ParseBotException;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class Messenger {
	private String name;

	private ChatMediator chatMediator;
	//private RasaNlu rasa;
    //private RasaNlu rasaAssessment;

	// Key: intent keyword
	private HashMap<String, IncomingMessage> knownIntents;

	// Used for keeping conversation state per channel
	private HashMap<String, IncomingMessage> stateMap;
    // Used for keeping context between assessment and non-assessment states
    // Key is the channelId
    private HashMap<String, String> context;
    // Used to know to which Function the received intents/messages are to be sent
    private HashMap<String, String> triggeredFunction;

	private Random random;
    

	public Messenger(String id, String chatService, String token, SQLDatabase database)
			throws IOException, DeploymentException, ParseBotException {

//		this.rasa = new RasaNlu(rasaUrl);
//        this.rasaAssessment = new RasaNlu(rasaAssessmentUrl);
		if (chatService.contentEquals("Slack")) {
			this.chatMediator = new SlackChatMediator(token);
		} else if (chatService.contentEquals("Rocket.Chat")) {
			this.chatMediator = new RocketChatMediator(token, database, new RasaNlu("rasaUrl"));
		} else { // TODO: Implement more backends
			throw new ParseBotException("Unimplemented chat service: " + chatService);
		}
		this.name = id;
		this.knownIntents = new HashMap<String, IncomingMessage>();
		this.stateMap = new HashMap<String, IncomingMessage>();
		this.random = new Random();
        // Initialize the assessment setup
        this.context = new HashMap<String, String>();
        this.triggeredFunction = new HashMap<String, String>();
	}

	public String getName() {
		return name;
	}

	public void addMessage(IncomingMessage msg) {
		this.knownIntents.put(msg.getIntentKeyword(), msg);
	}

	public ChatMediator getChatMediator() {
		return this.chatMediator;
	}
    // set the context of the specified channel
    public void setContext(String channel, String contextName){
        context.put(channel, contextName);
        
    }
    
    public void setContextToBasic(String channel){
        context.put(channel, "Basic");
        
        IncomingMessage state = this.stateMap.get(channel);
        if(state != null) {
	        if(state.getFollowingMessages() == null) {
	        	System.out.println("Conversation flow ended now");
	        } else {
	        	state = state.getFollowingMessages().get("");
	        	stateMap.put(channel, state);
	        	this.chatMediator.sendMessageToChannel(channel, state.getResponse(random).getResponse());
	        }
        }
    }
    public String getContext(String channel){
        return this.context.get(channel);
    }

    
	// Handles simple responses ("Chat Response") directly, logs all messages and
	// extracted intents into `messageInfos` for further processing later on.
	// TODO: This would be much nicer if we could get a las2peer context here, but this
	// is usually called from the routine thread. Maybe a context can be shared across
	// threads somehow?
	public void handleMessages(ArrayList<MessageInfo> messageInfos, Bot bot) {
		Vector<ChatMessage> newMessages = this.chatMediator.getMessages();
		//System.out.println(newMessages.size());
		for (ChatMessage message : newMessages) {
			try {
                if(this.context.get(message.getChannel()) == null){
                    this.context.put(message.getChannel(), "Basic");
                } 
				Intent intent = null;
				// Special case: `!` commands
				// System.out.println(this.knownIntents.toString());
				if (message.getText().startsWith("!")) {
					// Split at first occurring whitespace
                    System.out.println("This was a command");
                
					String splitMessage[] = message.getText().split("\\s+", 2);

					// First word without '!' prefix
					String intentKeyword = splitMessage[0].substring(1);
					IncomingMessage incMsg = this.knownIntents.get(intentKeyword);
					// TODO: Log this? (`!` command with unknown intent / keyword)
					if (incMsg == null) {
						if(this.context.get(message.getChannel()) == "Basic") {
						continue;
						} else {
							incMsg = new IncomingMessage(intentKeyword, "");
							incMsg.setEntityKeyword("newEntity");
						}
					}

					String entityKeyword = incMsg.getEntityKeyword();
					String entityValue = null;
					// Entity value is the rest of the message. The whole rest
					// is in the second element, since we only split it into two parts.
					if (splitMessage.length > 1) {
						entityValue = splitMessage[1];
					}

					intent = new Intent(intentKeyword, entityKeyword, entityValue);
				} else {
                    // what if you want to start an assessment with a command? 
                    System.out.println("Intent Extraction now with  : " + this.context.get(message.getChannel()));
                    if( this.context.get(message.getChannel()) == "Basic" ){
                        intent = bot.getRasaServer("0").getIntent(message.getText());
                        System.out.println("Extracted Basic");
                    } else {
                        intent = bot.getRasaServer(context.get(message.getChannel())).getIntent(message.getText());
                    }                  
					
				}
                System.out.println(intent.getKeyword());
				String triggeredFunctionId = null;
				IncomingMessage state = this.stateMap.get(message.getChannel());
				if(state!= null) {
					System.out.println(state.getIntentKeyword());

				}
				// No conversation state present, starting from scratch
				// TODO: Tweak this
				if(this.context.get(message.getChannel()) == "Basic"){
					if (intent.getConfidence() >= 0.40f) {
						if (state == null) {
							state = this.knownIntents.get(intent.getKeyword());
							System.out.println(
									intent.getKeyword() + " detected with " + intent.getConfidence() + " confidence.");
							stateMap.put(message.getChannel(), state);
						} else {
							// any is a static forward
							// TODO include entities of intents
							if (state.getFollowingMessages() == null) {
								System.out.println("no follow up messages");
								state = this.knownIntents.get(intent.getKeyword());
								System.out.println(
										intent.getKeyword() + " detected with " + intent.getConfidence() + " confidence.");
								stateMap.put(message.getChannel(), state);
							} else if (state.getFollowingMessages().get(intent.getKeyword()) != null) {
								System.out.println("try follow up message");
								state = state.getFollowingMessages().get(intent.getKeyword());
								stateMap.put(message.getChannel(), state);
							} else {
								System.out.println(intent.getKeyword() + " not found in state map. Confidence: "
										+ intent.getConfidence() + " confidence.");
								// try any
								if (state.getFollowingMessages().get("any") != null) {
									state = state.getFollowingMessages().get("any");
									stateMap.put(message.getChannel(), state);
								} else {
									state = this.knownIntents.get("default");
								//	System.out.println(state.getIntentKeyword() + " set");
								}
							}
						}
					} else {
						System.out.println(
								intent.getKeyword() + " not detected with " + intent.getConfidence() + " confidence.");
						state = this.knownIntents.get("default");
						System.out.println(state.getIntentKeyword() + " set");
					}
                } else if(intent.getConfidence() < 0.40f) {
                	intent = new Intent("default","","");
                }
				Boolean contextOn = false; 
				// No matching intent found, perform default action
                if(this.context.get(message.getChannel()) != "Basic"){
                    triggeredFunctionId = this.triggeredFunction.get(message.getChannel());
                    contextOn = true; 
                } else {
                    if (state != null) {
                    	ChatResponse response = null;
                    	if(intent.getEntitieValues().size()==1) {
	                    	for(ChatResponse res : state.getResponseArray() ){
	                    		System.out.println(res.getTriggerEntity());
	                    		if(res.getTriggerEntity().equals(intent.getEntitieValues().get(0))) {
	                    			response = res;
	                    		}
	                    	}
                    	}
                    	if(response == null) {
                    		response = state.getResponse(this.random);
                    	}
                        if(state.getNluID() != ""){
                            System.out.println("NluId is : " + state.getNluID());
                            this.context.put(message.getChannel(), state.getNluID());
                        }                        
                        if (response != null) {
                            if(response.getResponse() != ""){
                            	String split ="";
                            	// allows users to use linebreaks \n during the modeling for chat responses
                            	for(int i = 0; i < response.getResponse().split("\\\\n").length ; i++) {
                            		System.out.println(i);
                            		split += response.getResponse().split("\\\\n")[i] + " \n ";
                            	}
                            	System.out.println(split);
                                this.chatMediator.sendMessageToChannel(message.getChannel(), split);
                            } else {
                                if(response.getTriggeredFunctionId() != ""){
                                    this.triggeredFunction.put(message.getChannel(), response.getTriggeredFunctionId());
                                    contextOn = true; 
                                }
                            }
                        }
                        if(this.context.get(message.getChannel()) != "Basic"){
                            triggeredFunctionId = this.triggeredFunction.get(message.getChannel());
                        } else triggeredFunctionId = state.getTriggeredFunctionId();
                        // If conversation flow is terminated, reset state
                        if (state.getFollowingMessages().isEmpty()) {
                            this.stateMap.remove(message.getChannel());
                        }
                    }
                }
				messageInfos.add(
						new MessageInfo(message, intent, triggeredFunctionId, bot.getName(), bot.getVle().getName(), contextOn));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}