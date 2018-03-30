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
                aliased.aliases.push(key)
            } else {
                aliased.aliases = [key];
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
            populate(details, key)
        }
    });
}

function populatePropertyList(list, sectionKey) {
    var properties = metadata.properties;
    var section = metadata[sectionKey];
    for (var i = 0; i < section.length; i++) {
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
     populatePropertyList(selector, section);
     makeSelectable(selector, details, addParameterDetails);
}

function addParameterDetails(container, key) {
    var property = metadata.properties[key];

    var title = $('<div class="titleBanner"/>').text(property.name);
    container.append(title);

    var propertyKey = $('<div class="propertyKeys"/>').text(property.field);
    if (property.hasOwnProperty("aliases")) {
        var aliases = $('<span class="propertyAlias">').text(', ' + property.aliases.join(', '));
        propertyKey.append(aliases);
    }
    container.append(propertyKey);

    var propertyDescription = $('<div class="propertyDescription"/>');
    for (var i = 0; i < property.description.length; i++) {
        propertyDescription.append($('<div class="descriptionLine"/>').html(property.description[i]));
    }
    container.append(propertyDescription);

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
}

function populatePropertyHolderList(list, sectionKey) {
    var section = metadata[sectionKey];
    for (var key in section) {
        if (!section.hasOwnProperty(key)) continue;
        var holder = section[key];
        var item = $('<li>').text(holder.name);
        item.addClass('ui-widget-content');
        item.data('key', key);
        list.append(item);
    }
}

function makePropertyHolderSelector(selector, section, details, baseProperties) {
     populatePropertyHolderList(selector, section);
     makeSelectable(selector, details, function(details, key) {
         addPropertyHolderDetails(details, key, section, baseProperties);
     });
}

function addPropertyHolderDetails(container, key, section, baseProperties) {
    var propertyHolder = metadata[section][key];

    var title = $('<div class="titleBanner"/>').text(propertyHolder.name);
    container.append(title);

    var propertyKey = $('<div class="propertyKeys"/>').text(propertyHolder.short_class);
    container.append(propertyKey);

    var description = $('<div class="propertyTypeDescription"/>');
    for (var i = 0; i < propertyHolder.description.length; i++) {
        description.append($('<div class="descriptionLine"/>').html(propertyHolder.description[i]));
    }
    container.append(description);

    if (propertyHolder.example != '') {
        var exampleLink = $('<a target="_blank">');
        exampleLink.prop('href', 'https://github.com/elBukkit/MagicPlugin/blob/master/Magic/src/main/resources/defaults/spells/' + propertyHolder.example + '.yml');
        exampleLink.text("View Example Configuration");
        var exampleDiv = $('<div/>');
        exampleDiv.append(exampleLink);
        container.append(exampleDiv);
    }

    var parameterContainer = $('<div class="parameterContainer">');
    var parameterListContainer = $('<div class="parameterList">');
    var parameterList = $('<ul>');
    var properties = metadata.properties;
    for (var i = 0; i < propertyHolder.parameters.length; i++) {
        var propertyKey = propertyHolder.parameters[i];
        if (!properties.hasOwnProperty(propertyKey)) continue;
        var property = properties[propertyKey];

        var parameterItem = $('<li>').text(property.name);
        parameterItem.addClass('ui-widget-content');
        parameterItem.data('key', propertyKey);
        parameterList.append(parameterItem);
    }
    baseProperties = metadata[baseProperties];
    for (var i = 0; i < baseProperties.length; i++) {
        var propertyKey = baseProperties[i];
        if (!properties.hasOwnProperty(propertyKey)) continue;
        var property = properties[propertyKey];

        var parameterItem = $('<li class="baseProperty">').text(property.name);
        parameterItem.addClass('ui-widget-content');
        parameterItem.data('key', propertyKey);
        parameterList.append(parameterItem);
    }
    var parameterDetails = jQuery('<div class="details">').text("Select a parameter for details");
    makeSelectable(parameterList, parameterDetails, addParameterDetails);
    parameterListContainer.append(parameterList);
    parameterContainer.append(parameterListContainer);
    parameterContainer.append(parameterDetails);
    container.append(parameterContainer);
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

        makePropertyHolderSelector($("#effectList"), "effectlib_effects", $('#effectDetails'), 'effectlib_parameters');
        makePropertyHolderSelector($("#actionList"), "actions", $('#actionDetails'), 'action_parameters');

        // Create tab list
        $("#tabs").tabs().show();
    });
}