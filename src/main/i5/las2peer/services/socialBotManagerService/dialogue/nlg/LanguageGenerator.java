package i5.las2peer.services.socialBotManagerService.dialogue.nlg;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;

public abstract class LanguageGenerator {

    abstract public ResponseMessage parse(DialogueAct act);
    
    abstract public void addEntry(String intent, String message);
}
