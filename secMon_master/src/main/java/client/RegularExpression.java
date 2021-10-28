package client;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegularExpression {
    private static final  String CRLF = "\r\n";
    private static final  String PORT = "([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])";
    private static  final  String CHARACTER = "(\\s|\\w)";
    private static final  String CHARACTER_PASS = "\\w";
    private static final  String ID = "[a-zA-Z0-9]{5,10}";
    private static final  String PROTOCOL = "[a-zA-Z]{3,15}";
    private static final  String USER_NAME = "[a-zA-Z0-9]{3,50}";
    private static final  String PASSWORD = String.format("%s{3,50}", CHARACTER_PASS);
    private static final  String HOST = "([a-zA-Z0-9]|\\.|\\_|\\-){3,50}";
    private static final  String PATH = "\\/([a-zA-Z0-9]|\\.|\\_|\\-|\\/){0,100}";
    private static final  String URL = String.format("%s:\\/\\/(%s(:%s)?@)?%s(:%s)?%s", PROTOCOL, USER_NAME, PASSWORD,
            HOST, PORT, PATH);
    private static final String STATE = "(OK)|(ALARM)|(DOWN)";
    private static final String MESSAGE = String.format("%s{1,200}", CHARACTER);
    private static final String ADD_SERV_RESP = String.format("(\\+OK|-ERR)(%s)?%s",MESSAGE,CRLF );
    private static final String LIST_SERVICE_RESP = String.format("SRV( %s){0,100}%s",ID,CRLF );
    private static final String STATE_SERVICE_RESQ= String.format("STATE %s %s %s%s",ID,URL,STATE,CRLF );
    
    
    public static boolean addServiceResqMatch(String message) {
        Pattern p = Pattern.compile(ADD_SERV_RESP);
        Matcher m = p.matcher(message);
        return m.find();
    }
    
    public static boolean isServerList(String message) {
        Pattern p = Pattern.compile(LIST_SERVICE_RESP);
        Matcher m = p.matcher(message);
        return m.find();
    }
    
    public static List<String> getServerList(String message){
        Pattern p = Pattern.compile(ID);
        Matcher m = p.matcher(message);
        List<String> serv = new ArrayList<>();
        while(m.find()) {
            serv.add(m.group());
        }
        return serv;
    }
    
    public static  boolean isStateReq(String message) {
        Pattern p = Pattern.compile(STATE_SERVICE_RESQ);
        Matcher m = p.matcher(message);
        return m.find();
    }


}
