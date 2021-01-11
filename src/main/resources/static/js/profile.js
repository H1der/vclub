$(function () {
    $(".follow-btn").click(follow);
});

function follow() {
    var btn = this;
    if ($(btn).hasClass("btn-info")) {
        // 关注TA
        $.post(CONTEXT_PATH + "/follow", {"entityType": 3, "entityId": $("#entityId").val()}, function (data) {
            data = $.parseJSON(data);
            if (data.code == 200) {
                location.reload();
            } else {
                alert(data.msg)
            }

        })
        // $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
    } else {
        $.post(CONTEXT_PATH + "/unfollow", {"entityType": 3, "entityId": $("#entityId").val()}, function (data) {
            data = $.parseJSON(data);
            if (data.code == 200) {
                location.reload();
            } else {
                alert(data.msg)
            }

        })
        // 取消关注
        // $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
    }
}