package i5.las2peer.services.socialBotManagerService.parser.creation.function;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;

import i5.las2peer.services.socialBotManagerService.parser.creation.parameter.CreationParameter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = ServiceType.class, value = "las2peer")
public class Las2peer extends ServiceType {

	@ApiModelProperty(value = "Which intent should activate the service access?", required = true, example = "greet")
	String intent;
	@ApiModelProperty(value = "the service alias", required = true, example = "SBFManager")
	String serviceAlias;
	@ApiModelProperty(value = "The function Name", required = true, example = "getBots")
	String functionName;
	@ApiModelProperty(value = "URL of the openAPI definition", required = true, example = "https://petstore.swagger.io/v2/swagger.json")
	Collection<CreationParameter> parameters;

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

	public Collection<CreationParameter> getParameters() {
		return parameters;
	}

	public void setParameters(Collection<CreationParameter> parameters) {
		this.parameters = parameters;
	}

	public String getServiceAlias() {
		return serviceAlias;
	}

	public void setServiceAlias(String serviceAlias) {
		this.serviceAlias = serviceAlias;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	@JsonIgnore
	public ServiceAccessType getAccessType() {
		return ServiceAccessType.las2peer;
	}

}