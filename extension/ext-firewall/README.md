## 防火墙扩展

slardar 中用于拦截一些可能造成攻击的危险请求，例如启用了`BlackPath`的过滤器，则会对相应的请求path进行拦截并返回

内部实现了 `blackPath`、`headers`、 `hosts` 等拦截器，也支持自定义实现：

```java

@Component
public class MyFirewallHandler implements SlardarFirewallHandler {

    /**
     * 执行校验
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param context  上下文 用于在运行时获取bean等
     * @param params   预留扩展参数
     */
    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, SlardarContext context, Object params) throws SlardarException {
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            if (parameterName.contains("xss")) {
                throw new SlardarException("参数key中包含敏感词!");
            }
        }
    }

    /**
     * 是否启用该 handler 默认true
     *
     * @return
     */
    @Override
    public boolean isEnabled() {
        // false : 不启用
        return true;
    }
}
```

启用后，一旦请求参数含有 `xss` 字符串，则返回错误信息:

```json
{
  "message": "参数中包含敏感词!",
  "code": 500
}
```