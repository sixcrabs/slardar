package cn.piesat.nj.slardar.gateway.helper;

import cn.piesat.nj.slardar.core.SlardarSecurityHelper;
import cn.piesat.nj.slardar.core.entity.Account;
import cn.piesat.nj.slardar.core.entity.UserProfile;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/4/3
 */
@Component
public class SlardarApiGatewayFilter extends OncePerRequestFilter implements Ordered {

    private static final String SLARDAR_HEADER_ATTR_PREFIX = "x-auth-";

    @Override
    public int getOrder() {
        return -2023;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        SlardarSecurityHelper.SecurityContext context = SlardarSecurityHelper.getContext();
        context.setUserProfile(resolveProfile(request));
        context.setAccount(resolveAccount(request));
        chain.doFilter(request, response);
    }

    private UserProfile resolveProfile(HttpServletRequest request) {
        // request.getHeader(SLARDAR_HEADER_ATTR_PREFIX.concat())

        return null;

    }

    private Account resolveAccount(HttpServletRequest request) {

        return null;

    }
}
