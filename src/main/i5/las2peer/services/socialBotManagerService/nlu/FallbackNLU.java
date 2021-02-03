package i5.las2peer.services.socialBotManagerService.nlu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * This LanguageUnderstander knows basic intents that are needed for dialogue
 * management.
 */
public class FallbackNLU extends LanguageUnderstander {

	final private Collection<String> intents = new HashSet<>();
	final private List<String> confirms = new ArrayList<>();
	final private List<String> denys = new ArrayList<>();
	final private List<String> greets = new ArrayList<>();

	public FallbackNLU() {
		
		intents.add("confirm");
		intents.add("deny");
		
		confirms.add("yes");
		confirms.add("true");
		confirms.add("okay");
		confirms.add("ok");
		confirms.add("correct");
		confirms.add("\\\\uD83C\\\\uDD97");
		confirms.add("\\\\uD83D\\\\uDC4C");
		confirms.add("\\\\uD83D\\\\uDC4D");

		denys.add("no");
		denys.add("false");
		

	}

	@Override
	public Intent parse(String message) {

		Intent intent = null;

		for (String conf : confirms) {
			if (message.contentEquals(conf)) {
				intent = new Intent("confirm", 1.0f);
				intent.setIntentType(IntentType.CONFIRM);
			}
		}

		for (String deny : denys) {
			if (message.contentEquals(deny)) {
				intent = new Intent("deny", 1.0f);
				intent.setIntentType(IntentType.DENY);
			}
		}

		return intent;

	}

	@Override
	public Collection<String> getIntents() {
		return intents;
	}

	@Override
	public TrainingData getTrainingData() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void addTrainingData(TrainingDataEntry data) {
		// TODO Auto-generated method stub		
	}

	@Override
	public String getName() {
		return "fallback";
	}

	@Override
	public String getUrl() {
		return "fallback";
	}

	@Override
	protected void setTrainingData(TrainingData data) {
		// TODO Auto-generated method stub
		
	}



	
}
