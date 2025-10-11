package org.winterfell.slardar.license.manager.web;

import cn.piesat.v.misc.hutool.mini.StringUtil;
import org.winterfell.slardar.starter.support.HttpServletUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 许可管理页面controller
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/28
 */
@Controller
public class LicenseManageController {

    /**
     * sso 认证中心 默认登录页面
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/licenseManage", method = RequestMethod.GET)
    public ModelAndView licenseView(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        // 这里根据租户信息指向不同的页面
        String realm = HttpServletUtil.getHeadersAsMap(request).getOrDefault("realm", "");
        mav.setViewName(StringUtil.isBlank(realm) ? "index" : realm + "/index");
        return mav;
    }
}
