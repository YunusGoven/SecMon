package be.secMon.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.secMon.request.SnmpRequest;

public class RegularExpression {
	public static final String LETTER = "[a-zA-Z]";
	public static final String DIGIT = "\\d";
	public static final String LETTER_DIGIT = String.format("(%s|%s)", LETTER, DIGIT);
	public static final String CRLF = "\r\n";
	public static final String PORT = "([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])";
	public static final String CHARACTER = "(.+)";
	public static final String CHARACTER_PASS = "(\\S+)";
	public static final String SP = " "; // \s
	public static final String ID = String.format("%s{5,10}", LETTER_DIGIT);
	public static final String PROTOCOL = "[a-zA-Z]{3,15}";
	public static final String USER_NAME = "[a-zA-Z0-9]{3,50}";
	public static final String PASSWORD = String.format("(%s){3,50}", CHARACTER_PASS);
	public static final String HOST = "([a-zA-Z0-9]|\\.|\\_|\\-){3,50}";
	public static final String PATH = "\\/(([a-zA-Z0-9]|\\.|\\_|\\-|\\/){0,100})";
	public static final String URL = String.format("(%s):\\/\\/((%s)(:%s)?@)?(%s)(:(%s))?(%s)",PROTOCOL,USER_NAME,PASSWORD,HOST,PORT,PATH );
	public static final String MIN = DIGIT + "{1,8}";
	public static final String MAX = DIGIT + "{1,8}";
	public static final String FREQUENCY = DIGIT + "{1,8}";
	public static final String AUGMENTED_URL = String.format("(%s)!(%s)!(%s)!(%s)!(%s)", ID, URL, MIN, MAX, FREQUENCY);
	public static final String STATE = "(OK)|(ALARM)|(DOWN)";
	public static final String MESSAGE = String.format("%s{1,200}", CHARACTER);
	public static final String STATEREQ = String.format("(STATEREQ) ((%s))", ID);
	public static final String ANNOUNCE= String.format("IAMHERE %s %s\r\n",PROTOCOL,PORT);
	public static final String NOTIFICATION =String.format("NOTIFY %s %s%s",PROTOCOL,PORT, CRLF );
    public static final String GOODBYE = String.format("GOODBYE %s %s%s",PROTOCOL,PORT,CRLF);
    public static final String STATE_RESP=String.format("STATERESP %s %s%s",ID,STATE,CRLF );
    public static final String ADD_SERV_REQ = String.format("ADDSRV %s%s",AUGMENTED_URL,CRLF );
    public static final String LIST_SERVICE_REQ =String.format("LISTSRV%s",CRLF );
    public static final String STATE_SERVICE_REQ = String.format("STATESRV %s%s",ID,CRLF );

	public static boolean match(String message, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(message);
		return m.find();
	}
	
	public static String getMessage(String message) {
		Pattern p = Pattern.compile(CHARACTER_PASS);
		Matcher m = p.matcher(message);
		return m.find() ? m.group(1) : null;
	}
	
	public static String getProtocolName(String message) {
		Pattern p = Pattern.compile(PROTOCOL);
		Matcher m = p.matcher(message);

		return m.find(7) ? m.group(0) : null;
	}

	public static List<String> getServices(String message) {
		Pattern p = Pattern.compile(AUGMENTED_URL);
		Matcher m = p.matcher(message);
		List<String> services = new ArrayList<String>();
		while (m.find()) {
			services.add(m.group());
		}
		return services;
	}

	public static String getId(String message) {
		Pattern p = Pattern.compile(ID);
		Matcher m = p.matcher(message);
		return m.find() ? m.group() : "-1";
	}
	
	public static String[] getValuesFromReq(String message){
		String[] values = new String[2];
		Pattern p = Pattern.compile(STATEREQ);
		Matcher m = p.matcher(message);
		if(m.find()) {
			values[0] = m.group(1);
			values[1] = m.group(3);
		}
		return values;
	}

	public static String[] getValuesFromAURL(String message) {
		Pattern p = Pattern.compile(AUGMENTED_URL);
		Matcher m = p.matcher(message);
		String[] tabMessage = new String[5];
		if (m.find()) {
			tabMessage[0] = m.group(1);
			tabMessage[1] = m.group(3);
			tabMessage[2] = m.group(18);
			tabMessage[3] = m.group(19);
			tabMessage[4] = m.group(20);
			return tabMessage;
		} else {
			return null;
		}
	}
	public static int getPort(String message) {
		Pattern p = Pattern.compile(PORT);
		Matcher m = p.matcher(message);
		int port = -1;
		if (m.find()) {
			try {
				port = Integer.parseInt(m.group());
			} catch (NumberFormatException e) {
				port = -1;
			}
		}
		return port;
	}
	
    public static boolean isAnnouceMessage(String message){
        Pattern p = Pattern.compile(ANNOUNCE);
        Matcher m = p.matcher(message);
        return m.find();
    }
    
    public static boolean isNotificationMessage(String message){
        Pattern p = Pattern.compile(NOTIFICATION);
        Matcher m = p.matcher(message);
        return m.find();
    }
    
    public static boolean isGoodByeMessage(String message) {
        Pattern p = Pattern.compile(GOODBYE);
        Matcher m = p.matcher(message);
        return m.find();
    }
    
    public static String getIdAugmentUrl(String augmentedUrl) {
        Pattern p = Pattern.compile(AUGMENTED_URL);
        Matcher m = p.matcher(augmentedUrl);
        if(m.find()) {
            p = Pattern.compile(ID);
            m = p.matcher(augmentedUrl);
            return m.find()?m.group():null;
        }
        return null;
    }
    
    public static boolean isStateResq(String message){
        Pattern p = Pattern.compile(STATE_RESP);
        Matcher m = p.matcher(message);
        return m.find();
    }
    
    public static String getIdFromResq(String message){
        Pattern p = Pattern.compile(ID);
        Matcher m = p.matcher(message);

        return m.find(9)?m.group(0):null;
    }
    
    public static String getState(String message){
        Pattern p = Pattern.compile(STATE);
        Matcher m = p.matcher(message);

        return m.find()?m.group():"";
    }
    
    public static boolean isAddServiceReq(String message){
        Pattern p = Pattern.compile(ADD_SERV_REQ);
        Matcher m = p.matcher(message);
        return m.find();
    }
    
    public static boolean isListServiceReq(String message){
        Pattern p = Pattern.compile(LIST_SERVICE_REQ);
        Matcher m = p.matcher(message);
        return m.find();
    }
    
    public static boolean isStateServiceReq(String message){
        Pattern p = Pattern.compile(STATE_SERVICE_REQ);
        Matcher m = p.matcher(message);
        return m.find();
    }
    
    public static String getAugmentedUrl(String message){
        Pattern p = Pattern.compile(AUGMENTED_URL);
        Matcher m = p.matcher(message);
        return m.find()?m.group():"";
    }
    
    public static String getIdMessage(String message){
        Pattern p = Pattern.compile(ID);
        Matcher m = p.matcher(message);
        return m.find(7)?m.group():"";
    }
    
    public static String getUrlFromAugmentedUrl(String url) {
        Pattern p = Pattern.compile(URL);
        Matcher m = p.matcher(url);
        return m.find()?m.group():"";
    }
    
    public static SnmpRequest getAugmentedUrlValues(String msg) {
		Pattern p = Pattern.compile(AUGMENTED_URL);
		Matcher m = p.matcher(msg);
		m.find();
		return new SnmpRequest(m.group(1), m.group(6), m.group(10), m.group(13), m.group(16), m.group(18), m.group(19), m.group(20));
	}
}
