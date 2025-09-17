
 启动需要配置 redis 地址

 配置项 skv.uri

 单点登录场景（默认不同域名，可以理解成完全不同的应用）
 1. 访问应用1，应用1 后端验证是否已登录，没有则跳转到 sso server  统一登录，输入用户名 密码登录成功后，重定向到 应用1 的后台地址，带上 ticket，
 应用1 后台使用 ticket 调用sso server的接口验证 ticket 并拿到用户信息，同时生成 access token 给应用1 的前端