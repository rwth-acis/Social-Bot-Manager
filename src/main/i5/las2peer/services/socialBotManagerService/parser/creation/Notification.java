package i5.las2peer.services.socialBotManagerService.parser.creation;

import java.net.URL;

import i5.las2peer.services.socialBotManagerService.parser.creation.function.CreatorFunction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = CreatorFunction.class, value = "Notification")
public class Notification extends CreatorFunction {

    @ApiModelProperty(value = "The url of the service that should send a notification", required = true, example = "https://petstore3.swagger.io/")
    private URL serviceURL;

    @ApiModelProperty(dataType = "string", value = "The function that should send a notification", required = true, example = "addPet")
    private String operationID;

    @ApiModelProperty(dataType = "string", value = "The message that should be send to the user", required = true, example = "a pet was created")
    private String message;

    public URL getServiceURL() {
		return serviceURL;
	}

    public void setServiceURL(URL serviceURL) {
		this.serviceURL = serviceURL;
	}

	public String getOperationID() {
		return operationID;
	}

	public void setOperationID(String operationID) {
		this.operationID = operationID;
	}

    @Override
    public String toString() {
	return "NotificationFunction [serviceURL=" + serviceURL + ", operationID=" + operationID + "]";
    }

    public String getMessage() {
	return message;
    }

    public void setMessage(String message) {
	this.message = message;
    }

}
