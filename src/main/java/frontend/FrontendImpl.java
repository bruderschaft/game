package frontend;

import base.Abonent;
import base.Address;
import base.Frontend;
import base.MessageSystem;
import gameMech.MsgChangeState;
import gameMech.MsgStandToQueue;
import gameMech.Position;
import gameMech.UserSession;
import messageSystem.MessageSystemImpl;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import source.*;

import javax.servlet.http.*;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.*;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.servlet.*;


/**
 * Created with IntelliJ IDEA.
 * User: maxim
 * Date: 21.09.13
 * Time: 10:02
 * To change this template use File | Settings | File Templates.
 */

//TODO поведение, если такой пользователь уже зашел
public class FrontendImpl extends WebSocketServlet implements Runnable, Abonent, Frontend {
    private MessageSystemImpl ms;
	private Address address;
	private Map<String, UserSession> sessionIdToUserSession= new HashMap<>();
	private Map<Long, UserSession> userIdToUserSession = new HashMap<>();
	private Map<Integer, GameSessionReplica> gameSessionIdToReplica = new HashMap<>();
	private Map<Long, SocketConnect> userIdToSocket = new HashMap<>();
	private ArrayList<Long> connectedUsers = new ArrayList<>();

    Map<String, Object> data = new HashMap<String, Object>();
    Calendar date;

	private static int handleCount = 0; //количество запросов

    public FrontendImpl(MessageSystem ms){
		this.ms = (MessageSystemImpl)ms;
		this.address = new Address();
		ms.addService(this);
    }
	public Address getAddress(){
		return this.address;
	}
	public void run(){
		while(true){
			ms.execForAbonent(this);
			if (!userIdToUserSession.isEmpty()){
				for (int i=0; i < connectedUsers.size(); i++){
					UserSession userSession = userIdToUserSession.get(connectedUsers.get(i));
					if(userSession!=null){
						Long userId = userSession.getUserId();
						if ((!userIdToSocket.isEmpty()) && (userIdToSocket.get(userId)!=null) && (gameSessionIdToReplica.get(userSession.getGameSessionId()).isChanged())){
							SocketConnect socket = userIdToSocket.get(userId);
							String message = gameSessionIdToReplica.get(userSession.getGameSessionId()).getJSON();
 							socket.sendMessage(message);
						}
					}
				}
			}

			try {
				Thread.sleep(25);
			} catch (Exception e){}
		}
	}
	public Long getUserId(String sessionId){
		UserSession userSession = sessionIdToUserSession.get(sessionId);
		return userSession.getUserId();
	}
	public void putSessionIdUserSession(String sessionId, UserSession userSession){
		sessionIdToUserSession.put(sessionId, userSession);
	}
	public UserSession getUserSession(String sessionId){
		return sessionIdToUserSession.get(sessionId);
	}

	public UserSession getUserSession(Long userId){
		return userIdToUserSession.get(userId);
	}

    public void setId(String sessionId, Long userId){
		UserSession userSession = getUserSession(sessionId);
		if (userSession == null) {
			Writter.printConsole("Can't find user session for: " + sessionId);
			return;
		}
		if (userId == null) {
			userSession.setUserId(-1L);
			return;
		}
		userSession.setUserId(userId);
		userIdToUserSession.put(userId, userSession);

		Address frontendAddress = getAddress();
		Address gameMechAddress = userSession.getGameMech();
		System.out.println("frontend.setId(){");
		ms.sendMessage(new MsgStandToQueue(frontendAddress, gameMechAddress, userId, this ));
		System.out.println("ms.sendMessage(new MsgStandToQueue), userId= " + userId);
	}

	public void setGameSession(Long userId, GameSessionReplica gameSessionReplica){
		getUserSession(userId).setGameSession(gameSessionReplica);
	}

	public void setReady(Long userId){
		/*здесь берется сокет по userId и ему кидается сообщение 2.1.1.1.1.1*/
	}

	public void setGameSessionReplica(Integer gameSessionReplicaId, GameSessionReplica gameSessionReplica){
		gameSessionIdToReplica.put(gameSessionReplicaId, gameSessionReplica);
	}


	// TODO: fix WebSockets from here
	@Override
	public void configure(WebSocketServletFactory webSocketServletFactory) {
		webSocketServletFactory.getPolicy().setIdleTimeout(1000000000);
		webSocketServletFactory.setCreator(new WebSocketCreator() {
			@Override
			public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
//				String path = servletUpgradeRequest.getRequestPath();
//				String room = path.substring("/gamemechanics".length() + 1);
// servletUpgradeResponse.addHeader("roomName",room);


				List<HttpCookie> cookieList = servletUpgradeRequest.getCookies();
				Long userId = -1L;
				for (int i = 0; i < cookieList.size(); i++){
					System.out.println(cookieList.get(i).getName());
					if (cookieList.get(i).getName().equals("id")){
						userId = Long.parseLong(cookieList.get(i).getValue());
					}
				}


				SocketConnect socket = new SocketConnect("sss", userId);
				// msg to GM -> room / count player
//				if (nameToRoom.get(room) == null) {
//					Set roomSet = new HashSet();
//					roomSet.add(socket);
//					nameToRoom.put(room, roomSet);
//				} else {
//					nameToRoom.get(room).add(socket);
//				}
				return socket;
			}
		});
	}

	private class SocketConnect implements WebSocketListener {
		private Session session;
		private String roomName;
		private Long userId;

		public SocketConnect(String roomName, Long userId) {
			this.roomName = roomName;
			this.userId = userId;
		}

		private void sendMessage(String message) {
			try {
				session.getRemote().sendString(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void broadcast(String message) {
//			for (SocketConnect socket : nameToRoom.get(roomName)) {
//				if (this == socket)
//					continue;
//				socket.sendMessage(message);
//			}
		}

		@Override
		public void onWebSocketConnect(Session session) {
			this.session = session;
			userIdToSocket.put(userId, this);
			connectedUsers.add(userId);
			System.out.println("Socket opened. userId= " + userId);
		}

		@Override
		public void onWebSocketBinary(byte[] payload, int offset, int len) {

		}

		@Override
		public void onWebSocketClose(int statusCode, String reason) {
//			if (nameToRoom.get(roomName) != null) {
//				nameToRoom.get(roomName).remove(this);
//			}

		}



		@Override
		public void onWebSocketError(Throwable cause) {
			System.out.println("Cause: " + cause);
//			if (nameToRoom.get(roomName) != null) {
//				nameToRoom.get(roomName).remove(this);
//			}
		}

		@Override
		public void onWebSocketText(String message) {
			/*JSONObject jsonObject = new JSONObject(message);
			String id = jsonObject.getString("id");
			String msg = jsonObject.getString("message");
			System.out.println(" id: " + id);
			System.out.println(" message: " + msg);

			UserSession userSession = idToUserSession.get(Long.parseLong(id));
			String gamerLogin = userSession.getLogin();
			Address frontend = messageSystem.getAddressService().getAddressFE();
			Address gameMechanics = messageSystem.getAddressService().getAddressGM();
			messageSystem.sendMessage(new MsgSendEvent(frontend, gameMechanics, gamerLogin + ": " + msg));
			System.out.println("Socket session:");
			System.out.println(session);
			System.out.println("User session");
			System.out.println(userSession);*/


			try {
				System.out.println("message\n\n");
				JSONObject jsonObject = new JSONObject(message);

				double r = Double.parseDouble(jsonObject.getString("r")),
						l = Double.parseDouble(jsonObject.getString("l"));
				double u = Double.parseDouble(jsonObject.getString("u")),
						d = Double.parseDouble(jsonObject.getString("d"));

			//UserSession userSession = userIdToUserSession.get(Long.parseLong(id));
			//String gamerLogin = userSession.getLogin();
			//Address frontend = messageSystem.getAddressService().getAddressFE();
			//Address gameMechanics = messageSystem.getAddressService().getAddressGM();
			//messageSystem.sendMessage(new MsgSendEvent(frontend, gameMechanics, gamerLogin + ": " + msg));

				Position pos = new Position(r-l, u-d);
				System.out.println("position change: " + pos.toString());

				System.out.println("userId: " + userId);
				UserSession us = userIdToUserSession.get(userId);
				Boolean usLeft = us.getLeft();
				System.out.println("userSession.left: " + usLeft);
				Integer gsId = us.getGameSessionId();
				System.out.println("gsId " + gsId);
				Address frontendAddress = getAddress();
				Address gameMechAddress = us.getGameMech();

				ms.sendMessage(new MsgChangeState(frontendAddress, gameMechAddress, gsId, pos, userId, usLeft));
				System.out.println("MsgChangeState has been sent. ");
				System.out.println("MsgChangeState(frontendAddress, gameMechAddress, gsId, pos, userId, usLeft); ");

			} catch (JSONException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			broadcast("Hello");
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response){
        try{
			//счетчик подключений. Логируется каждые 5 секунд
			handleCount++;


			HttpSession session = request.getSession();
			String sessionId = session.getId();

			UserSession userSession = sessionIdToUserSession.get(sessionId);

			String requestURI = request.getRequestURI();

			response.setContentType("text/html;charset=utf-8");

			//запрос сообщения от сервера (найден или нет userId) (аяксом фигачит)
            if (requestURI.equals("/message")){
				String username = userSession.getUsername();
				Long userId = userSession.getUserId();
				if (userId == null) {
					Writter.print(response, "Ждите авторизации");
					return;
				} else if(userId == -1L){
					Writter.print(response, "Неправильный username");
				} else{
					Writter.print(response, "userId = " + userId);
					Cookie cookie = new Cookie("id", userId.toString());
					cookie.setMaxAge(3600 * 24 * 30);
					response.addCookie(cookie);
				}
			//финально окошечко приветствия
			}else if (requestURI.equals("/greeting")){
				String username = userSession.getUsername();
				Long userId = userSession.getUserId();
				data.put("username", username);
				data.put("userId",userId);
				Writter.print(response, PageGenerator.getPage("greeting.html", data));
			//первый приход юзера
			} else if (requestURI.equals("/getstate")){
				//не работает теперь
				System.out.println("state requested. gameSessionIdToReplica.get(userSession.getGameSessionId()).getJSON()");
				Writter.print(response, gameSessionIdToReplica.get(userSession.getGameSessionId()).getJSON());
			} else	if (requestURI.equals("/index")){
				if(userSession == null)  {
					Cookie cookies[] = request.getCookies();
					Long userIdCookie = null;
					if (cookies != null){
						for (int i = 0; i < cookies.length; i++){
							if (cookies[i].getName().equals("id")){
								userIdCookie = Long.parseLong(cookies[i].getValue());
								this.addNewUserSession(request.getSession().getId(), "old");//TODO: пофиксить и запросить юзернейм
								setId(request.getSession().getId(), userIdCookie);
								response.sendRedirect("/greeting");
							}
						}
					}
					if (userIdCookie == null){
						data.put("sessionId", sessionId);
						response.setContentType("text/html;charset=utf-8");
						Writter.print(response, PageGenerator.getPage("index1.html", data));
					}

				} else if (userSession.getUserId() == -1L){
					Cookie cookies[] = request.getCookies();
					Long userIdCookie = null;
					for (int i = 0; i < cookies.length; i++){
						if (cookies[i].getName().equals("id")){
							userIdCookie = Long.parseLong(cookies[i].getValue());
							this.addNewUserSession(request.getSession().getId(), "old");//TODO: пофиксить и запросить юзернейм
							setId(request.getSession().getId(), userIdCookie);
							response.sendRedirect("/greeting");
						}
					}
					if (userIdCookie == null){
						data.put("sessionId", sessionId);
						response.setContentType("text/html;charset=utf-8");
						Writter.print(response, PageGenerator.getPage("index1.html", data));
					}
				}  else	{
						response.sendRedirect("/greeting");
				}
			}
        } catch (Exception e){
        	//System.out.println(e.toString());
			e.printStackTrace();}
    }

	public void addUserSession(String requestSessionId, String requestUsername){
		UserSession userSession = new UserSession(requestSessionId, requestUsername, ms.getAddresService());
		sessionIdToUserSession.put(requestSessionId, userSession);
	}

	private void addNewUserSession(String requestSessionId, String requestUsername){
		UserSession userSession = new UserSession(requestSessionId, requestUsername, ms.getAddresService());
		sessionIdToUserSession.put(requestSessionId, userSession);
	}


    public void doPost(HttpServletRequest request, HttpServletResponse response){
		String requestURI = request.getRequestURI();
		String requestSessionId = request.getSession().getId();
		if (requestURI.equals("/")){
			handleCount++;
			String requestUsername = request.getParameter("name");
			if(requestUsername != null){
				UserSession userSession = new UserSession(requestSessionId, requestUsername, ms.getAddresService());
				sessionIdToUserSession.put(requestSessionId, userSession);

				Address frontendAddress = getAddress();
				Address accountServiveAddress = userSession.getAccountService();

				ms.sendMessage(new MsgGetUserId(frontendAddress, accountServiveAddress, requestUsername, requestSessionId));

				try {
					response.setContentType("text/html;charset=utf-8");
					response.setStatus(HttpServletResponse.SC_OK);
					data.put("message","Ждите авторизации");
					Writter.print(response,PageGenerator.getPage("waiting.html", data));
				} catch (Exception e){}
			}
    	} else if(requestURI.equals("/change")){
			System.out.println("post-request on /change");
			try {
			JSONObject jsonObject = new JSONObject(request.getParameter("ss"));
			//JSONObject L = jsonObject.getJSONObject("L");
			//JSONObject R = jsonObject.getJSONObject("R");
//
//			Integer lx = Integer.parseInt(L.getString("X"));
//			Integer ly = Integer.parseInt(L.getString("Y"));
//			Integer lr = Integer.parseInt(L.getString("R"));
//			Integer rx = Integer.parseInt(R.getString("X"));
//			Integer ry = Integer.parseInt(R.getString("Y"));
//			Integer rr = Integer.parseInt(R.getString("R"));

			double r = Double.parseDouble(jsonObject.getString("r")),
					l = Double.parseDouble(jsonObject.getString("l"));
			double	u = Double.parseDouble(jsonObject.getString("u")),
					d = Double.parseDouble(jsonObject.getString("d"));

			Position pos = new Position(r-l, u-d);
			System.out.println("position change: " + pos.toString());
			Cookie cookie[] = request.getCookies();
			Long userId = -1L;
			for (int i = 0; i < cookie.length; i++){
				if (cookie[i].getName().equals("id")){
					userId = Long.parseLong(cookie[i].getValue());
				}
			}
			System.out.println("userId: " + userId);
			UserSession us = userIdToUserSession.get(userId);
			Boolean usLeft = us.getLeft();
			System.out.println("userSession.left: " + usLeft);
			Integer gsId = us.getGameSessionId();
			System.out.println("gsId " + gsId);
			Address frontendAddress = getAddress();
			Address gameMechAddress = us.getGameMech();

			ms.sendMessage(new MsgChangeState(frontendAddress, gameMechAddress, gsId, pos, userId, usLeft));
			System.out.println("MsgChangeState has been sent. ");
			System.out.println("MsgChangeState(frontendAddress, gameMechAddress, gsId, pos, userId, usLeft); ");
			} catch (JSONException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}
	}
}
