// Initialization
$body = $("body");
$(document).on({
    ajaxStart: function() {
        $body.addClass("loading");
    },
    ajaxStop: function() {
        $body.removeClass("loading");
    }
});
$(document).ready(initialize);

var metadata = null;

function processMetadata(meta) {
    var properties = meta.properties;
    var filtered = {};

    for (var key in properties) {
        var property = properties[key];
        if (property.hasOwnProperty('alias')) {
            var aliased = properties[property.alias];
            if (aliased.hasOwnProperty('aliases')) {
                aliased.aliases.push(property)
            } else {
                aliased.aliases = [property];
            }
        } else {
            filtered[key] = property;
        }
    }
    meta.properties = filtered;
    return meta;
}

function makeSelectable(tab, details, populate) {
    tab.selectable({
        selected: function(event, ui) {
            var selected = jQuery(".ui-selected", this);
            var key = selected.data('key');
            details.empty();
            details.append(populate(key));
        }
    });
}

function populateList(list, sectionKey) {
    var properties = metadata.properties;
    var section = metadata[sectionKey];
    for (var i = 0;i < section.length; i++) {
        var key = section[i];
        if (!properties.hasOwnProperty(key)) continue;
        var parameter = properties[key];
        var item = $('<li>').text(parameter.name);
        item.addClass('ui-widget-content');
        item.data('key', key);
        list.append(item);
    }
}

function makePropertySelector(selector, section, details) {
     populateList(selector, section);
     makeSelectable(selector, details, getParameterDetails);
}

function getParameterDetails(key, details) {
    var property = metadata.properties[key];

    var container = $('<div>');
    var title = $('<div class="titleBanner"/>').text(property.name);
    container.append(title);

    var propertyType = metadata.types[property.type];
    var typeDescription = $('<div class="propertyType"/>').text(propertyType.name);
    container.append(typeDescription);

    var propertyTypeDetails = $('<div class="propertyTypeDetails"/>');
    var propertyTypeDescription = $('<div class="propertyTypeDescription"/>');
    for (var i = 0; i < propertyType.description.length; i++) {
        propertyTypeDescription.append($('<div class="descriptionLine"/>').html(propertyType.description[i]));
    }
    propertyTypeDetails.append(propertyTypeDescription);
    var propertyTypeOptions = $('<div class="propertyTypeOptions"/>');
    for (var i = 0; i < propertyType.options.length; i++) {
        propertyTypeOptions.append($('<div class="propertyTypeOption"/>').text(propertyType.options[i]));
    }
    propertyTypeDetails.append(propertyTypeOptions);

    container.append(propertyTypeDetails);

    typeDescription.click(function() {
        propertyTypeDetails.toggle();
    });

    return container;
}

function initialize() {
    $.ajax( {
        type: "GET",
        url: "../meta.json",
        dataType: 'json'
    }).done(function(meta) {
        // Populate tabs
        metadata = processMetadata(meta);
        var parameters = meta.parameters;

        makePropertySelector($("#spellPropertyList"), "spell_properties", $('#spellPropertyDetails'));
        makePropertySelector($("#spellParameterList"), "spell_parameters", $('#spellParameterDetails'));
        makePropertySelector($("#wandParameterList"), "wand_properties", $('#wandParameterDetails'));
        makePropertySelector($("#mobParameterList"), "mob_properties", $('#mobParameterDetails'));
        makePropertySelector($("#effectParameterList"), "effect_parameters", $('#effectParameterDetails'));

        // Create tab list
        $("#tabs").tabs().show();
    });
}