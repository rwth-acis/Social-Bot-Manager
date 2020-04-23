package i5.las2peer.services.socialBotManagerService.chat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Vector;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.io.Files;
import com.rocketchat.common.data.lightdb.document.UserDocument;
import com.rocketchat.common.data.model.ErrorObject;
import com.rocketchat.common.data.model.UserObject;
import com.rocketchat.common.listener.ConnectListener;
import com.rocketchat.common.listener.SubscribeListener;
import com.rocketchat.common.network.ReconnectionStrategy;
import com.rocketchat.common.network.Socket.State;
import com.rocketchat.core.RocketChatAPI;
import com.rocketchat.core.RocketChatAPI.ChatRoom;
import com.rocketchat.core.callback.FileListener;
import com.rocketchat.core.callback.GetSubscriptionListener;
import com.rocketchat.core.callback.LoginListener;
import com.rocketchat.core.callback.MessageListener.SubscriptionListener;
import com.rocketchat.core.callback.RoomListener;
import com.rocketchat.core.callback.RoomListener.GetMembersListener;
import com.rocketchat.core.callback.RoomListener.GetRoomListener;
import com.rocketchat.core.factory.ChatRoomFactory;
import com.rocketchat.core.model.RocketChatMessage;
import com.rocketchat.core.model.RocketChatMessage.Type;
import com.rocketchat.core.model.RoomObject;
import com.rocketchat.core.model.SubscriptionObject;
import com.rocketchat.core.model.TokenObject;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.services.socialBotManagerService.chat.state.StatefulResponse;
import i5.las2peer.services.socialBotManagerService.chat.state.personaldata.DataAsking;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;
import i5.las2peer.services.socialBotManagerService.nlu.RasaNlu;

public class RocketChatMediator extends ChatMediator implements ConnectListener, LoginListener,
		RoomListener.GetRoomListener, SubscribeListener, GetSubscriptionListener, SubscriptionListener {

	private final static String url = "https://chat.tech4comp.dbis.rwth-aachen.de";
	RocketChatAPI client;
	private String username = "las2peer";
	private String password;
	private String token;
	private RocketChatMessageCollector messageCollector = new RocketChatMessageCollector();
	private HashSet<String> activeSubscriptions = null;
	private Map<String, StatefulResponse> states = new HashMap<>();
	private RasaNlu rasa;
	private SQLDatabase database;

	public RocketChatMediator(String authToken, SQLDatabase database, RasaNlu rasa) {
		super(authToken);
		this.database = database;
		password = authToken;
		if (activeSubscriptions == null) {
			activeSubscriptions = new HashSet<String>();
		}
		client = new RocketChatAPI(url);
		client.setReconnectionStrategy(new ReconnectionStrategy(4, 2000));
		client.setPingInterval(15000);
		client.connect(this);
		client.LOGGER.setLevel(Level.OFF);
		this.rasa = rasa;
	}

	@Override
	public void sendMessageToChannel(String channel, String text, OptionalLong id) {

		ChatRoom room = client.getChatRoomFactory().getChatRoomById(channel);
		System.out.println("Sending Message to : " + room.getRoomData().getRoomId());
		room.getMembers(new GetMembersListener() {

			@Override
			public void onGetRoomMembers(Integer arg0, List<UserObject> arg1, ErrorObject arg2) {
				// TODO Auto-generated method stub
				try {
					String userName = "";
					String newText = text;
					for (UserObject u : (ArrayList<UserObject>) arg1) {
						if (!u.getUserId().equals(client.getMyUserId())) {
							userName += u.getUserName() + ", ";
						}
					}

					if (userName.length() > 2) {
						userName = userName.substring(0, userName.length() - 2);
					}
					newText = newText.replace("menteeName", userName);
					newText = newText.replace("\\n", "\n");
					if (newText.length() > 5000) {
						try {
							File tempFile = new File("message.txt");
							FileWriter writer = new FileWriter(tempFile);
							writer.write(newText);
							writer.close();
							room.uploadFile(tempFile, "message.txt", "", new FileListener() {

								@Override
								public void onSendFile(RocketChatMessage arg0, ErrorObject arg1) {
									// TODO Auto-generated method stub
								}

								@Override
								public void onUploadError(ErrorObject arg0, IOException arg1) {
									room.sendMessage(arg0.getMessage());
									room.sendMessage(arg0.getReason());
									tempFile.delete();
								}

								@Override
								public void onUploadProgress(int arg0, String arg1, String arg2, String arg3) {
									// TODO Auto-generated method stub

								}

								@Override
								public void onUploadStarted(String arg0, String arg1, String arg2) {
									// TODO Auto-generated method stub

								}

								@Override
								public void onUploadComplete(int arg0, com.rocketchat.core.model.FileObject arg1,
										String arg2, String arg3, String arg4) {
									tempFile.delete();
								}
							});
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						room.sendMessage(newText);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});

	}

	@Override
	public Vector<ChatMessage> getMessages() {
		Vector<ChatMessage> messages = this.messageCollector.getMessages();
		return messages;
	}

	@Override
	public String getChannelByEmail(String email) {
		List<UserDocument> users = client.getDbManager().getUserCollection().getData();
		for (UserDocument u : users) {
			// TODO Email Matching
			return u.getName();
		}
		return null;
	}

	@Override
	public void onGetRooms(List<RoomObject> rooms, ErrorObject error) {
		if (error == null) {
			try {
				System.out.println("Available rooms: " + rooms.size());
				ChatRoomFactory factory = client.getChatRoomFactory();
				synchronized (factory) {
					ArrayList<ChatRoom> roomList = factory.createChatRooms(rooms).getChatRooms();
					for (ChatRoom room : roomList) {
						if (!activeSubscriptions.contains(room.getRoomData().getRoomId())) {
							room.subscribeRoomMessageEvent(new SubscribeListener() {
								@Override
								public void onSubscribe(Boolean isSubscribed, String subId) {

								}
							}, this);
							activeSubscriptions.add(room.getRoomData().getRoomId());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onLogin(TokenObject token, ErrorObject error) {
		if (error == null) {
			System.out.println("Logged in successfully, returned token " + token.getAuthToken());
			client.getRooms(this);
			this.token = token.getAuthToken();
			GetRoomListener grl = this;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						while (client.getState().equals(State.CONNECTED)) {
							client.getRooms(grl);
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		} else {
			System.out.println("Got error " + error.getMessage());
		}
	}

	@Override
	public void onConnect(String sessionID) {
		System.out.println("Connected to server.");
		client.login(username, password, this);
	}

	@Override
	public void onConnectError(Exception arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnect(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSubscribe(Boolean arg0, String arg1) {
		// System.out.println(arg1);
	}

	public static int countWords(String s) {

		int wordCount = 0;

		boolean word = false;
		int endOfLine = s.length() - 1;

		for (int i = 0; i < s.length(); i++) {
			// if the char is a letter, word = true.
			if (Character.isLetter(s.charAt(i)) && i != endOfLine) {
				word = true;
				// if char isn't a letter and there have been letters before,
				// counter goes up.
			} else if (!Character.isLetter(s.charAt(i)) && word) {
				wordCount++;
				word = false;
				// last word of String; if it doesn't end with a non letter, it
				// wouldn't count without this.
			} else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
				wordCount++;
			}
		}
		return wordCount;
	}

	protected String getTxtFile(String userId, String file) {
		MiniClient textClient = new MiniClient();
		textClient.setConnectorEndpoint(url);
		HashMap<String, String> textClientHeader = new HashMap<String, String>();
		textClientHeader.put("cookie", "rc_uid=" + userId + "; rc_token=" + token + "; ");
		ClientResponse r = textClient.sendRequest("GET", file, "", MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN,
				textClientHeader);
		InputStream in = new ByteArrayInputStream(r.getRawResponse());
		StringWriter writer = new StringWriter();
		String encoding = StandardCharsets.UTF_8.name();
		try {
			IOUtils.copy(in, writer, encoding);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return writer.toString();
	}

	protected int getStudentRole(String email) {
		int role = 0;
		PreparedStatement ps;
		try {

			Connection con = database.getDataSource().getConnection();
			ps = con.prepareStatement("SELECT role FROM users WHERE email=?");
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				role = rs.getInt(1);
			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return role;
	}

	protected String getStudentEmail(String userName) {
		MiniClient textClient = new MiniClient();
		textClient.setConnectorEndpoint(url);
		HashMap<String, String> textClientHeader = new HashMap<String, String>();
		textClientHeader.put("X-User-Id", client.getMyUserId());
		textClientHeader.put("X-Auth-Token", token);
		ClientResponse r = textClient.sendRequest("GET", "api/v1/users.info?username=" + userName, "",
				MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, textClientHeader);
		JSONObject userObject = new JSONObject(r.getResponse());
		JSONArray emails = userObject.getJSONObject("user").getJSONArray("emails");
		return emails.getJSONObject(0).getString("address");
	}

	@Override
	public void onGetSubscriptions(List<SubscriptionObject> subscriptions, ErrorObject error) {
		// Creating Logical ChatRooms using factory class
	}

	protected Boolean checkUserProvidedData(String email) {
		Boolean dataProvided = null;
		PreparedStatement ps;
		try {
			Connection con = database.getDataSource().getConnection();
			ps = con.prepareStatement("SELECT data_provided FROM users WHERE email=?");
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				dataProvided = rs.getBoolean(1);
				if (rs.wasNull()) {
					dataProvided = null;
				}
			}
			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dataProvided;
	}

	protected Boolean checkUserExist(String email) {
		int count = 0;
		PreparedStatement ps;
		try {
			Connection con = database.getDataSource().getConnection();
			ps = con.prepareStatement("SELECT Count(*) FROM users WHERE email=?");
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count > 0;
	}

	private void addNewUser(String email) {
		PreparedStatement ps;
		try {
			Connection con = database.getDataSource().getConnection();
			ps = con.prepareStatement("INSERT into users (email, role) values (?, 2)");
			ps.setString(1, email);
			ps.executeUpdate();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(String arg0, RocketChatMessage message) {
		ChatRoom room = client.getChatRoomFactory().getChatRoomById(message.getRoomId());
		synchronized (room) {
			if (!message.getSender().getUserId().equals(client.getMyUserId())) {
				String email = getStudentEmail(message.getSender().getUserName());
				System.out.println("Email: " + email);
				System.out.println("Message: " + message.getMessage());
				if (!checkUserExist(email)) {
					System.out.println("Add new user: " + email);
					addNewUser(email);
				}

				Boolean dataProvided = checkUserProvidedData(email);
				System.out.println(dataProvided);

				StatefulResponse statefulResponse = states.get(email);

				if (statefulResponse == null && dataProvided == null) {
					DataAsking userDataQuestion = new DataAsking(rasa, database, email);
					room.sendMessage(userDataQuestion.getResponse());
					states.put(email, userDataQuestion);
					return;
				}

				if (statefulResponse != null) {
					statefulResponse = statefulResponse.getNext(message.getMessage());
					states.put(email, statefulResponse);
					if (statefulResponse != null) {
						room.sendMessage(statefulResponse.getResponse());
						return;
					}
				}

				Type type = message.getMsgType();
				if (type.equals(Type.ATTACHMENT)) {
					try {
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									System.out.println("Handling attachement");
									JSONObject j = message.getRawJsonObject();
									String fileType = j.getJSONObject("file").getString("type");
									String fileName = j.getJSONObject("file").getString("name");
									if (fileType.equals("text/plain")) {
										room.sendMessage(
												"Danke für deine Abgabe. Ich leite sie an das Analysesystem “T-MITOCAR” weiter und gebe dir gleich deine Rückmeldung. Das dürfte nur ein paar Sekunden dauern.");

										String email = getStudentEmail(message.getSender().getUserName());
										int role = getStudentRole(email);

										String file = j.getJSONArray("attachments").getJSONObject(0)
												.getString("title_link").substring(1);

										String body = getTxtFile(client.getMyUserId(), file);
										int numWords = countWords(body);
										if (numWords < 350) {
											room.sendMessage("Der Text muss mindestens 350 Woerter enthalten (aktuell: "
													+ numWords + ").");
										} else {

											MiniClient c = new MiniClient();
											c.setConnectorEndpoint("https://las2peer.tech4comp.dbis.rwth-aachen.de");
											HashMap<String, String> headers = new HashMap<String, String>();
											// TODO
											String ending = ".txt";
											File tempFile = null;
											JSONObject bodyJSON = new JSONObject(Collections.singletonMap("text",
													StringEscapeUtils.escapeJson(body)));
											if (role == 1) {
												ending = ".pdf";
												tempFile = new File(message.getRoomId() + ending);
												FileWriter writer = new FileWriter(tempFile);
												writer.write("Wip...");
												writer.close();
												String expertLabel = "1";
												ClientResponse result = c.sendRequest("POST",
														"tmitocar/" + message.getRoomId() + "/" + expertLabel,
														bodyJSON.toString(), MediaType.APPLICATION_JSON,
														MediaType.TEXT_HTML, headers);
												System.out.println("Submitted text: " + result.getHttpCode());
												boolean isActive = true;
												while (isActive) {
													result = c.sendRequest("GET",
															"tmitocar/" + message.getRoomId() + "/status", "");
													isActive = result.getResponse().toLowerCase().contains("true");
													// isActive = Boolean.parseBoolean(result.getResponse());
													System.out.println(isActive);
													try {
														Thread.sleep(1000);
													} catch (Exception e) {
														e.printStackTrace();
													}
												}
												result = c.sendRequest("GET",
														"tmitocar/" + message.getRoomId() + "/compare/" + expertLabel,
														"", MediaType.TEXT_HTML, "application/pdf", headers);

												tempFile = new File(message.getRoomId() + ending);
												Files.write(result.getRawResponse(), tempFile);
											} else if (role == 2) {
												ending = ".png";
												ClientResponse result = c.sendRequest("POST",
														"tmitocar/" + message.getRoomId(), bodyJSON.toString(),
														MediaType.APPLICATION_JSON, "text/html", headers);
												System.out.println("Submitted text: " + result.getHttpCode());
												boolean isActive = true;
												while (isActive) {
													result = c.sendRequest("GET",
															"tmitocar/" + message.getRoomId() + "/status", "");
													isActive = result.getResponse().toLowerCase().contains("true");
													// isActive = Boolean.parseBoolean(result.getResponse());
													System.out.println(isActive);
													try {
														Thread.sleep(1000);
													} catch (Exception e) {
														e.printStackTrace();
													}
												}
												result = c.sendRequest("GET", "tmitocar/" + message.getRoomId(), "",
														MediaType.TEXT_HTML, "image/png", headers);
												InputStream in = new ByteArrayInputStream(result.getRawResponse());
												BufferedImage bImageFromConvert = ImageIO.read(in);
												tempFile = new File(message.getRoomId() + ending);
												ImageIO.write(bImageFromConvert, "png", tempFile);
											} else {
												room.sendMessage(
														"Ich kann dir leider kein Feedback geben. Du erfüllst nicht die notwendingen Bedingungen. Prüfe deine Email Adresse oder deine Kursberechtigungen.");
											}
											if (tempFile != null) {
												room.uploadFile(tempFile, message.getRoomId() + ending, "",
														new FileListener() {

															@Override
															public void onSendFile(RocketChatMessage arg0,
																	ErrorObject arg1) {
																// TODO Auto-generated method stub
															}

															@Override
															public void onUploadError(ErrorObject arg0,
																	IOException arg1) {
																room.sendMessage(arg0.getMessage());
																room.sendMessage(arg0.getReason());
															}

															@Override
															public void onUploadProgress(int arg0, String arg1,
																	String arg2, String arg3) {
																// TODO Auto-generated method stub

															}

															@Override
															public void onUploadStarted(String arg0, String arg1,
																	String arg2) {
																// TODO Auto-generated method stub

															}

															@Override
															public void onUploadComplete(int arg0,
																	com.rocketchat.core.model.FileObject arg1,
																	String arg2, String arg3, String arg4) {
																room.sendMessage("Hier ist deine Wissenslandkarte:");

															}
														});
											}
										}
									} else {
										room.sendMessage(
												"Der Typ `" + fileType + "` wird momentan nicht unterstuetzt.");
									}
									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
								System.out.println("Intent processing finished.");
							}
						}).start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					messageCollector.handle(message);
				}
			}
		}
	}
}