package cn.piesat.nj.slardar.sso.server.web;

import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static cn.piesat.nj.slardar.sso.server.SsoConstants.SSO_LOGIN_VIEW_URL;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/28
 */
@Controller
public class SsoServerController {

    @Resource
    private SlardarProperties slardarProperties;

    /**
     * sso 认证中心 默认登录页面
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = SSO_LOGIN_VIEW_URL, method = RequestMethod.GET)
    public ModelAndView ssoLoginView(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        mav.getModel().put("loginUrl", slardarProperties.getLogin().getUrl());
        mav.setViewName("sso-login");
        return mav;
    }
}
