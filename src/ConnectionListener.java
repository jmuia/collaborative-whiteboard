/**
 * Created by jmuia on 2016-03-15.
 */
public interface ConnectionListener {
    void connectionClosed(String reason);
    void connectionOpened();
}
