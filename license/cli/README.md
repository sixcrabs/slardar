# slardar-license-cli

## 生成许可文件
```shell
java -jar slardar-license-cli-1.0.jar generate -o my.lic -cname=test -cemail=admin@qq.com-cexpired=20250922 -v
```

## 验证许可
```shell
java -jar slardar-license-cli-1.0.jar verify -i /xxx/my.lic -v
```