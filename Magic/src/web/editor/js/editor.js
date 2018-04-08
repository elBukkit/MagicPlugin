$(document).ready(initialize);

var saving = false;
function save() {
    if (saving) return;

    saving = true;
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
        saving = false;
        if (!response.success) {
            alert("Save failed: " + response.message);
        }
    });
}

function checkUser() {
    if (user.id == '') {
        $('#userName').text('Anonymous');
        $('#userSkin').css('background-image', '');
        $('#userOverlay').css('background-image', '');
        $("#saveButton").button('disable');
        $("#saveButton").prop('title', 'Log in to save your work');
        $('#loginButton').show();
        $('#logoutButton').hide();
    }  else {
        $('#userName').text(user.name);
        $('#userSkin').css('background-image', 'url("' + user.skin + '")');
        $('#userOverlay').css('background-image', 'url("' + user.skin + '")');
        $("#saveButton").button('enable');
        $("#saveButton").prop('title', 'Save changes and reload the sandbox server');
        $('#loginButton').hide();
        $('#logoutButton').show();
    }
}

function checkKey(event) {
    if (event.key == 's' && event.ctrlKey) {
        save();
    }
}

function initialize() {
    $("#saveButton").button().click(save);
    $('#logoutButton').click(logout);
    $('#loginButton').click(login);
    $('#editor').keyup(checkKey);
    checkUser();
}