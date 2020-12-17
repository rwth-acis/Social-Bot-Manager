package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public class SimpleDialogueManager extends AbstractDialogueManager {

	MultivaluedMap<String, String> intentMessageMap;

	public SimpleDialogueManager() {
		super();
		this.intentMessageMap = new MultivaluedHashMap<String, String>();
	}

	@Override
	public DialogueAct handle(Intent intent) {

		List<String> responses = this.intentMessageMap.get(intent.getKeyword());
		if (responses == null || responses.isEmpty()) {
			System.out.println("no responses for intent keyword: " + intent.getKeyword());
			return null;
		}

		String response = responses.get(new Random().nextInt(responses.size()));
		DialogueAct act = new DialogueAct(response);
		act.setIntent(intent.getKeyword());
		return new DialogueAct(response);

	}

	public void addIntent(String intent, String message) {
		this.intentMessageMap.add(intent, message);
	}

	@Override
	public boolean hasIntent(String intent) {
		return this.intentMessageMap.containsKey(intent);
	}

	@Override
	public Collection<String> getNLUIntents() {
		return this.intentMessageMap.keySet();
	}
	
	public String getMessage() {
		
		   Random rnd = new Random();
		   int i = rnd.nextInt(this.intentMessageMap.values().size());
		   Collection<List<String>> mes = this.intentMessageMap.values();
		   Iterator<List<String>> iter = mes.iterator();
		   List<String> first = iter.next();
		   return first.get(0);	
	
	}

	@Override
	public void reset() {

	}

	@Override
	public Collection<String> getNLGIntents() {
		// TODO Auto-generated method stub
		return null;
	}

}
