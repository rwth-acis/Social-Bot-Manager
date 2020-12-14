package i5.las2peer.services.socialBotManagerService.chat;

public class ChatMessage {

	private String channel;
	private String user;
	private int role;
	private String email;
	private String text;
	private String command;
	private double timestamp;
	private String fileContent;

	public ChatMessage(String channel, String user, String text) {
		this.channel = channel;
		this.user = user;
		this.text = text;
	}

	public ChatMessage(String channel, String user, String text, String timestamp) {
		this(channel, user, text);
		this.timestamp = Double.parseDouble(timestamp);
	}

	public ChatMessage() {
		// TODO Auto-generated constructor stub
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getChannel() {
		return this.channel;
	}

	public String getUser() {
		return this.user;
	}

	public String getText() {
		return this.text;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public String getCommand() {
		return command;
	}

	public boolean hasCommand() {
		return this.command != null;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public String toString() {
		return "ChatMessage [channel=" + channel + ", text=" + text + "]";
	}

	public String getFileContent() {
		return fileContent;
	}

	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}
}
