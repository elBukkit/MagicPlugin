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

function initialize() {
    $("#saveButton").button().click(save);
}