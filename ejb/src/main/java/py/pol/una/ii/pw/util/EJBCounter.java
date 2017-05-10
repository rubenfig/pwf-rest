package py.pol.una.ii.pw.util;
import javax.ejb.Singleton;

/**
 * Created by carlitos on 29/04/17.
 */

@Singleton
public class EJBCounter {
    private int count=20;

    public void getFromPool() {
        count--;
    }
    public void returnToPool() {
        count++;
    }
    public void dumpPoolSize() {
        System.out.println("Current pool size is "+count);
    }
}
