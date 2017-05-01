package py.pol.una.ii.pw.util;

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * Created by carlitos on 29/04/17.
 */
public class EjbInterceptor{
    @EJB
    EJBCounter singleton;

        @AroundInvoke
        public Object defaultMethod(InvocationContext context) throws
                Exception{
            // Take a bean from the pool
            singleton.getFromPool();

            // Invoke the EJB method
            Object result = context.proceed();

            // Return the bean to the pool
            singleton.returnToPool();

            // Prints out the current pool size
            singleton.dumpPoolSize();
            return result;
        }
}
