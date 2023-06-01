// sa 
var sa = {};

// 打开loading
sa.loading = function (msg) {
    layer.closeAll();	// 开始前先把所有弹窗关了
    return layer.msg(msg, {icon: 16, shade: 0.3, time: 1000 * 20, skin: 'ajax-layer-load'});
};

// 隐藏loading
sa.hideLoading = function () {
    layer.closeAll();
};


// ----------------------------------- 登录事件 -----------------------------------

$('.login-btn').click(function () {
    sa.loading("正在登录...");
    // 开始登录 调用 slander-starter 自带的 登录url
    setTimeout(function () {
        $.ajax({
            url: loginUrl,
            type: "post",
            data: {
                username: $('[name=name]').val(),
                password: $('[name=pwd]').val()
            },
            dataType: 'json',
            success: function (res) {
                console.log('返回数据：', res);
                sa.hideLoading();
                if (res.code === 0) {
                    layer.msg('登录成功', {anim: 0, icon: 6});
                    setTimeout(function () {
                        // 这里刷新当前页面 进入认证中心的判断逻辑
                        location.reload();
                    }, 800)
                } else {
                    layer.msg(res.msg, {anim: 6, icon: 2});
                }
            },
            error: function (xhr, type, errorThrown) {
                sa.hideLoading();
                if (xhr.status === 0) {
                    return layer.alert('无法连接到服务器，请检查网络');
                }
                var resText = xhr.responseText;
                var res = JSON.parse(resText);
                return layer.alert(res.message, {icon: 0});
            }
        });
    }, 400);
});

// 绑定回车事件
$('[name=name],[name=pwd]').bind('keypress', function (event) {
    if (event.keyCode === "13") {
        $('.login-btn').click();
    }
});

// 输入框获取焦点
$("[name=name]").focus();