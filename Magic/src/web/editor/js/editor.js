$(document).ready(initialize);

var saving = false;
var loading = false;
var spellFiles = null;
function save() {
    if (saving) return;

    saving = true;
    spellFiles = null;
    $("#saveButton").button('disable');
    $.ajax( {
        type: "POST",
        url: "save.php",
        data: {
            spell: jQuery('#editor').val()
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

function startNew() {
    $('#editor').val('');
}

function load() {
    if (loading) return;

    if (spellFiles == null) {
        loading = true;
        $("#loadButton").button('disable');
        $.ajax( {
            type: "POST",
            url: "spells.php",
            dataType: 'json'
        }).done(function(response) {
            loading = false;
            $("#loadButton").button('enable');
            if (!response.success) {
                alert("Failed to fetch spells: " + response.message);
            } else {
                spellFiles = response.spells;
                if (spellFiles != null) {
                    populateSpellFiles();
                    load();
                }
            }
        });
        return;
    }

    $("#loadSpellDialog").dialog({
      modal: true,
      width: 'auto',
      buttons: {
        Cancel: function() {
            $(this).dialog("close");
        },
        "Load": function() {
            $(this).dialog("close");
            loadFile($('#loadSpellSelect').val());
        }
      }
    }).show().keydown(function (event) {
        if (event.keyCode == $.ui.keyCode.ENTER) {
            $(this).parent().find("button:eq(2)").trigger("click");
            return false;
        }
    });
}

function loadFile(fileName) {
    if (fileName == null || fileName.length == 0) return;

    $.ajax( {
        type: "POST",
        url: "spell.php",
        dataType: 'json',
        data: {
            key: fileName
        }
    }).done(function(response) {
        if (!response.success) {
            alert("Failed to fetch spell: " + response.message);
        } else {
            $('#editor').val(response.yml);
        }
    });
}

function populateSpellFiles() {
    var select = $('#loadSpellSelect');
    select.empty();

    spellFiles.sort(function(a, b) {
        var aIsDefault = (a.creator_id == '');
        var bIsDefault = (b.creator_id == '');
        if (aIsDefault && !bIsDefault) {
            return 1;
        } else if (!aIsDefault && bIsDefault) {
            return -1;
        }
        var aIsCreators = (user.id != '' && a.creator_id != '' && a.creator_id == user.id);
        var bIsCreators = (user.id != '' && b.creator_id != '' && b.creator_id == user.id);
        if (aIsCreators && !bIsCreators) {
            return -1;
        } else if (!aIsCreators && bIsCreators) {
            return 1;
        }
        return a.key.localeCompare(b.key);
    });
    var owned = false;
    var unowned = false;
    var defaults = false;
    for (var i = 0; i < spellFiles.length; i++) {
        var spell = spellFiles[i];
        var key = spell.key;
        var isDefault = false;
        if (key.startsWith("default.")) {
            isDefault = true;
            key = key.substr(8);
        }
        var spellName = key + " : " + spell.name + " : " + spell.creator_name + " : " + spell.description;
        if (!owned && spell.creator_id != '' && spell.creator_id == user.id) {
            owned = true;
            select.append($('<optgroup>').prop("label", "Your Spells"));
        } else if (!unowned && owned && (spell.creator_id == ''|| spell.creator_id !== user.id)) {
            unowned = true;
            select.append($('<optgroup>').prop("label", "Other Sandbox Spells"));
        } else if (!defaults && isDefault) {
            defaults = true;
            select.append($('<optgroup>').prop("label", "Default Spells"));
        }
        var option = $('<option>').val(spell.key).text(spellName);
        select.append(option);
    }
}

function checkMode() {
    if (this.id == 'editorModeButton') {
        $('#codeEditor').hide();
        $('#guiEditor').show();
    } else {
        $('#codeEditor').show();
        $('#guiEditor').hide();
    }
}

function initialize() {
    $("#loadButton").button().click(load);
    $("#newButton").button().click(startNew);
    $("#saveButton").button().click(save);
    $('#logoutButton').click(logout);
    $('#loginButton').click(login);
    $('#editor').keyup(checkKey);
    $('#modeSelector').buttonset();
    $('#modeSelector input[type=radio]').change(checkMode);
    checkUser();
}