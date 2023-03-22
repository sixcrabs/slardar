## SSO-sample

前后端分离模式下 实现 单点登录

sso-client as SC
sso-server as SS

单点登录：

前端进入首页时，向 SC 发起请求，验证当前是否已登录（登录状态是否有效），如果无效 则跳转到登录路由，在登录路由页面，
先请求 SC 获取认证中心地址，SC 后台拼接地址返回，前端接受到地址，在回调里转向到该地址（eg： http://xxx.com?redirectUrl=xxx.com?back=xxx.com）
，此时，SS 接受到认证请求，跳转到 SS 的默认登录页面，进行登录后，派发 ticket 并重定向到 上述的前端页面，前端页面加载时 判断 ticket 是否存在，若存在，则
使用 ticket 值 访问 SC 接口，验证身份 并拿到 token ，完成 登录

单点注销： 
> TODO


