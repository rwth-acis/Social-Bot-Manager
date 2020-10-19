package i5.las2peer.services.socialBotManagerService.parser.creation;

import i5.las2peer.services.socialBotManagerService.chat.ChatService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = Messenger.class, value = "Telegram")
public class TelegramMessenger extends Messenger {

    @ApiModelProperty(dataType = "string", value = "The authentication token", required = true, example = "1362647206:AAGInsj3CfS3zJ_mj_09K5-GL7ltEq3ARcM")
    private String token;

    public String getToken() {
	return token;
    }

    public void setToken(String token) {
	this.token = token;
    }

    @Override
    public ChatService getType() {
	return ChatService.TELEGRAM;
    }

    @Override
    public String toString() {
	return "TelegramMessenger [token=" + token + ", getType()=" + getType() + "]";
    }

}