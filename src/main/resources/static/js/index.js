$(function () {
    $("#publishBtn").click(publish);
});

function publish() {
    $("#publishModal").modal("hide");

    // 获取标题和内容
    let title = $("#recipient-name").val();
    let content = $("#message-text").val();

    // 发送异步请求
    $.post(
        CONTEXT_PATH + "/discuss/add",
        {"title": title, "content": content},
        function (data) {
            data = $.parseJSON(data);
            console.log(data)
            // 渲染提示框的提示消息
            $("#hintBody").text(data.msg);
            $("#hintModal").modal("show");
            setTimeout(function () {
                $("#hintModal").modal("hide");
                // 刷新页面
                if (data.code == 200) {
                    location.reload();
                }
            }, 2000);
        }
    );


}