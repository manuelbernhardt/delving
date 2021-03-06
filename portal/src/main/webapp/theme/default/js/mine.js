$(document).ready(function() {
    $("#savedItems").tabs({selected: $.cookie('ui-tabs-3')});
    $("#savedItems").tabs({ cookie: { expires: 30 } });
});

function removeRequest(className, id) {
    $.ajax({
        type: "POST",
        url: "/portal/remove.ajax",
        data: "className=" + className + "&id=" + id,
        success: function(msg) {
            window.location.reload();
        },
        error: function(msg) {
            alert("An error occured. The item could not be removed");
        }
    });
}

function saveRequest(className, id) {
    $.ajax({
        type: "POST",
        url: portalName + "/save.ajax",
        data: "className=" + className + "&id=" + id,
        success: function(msg) {
            window.location.reload();
        },
        error: function(msg) {
            alert("An error occured. The item could not be saved");
        }
    });
}


function showDefault(obj, iType) {
    switch (iType) {
        case "TEXT":
            obj.src = "/portal/images/item-page.gif";
            break;
        case "IMAGE":
            obj.src = "/portal/images/item-image.gif";
            break;
        case "VIDEO":
            obj.src = "/portal/images/item-video.gif";
            break;
        case "SOUND":
            obj.src = "/portal/images/item-sound.gif";
            break;
        default:
            obj.src = "/portal/images/item-page.gif";
    }
}
