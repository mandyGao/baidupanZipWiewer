package hotstu.github.bdzviewer.baiduapi;

import java.util.LinkedList;
import java.util.Observable;

public class LoadingQuene extends Observable {

    private static class SingletonHolder {
        private static final LoadingQuene INSTANCE = new LoadingQuene(3);
    }

    private LinkedList<String> preUrls;
    private final int capacity;

    public static LoadingQuene instance() {
        return SingletonHolder.INSTANCE;
    }

    private LoadingQuene(int capacity) {
        super();
        this.capacity = capacity;
        this.preUrls = new LinkedList<String>();
    }

    /**
     * 
     * @return 当前正在下载的url
     */
    public LinkedList<String> getUrls() {
        return preUrls;
    }

    public void enquene(String url) {
        if (preUrls.contains(url)) {
            return;
        }
        preUrls.addLast(url);
        while (preUrls.size() > capacity) {
            preUrls.removeFirst();
        }
        setChanged();
        notifyObservers();
    }

}
