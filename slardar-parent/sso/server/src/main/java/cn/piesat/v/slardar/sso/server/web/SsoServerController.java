package cn.piesat.v.slardar.sso.server.web;

import cn.hutool.core.util.StrUtil;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
import cn.piesat.v.slardar.starter.support.HttpServletUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static cn.piesat.v.slardar.sso.server.support.SsoConstants.SSO_LOGIN_VIEW_URL;


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
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = SSO_LOGIN_VIEW_URL, method = RequestMethod.GET)
    public ModelAndView ssoLoginView(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        mav.getModel().put("loginUrl", slardarProperties.getLogin().getUrl());
        mav.getModel().put("captchaEnabled", slardarProperties.getLogin().getCaptchaEnabled());
        // TODO: 这里根据租户信息指向不同的登陆页 从请求header 里获取租户信息 若为空 则默认登陆页
        String realm = HttpServletUtil.getHeadersAsMap(request).getOrDefault("realm", "");
        mav.setViewName(StrUtil.isBlank(realm) ? "sso-login" : realm + "/login");
        return mav;
    }
}
