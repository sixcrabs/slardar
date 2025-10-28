# slardar-license-cli

## 生成许可文件
```shell
java -jar slardar-license-cli-1.0.jar generate -o my.lic -p varok-v1.2 -cname=test -cemail=admin@qq.com -cexpired=20250922 -v
```

> 说明
> -o 指定输出文件名，默认使用 cname 作为文件名称
> -p 产品名称或产品码 必须
> -c 客户（使用者）信息
  - name 客户名称
  - email 客户邮箱
  - expired 过期时间 必须 格式 yyyyMMdd

## 验证许可
```shell
java -jar slardar-license-cli-1.0.jar verify -i /xxx/my.lic -v
```