package py.pol.una.ii.pw.rest;

/**
 * Created by carlitos on 25/04/17.
 */

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class RestAuthenticationFilter implements javax.servlet.Filter {
    public static final String AUTHENTICATION_HEADER = "X-sessionid";


    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain filter) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            String path =( (HttpServletRequest)request).getPathInfo();
            if ((path != null) && (!path.startsWith("/login"))) {
                HttpServletRequest httpServletRequest = (HttpServletRequest) request;
                String authCredentials = httpServletRequest
                        .getHeader(AUTHENTICATION_HEADER);

                // better injected
                AuthenticationService authenticationService = new AuthenticationService();

                boolean authenticationStatus = authenticationService
                        .authenticate(authCredentials);

                if (authenticationStatus) {
                    filter.doFilter(request, response);
                } else {
                    if (response instanceof HttpServletResponse) {
                        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                        httpServletResponse
                                .setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            }else
                filter.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
}
