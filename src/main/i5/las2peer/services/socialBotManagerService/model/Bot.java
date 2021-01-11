package i5.las2peer.services.socialBotManagerService.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.websocket.DeploymentException;

import i5.las2peer.services.socialBotManagerService.chat.ChatService;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.Language;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.LanguageGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.notification.EventToMessageTrigger;
import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;
import i5.las2peer.services.socialBotManagerService.nlu.NLUGenerator;
import i5.las2peer.services.socialBotManagerService.nlu.RasaNlu;
import i5.las2peer.services.socialBotManagerService.parser.ParseBotException;
import i5.las2peer.services.socialBotManagerService.parser.creation.CreationFunction;
import i5.las2peer.services.socialBotManagerService.parser.creation.Function;

public class Bot {

	/**
	 * Corresponds to BotAgent identifier
	 */
	private String id;
	/**
	 * Corresponds to BotAgent LoginName
	 */
	private String name;
	private String version = "1.0.0";
	private String service;
	private String botAgent;
	private String description;
	private VLE vle;

	/**
	 * Indicates if this bot is active (value) on a VLE environment (key)
	 */
	private Map<String, Boolean> active;
	private Map<String, ServiceFunction> botServiceFunctions;
	private Set<Trigger> triggerList;
	private Map<String, ContentGenerator> generatorList;
	private Map<String, Messenger> messengers;

	private Map<String, Collection<EventToMessageTrigger>> activeTriggers;
	private Map<String, LanguageUnderstander> nlus;
	private Map<String, LanguageGenerator> nlgs;
	private Language language = Language.ENGLISH;
	private Map<String, Function> creationFunctions;
	private BotModel model;

	public Bot(BotModel model) {
		this();
		this.model = model;
	}

	public BotModel getModel() {
		return this.model;
	}

	public Bot() {

		this.botServiceFunctions = new HashMap<>();
		this.triggerList = new HashSet<>();
		this.generatorList = new HashMap<>();
		this.active = new HashMap<>();
		this.messengers = new HashMap<>();
		this.nlus = new HashMap<>();
		this.creationFunctions = new HashMap<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public Map<String, ServiceFunction> getBotServiceFunctions() {
		return botServiceFunctions;
	}

	public void setServiceFunctions(HashMap<String, ServiceFunction> serviceFunctions) {
		this.botServiceFunctions = serviceFunctions;
	}

	public void addBotServiceFunction(String name, ServiceFunction serviceFunction) {
		this.botServiceFunctions.put(name, serviceFunction);
	}

	public RasaNlu getRasaServer(String id) {
		if (this.nlus.get(id) instanceof RasaNlu)
			return (RasaNlu) this.nlus.get(id);
		return null;
	}

	public RasaNlu addRasaServer(NLUKnowledge nlu) {
		RasaNlu rasa = NLUGenerator.createRasaNLU(nlu);
		String id = nlu.getId();
		if (id == null)
			id = String.valueOf(this.nlus.size());
		if (id.contentEquals("0") && this.nlus.containsKey("0"))
			id = String.valueOf(this.nlus.size());
		this.nlus.put(id, rasa);
		return rasa;
	}

	public Map<String, LanguageUnderstander> getNLUs() {
		return this.nlus;
	}

	public Set<Trigger> getTriggerList() {
		return triggerList;
	}

	public void setTriggerList(HashSet<Trigger> triggerList) {
		this.triggerList = triggerList;
	}

	public void addTrigger(Trigger t) {
		this.triggerList.add(t);
	}

	public Map<String, ContentGenerator> getGeneratorList() {
		return generatorList;
	}

	public void setGeneratorList(HashMap<String, ContentGenerator> generatorList) {
		this.generatorList = generatorList;
	}

	public void addGenerator(String s, ContentGenerator g) {
		this.generatorList.put(s, g);
	}

	public VLE getVle() {
		return vle;
	}

	public void setVle(VLE vle) {
		this.vle = vle;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Map<String, Boolean> getActive() {
		if (this.active == null)
			this.active = new HashMap<>();
		return active;
	}

	public boolean isActive(VLE vle) {
		String key = vle.getName();
		if (this.active.containsKey(key) && this.active.get(key))
			return true;
		return false;
	}

	public void setActive(Map<String, Boolean> active) {
		this.active = active;
	}

	public void setIdActive(String id, boolean active) {
		this.active.put(id, active);
	}

	public Messenger getMessenger(String name) {
		// TODO: I'm not too sure about thread safety when calling
		// something on this. Might need to make ChatMediator
		// methods synchronized?
		return this.messengers.get(name);
	}

	public Messenger getMessenger(ChatService chatservice) {
		for (Messenger messenger : this.messengers.values()) {
			if (messenger.getChatService() == chatservice)
				return messenger;
		}
		return null;
	}

	/**
	 * @return first messenger
	 */
	public Messenger getMessenger() {
		for (Messenger messenger : this.messengers.values())
			return messenger;
		return null;
	}

	public void addMessenger(Messenger messenger) throws IOException, DeploymentException, ParseBotException {
		this.messengers.put(messenger.getName(), messenger);
	}

	public Map<String, Messenger> getMessengers() {
		return this.messengers;
	}

	public void deactivateAll() {
		for (String k : this.active.keySet()) {
			this.active.put(k, false);
		}
	}

	public int countActive() {
		int trueCount = 0;
		for (boolean b : active.values()) {
			if (b)
				trueCount++;
		}
		return trueCount;
	}

	public void activate(String vleName) {
		this.active.put(vleName, true);
	}

	public void handleMessages(ArrayList<MessageInfo> messageInfos) {
		for (Messenger m : this.messengers.values()) {
			m.handleMessages(messageInfos, this);
		}
	}

	public String getBotAgent() {
		return botAgent;
	}

	public void setBotAgent(String botAgent) {
		this.botAgent = botAgent;
	}

	public Map<String, LanguageGenerator> getNLGs() {
		return nlgs;
	}

	public void setNLGs(Map<String, LanguageGenerator> nlgs) {
		this.nlgs = nlgs;
	}

	public void setNLUs(Map<String, LanguageUnderstander> nlus) {
		this.nlus = nlus;
	}

	public boolean hasActiveTrigger(String triggerId) {
		if (this.activeTriggers == null)
			return false;
		return this.activeTriggers.containsKey(triggerId);
	}

	public void addActiveTrigger(EventToMessageTrigger trigger) {

		if (this.activeTriggers == null)
			this.activeTriggers = new HashMap<>();

		if (!this.activeTriggers.containsKey(trigger.getId()))
			this.activeTriggers.put(trigger.getId(), new ArrayList<>());

		this.activeTriggers.get(trigger.getId()).add(trigger);
	}

	public EventToMessageTrigger getActiveTrigger(String triggerId, String eventName) {

		if (this.activeTriggers.containsKey(triggerId)) {
			System.out.println("event name: " + eventName + ", getActiveTrigger, size: "
					+ this.activeTriggers.get(triggerId).size());
			for (EventToMessageTrigger event : this.activeTriggers.get(triggerId)) {
				System.out.println("event: " + event.getEvent().getName());
				if (event.getEvent().getName().equalsIgnoreCase(eventName))
					;
				return event;
			}
		}

		return null;
	}

	public void removeActiveTrigger(String triggerId) {
		this.activeTriggers.remove(triggerId);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLanguage(String value) {
		this.language = Language.fromString(value);
	}

	public Language getLanguage() {
		return this.language;
	}

	public Collection<String> getNLUIntents() {
		Collection<String> res = new ArrayList<>();
		for (Messenger messenger : this.messengers.values())
			res.addAll(messenger.getNLUIntents());
		return res;
	}

	public Collection<String> getNLGIntents() {
		Collection<String> res = new ArrayList<>();
		for (Messenger messenger : this.messengers.values())
			res.addAll(messenger.getNLGIntents());
		return res;
	}

	public Collection<Domain> getDomains() {
		Map<String, Domain> res = new HashMap<>();
		for (Messenger messenger : this.messengers.values()) {
			for (Entry<String, Domain> domain : messenger.getDomains().entrySet())
				res.put(domain.getKey(), domain.getValue());
		}
		return res.values();
	}

	public Collection<CreationFunction> getCreationFunctions() {
		assert messengers != null;

		Collection<CreationFunction> res = new LinkedList<CreationFunction>();
		for (Messenger messenger : this.messengers.values()) {
			if (messenger.getDomains() != null)
				for (Domain domain : messenger.getDomains().values()) {
					if (domain.getCreationFunctions() != null)
						for (CreationFunction function : domain.getCreationFunctions().values())
							res.add(function);
				}
		}
		return res;

	}

	public void setCreationFunctions(Collection<Function> creationFunctions) {
		if (this.creationFunctions == null)
			this.creationFunctions = new HashMap<String, Function>();
		for (Function function : creationFunctions) {
			this.addCreationFunction(function);
		}
	}

	public boolean deleteCreationFunction(String name) {
		if (this.messengers != null)

			for (Messenger messenger : this.messengers.values()) {
				if (messenger.getDomains() != null)
					for (Domain domain : messenger.getDomains().values()) {
						if (domain.getCreationFunctions().containsKey(name)) {
							domain.deleteCreationFunction(name);
							return true;
						}
					}
			}

		return false;
	}

	public void addCreationFunction(Function creationFunction) {
		assert creationFunction != null;

		String name = creationFunction.getName();
		if (name == null || name.contentEquals("")) {
			name = creationFunction.getType() + " " + this.creationFunctions.size();
			creationFunction.setName(name);
		}
		this.creationFunctions.put(name, creationFunction);
	}

}
