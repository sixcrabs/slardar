## SSO

前后端分离模式下 实现 单点登录

sso-client1 as SC1
sso-client2 as SC2

sso-server as SS

单点登录(前后端分离模式)：

### 流程

前端访问 SC1 首页，向 SC1 发起请求，验证当前是否已登录（登录状态是否有效），
SC1 验证登录状态：通过 http 请求 SS 进行身份验证 ，如果有效，则返回用户信息 结束
如果无效 则跳转到登录路由，在登录路由页面， 先请求 SC 获取认证中心地址，（/sso/auth）SC 后台拼接地址返回，前端接受到地址，在回调里转向到该地址（eg： http://xxx/sso/auth?redirectUrl=xxx.com?back=xxx.com）
此时，SS 接受到认证请求，跳转到 SS 的默认登录页面(/sso-login)，进行登录后(此时SS的前端页面会缓存登录成功后的 token)，派发 ticket 并重定向到 上述的前端页面，
前端页面加载时 判断 ticket 是否存在，若存在，则 使用 ticket 值 访问 SC 接口，验证身份 并拿到 token ，完成 登录


客户端访问 SC2 首页，SC2 服务端验证当前是否已登录，未登录，则跳转到 SS 的登录页， 此时由于已经有了 token， 则无需输入用户名 密码，自动生成 ticket，跳转到 SC2 的回调地址
SC2 服务端拿到 ticket，rest 调用SS 接口验证 ticket 并换取 token，返回到 SC2 前端页面，SC2 前端缓存 token，后续接着访问接口


单点注销：
> TODO


