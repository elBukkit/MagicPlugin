$(document).ready(initialize);

var checkCodeTimer = null;

function register() {
    var userName = jQuery('#userId').val();
    if (userName.length == 0) {
        jQuery('#userId').addClass('invalid');
        return;
    }
    jQuery('#userId').removeClass('invalid');
    $.ajax( {
        type: "POST",
        url: "getcode.php",
        data: {
            user: userName
        },
        dataType: 'json'
    }).done(function(response) {
        $("#getCodeButton").button('enable');
        if (!response.success) {
            alert("Code fetch failed: " + response.message);
            return;
        }

        displayCode(userName, response.id, response.code);
    });
}

function displayCode(userName, userId, code) {
    $('#codeDiv').text(code);
    $("#codeDialog").dialog({
      modal: true,
      buttons: {
        Cancel: function() {
            if (checkCodeTimer != null) {
                clearTimeout(checkCodeTimer);
                checkCodeTimer = null;
            }
            $(this).dialog("close");
        }
      }
    }).show();
    scheduleCheck(userName, userId, code, 1000)
}

function scheduleCheck(userName, userId, code, timeout) {
    checkCodeTimer = setTimeout(function() {
        checkCode(userName, userId, code, timeout);
    }, timeout);
}

function checkCode(userName, userId, code, timeout) {
    $.ajax( {
        type: "POST",
        url: "checkcode.php",
        data: {
            user: userId,
            code: code
        },
        dataType: 'json'
    }).done(function(response) {
        $("#getCodeButton").button('enable');
        if (response.success) {
            $("#codeDialog").dialog('close');
            document.location = "index.php";
        } else {
            scheduleCheck(userName, userId, code, timeout + 1000);
        }
    });
}

function initialize() {
    $("#getCodeButton").button().click(register);
}