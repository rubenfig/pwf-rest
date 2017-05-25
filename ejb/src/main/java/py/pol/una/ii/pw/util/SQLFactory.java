package py.pol.una.ii.pw.util;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.cdi.SessionFactoryProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.io.Reader;

/**
 *
 * @author rubenfig
 */
public class SQLFactory {

    /**
     * Provee el SqlSessionFactory
     *
     * @return
     * @throws java.io.IOException
     */

    @Produces
    @ApplicationScoped
    @SessionFactoryProvider
    public SqlSessionFactory produceFactory() throws Exception {
        String resource = "META-INF/mybatis-config.xml";
        Reader reader = Resources.getResourceAsReader(resource);
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);
        reader.close();
        return factory;
    }
}
