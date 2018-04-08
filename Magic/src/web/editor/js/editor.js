$(document).ready(initialize);

function save() {
    $("#saveButton").button('disable');
    $.ajax( {
        type: "POST",
        url: "save.php",
        data: {
            spells: jQuery('#editor').val()
        },
        dataType: 'json'
    }).done(function(response) {
        $("#saveButton").button('enable');
        if (!response.success) {
            alert("Save failed: " + response.message);
        }
    });
}

function checkUser() {
    if (user.id == '') {
        $('#userName').text('Anonymous');
        $("#saveButton").button('disable');
        $('#loginButton').show();
        $('#logoutButton').hide();
    }  else {
        $('#userName').text(user.name);
        $("#saveButton").button('enable');
        $('#loginButton').hide();
        $('#logoutButton').show();
    }
}

function initialize() {
    $("#saveButton").button().click(save);
    $('#logoutButton').click(logout);
    $('#loginButton').click(login);
    checkUser();
}