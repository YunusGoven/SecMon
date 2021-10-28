package be.secMon.main.daemon;

import java.util.Map;

public interface ILauncher{
    void setState(Map<String,String> state);
    Map<String, String> getState();

    void removeAllStateForProtocol(String protocol);
}
