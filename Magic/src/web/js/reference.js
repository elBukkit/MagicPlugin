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

function makeSelectable(tab, details, populate, depth) {
    tab.selectable({
        selected: function(event, ui) {
            var selected = jQuery(".ui-selected", this);
            details.empty();
            populate(details, selected);
            _selectedDetails.length = depth;
            _selectedDetails[depth - 1] = selected.data('key');
            var hash = _selectedTab;
            for (var i = 0; i < _selectedDetails.length; i++) {
                hash += "." + _selectedDetails[i];
            }
            window.location.hash = hash;
        }
    });

    var currentHash = window.location.hash;
    if (currentHash != '') {
        var pieces = currentHash.split('.');
        if (pieces.length >= depth) {
            var key = pieces[depth];
            jQuery(tab).find('.ui-selectee').each(function() {
                var selected = $(this);
                if (selected.data('key') == key) {
                    selected.addClass('ui-selected');
                    details.empty();
                    populate(details, selected);
                }
            });
        }
    }
}

function populatePropertyList(list, sectionKey) {
    var properties = metadata.properties;
    var defaultValues = metadata[sectionKey];
    var section = Object.keys(defaultValues).sort();
    for (var i = 0; i < section.length; i++) {
        var key = section[i];
        if (!properties.hasOwnProperty(key)) continue;
        var parameter = properties[key];
        var item = $('<li>').text(parameter.name);
        item.addClass('ui-widget-content');
        item.data('key', key);
        item.data('default', defaultValues[key]);
        list.append(item);
    }
    sortList(list);
}

function makePropertySelector(selector, section, details) {
     populatePropertyList(selector, section);
     makeSelectable(selector, details, addParameterDetails, 1);
}

function addParameterDetails(container, listItem) {
    var key = listItem.data('key');
    var defaultValue = listItem.data('default');
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

    if (defaultValue != null && defaultValue != '') {
        var defaultValueContainer = $('<div class="defaultValue"/>');
        defaultValueContainer.text("Default: " + defaultValue);
        container.append(defaultValueContainer);
    }

    if (property.category != '') {
        var category = metadata.categories[property.category];
        var categoryName = $('<div class="category"/>')
            .append($('<span class="prefix">').text('Category: '))
            .append($('<span>').text(category.name));
        container.append(categoryName);
    }

    var propertyType = metadata.types[property.type];
    var typeDescription = $('<div class="propertyType"/>')
        .append($('<span class="prefix">').text('Type: '))
        .append($('<span>').text(propertyType.name));
    container.append(typeDescription);

    var propertyTypeDetails = $('<div class="propertyTypeDetails"/>');
    var propertyTypeDescription = $('<div class="propertyTypeDescription"/>');
    for (var i = 0; i < propertyType.description.length; i++) {
        propertyTypeDescription.append($('<div class="descriptionLine"/>').html(propertyType.description[i]));
    }
    propertyTypeDetails.append(propertyTypeDescription);

    populateOptions(propertyType.options, propertyTypeDetails);
    if (propertyType.hasOwnProperty('key_type')) {
        populateOptions(metadata.types[propertyType.key_type].options, propertyTypeDetails, 'Map of ' + metadata.types[propertyType.key_type].name + ':');
        populateOptions(metadata.types[propertyType.value_type].options, propertyTypeDetails, 'to ' + metadata.types[propertyType.value_type].name + ':');
    } else if (propertyType.hasOwnProperty('value_type')) {
        populateOptions(metadata.types[propertyType.value_type].options, propertyTypeDetails, 'List of ' + metadata.types[propertyType.value_type].name + + ':');
    }
    container.append(propertyTypeDetails);

    typeDescription.click(function() {
        propertyTypeDetails.toggle();
    });
}

function populateOptions(options, container, title) {
    var propertyTypeOptions = $('<div class="propertyTypeOptions"/>');
    var table = $('<table>');
    var tbody = $('<tbody>');
    table.append(tbody);
    var optionsCount = 0;
    for (var key in options) {
        if (!options.hasOwnProperty(key)) continue;

        var row = $('<tr>');
        var keyCell = $('<td>').text(key);
        var descriptionCell = $('<td>');
        if (options[key] != null) {
            descriptionCell.text(options[key]);
        }
        row.append(keyCell);
        row.append(descriptionCell);
        tbody.append(row);
        optionsCount++;
    }

    if (optionsCount == 0) return;

    if (title) {
        container.append($('<div>').addClass('optionsTitle').text(title));
    }

    propertyTypeOptions.append(table);
    container.append(propertyTypeOptions);
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
    sortList(list);
}

function makePropertyHolderSelector(selector, section, details, baseProperties) {
     populatePropertyHolderList(selector, section);
     makeSelectable(selector, details, function(details, listItem) {
         addPropertyHolderDetails(details, listItem.data('key'), section, baseProperties);
     }, 1);
}

function sortProperties(list) {
    list.sort(function(a, b) {
        var aName = metadata.properties.hasOwnProperty(a) ? metadata.properties[a].name : a;
        var bName = metadata.properties.hasOwnProperty(b) ? metadata.properties[b].name : b;
        return a.localeCompare(b);
    });
}

function sortList(ul) {
    var listitems = ul.children('li').get();
    listitems.sort(function(a, b) {
       return $(a).text().toUpperCase().localeCompare($(b).text().toUpperCase());
    });
    $.each(listitems, function(idx, itm) { ul.append(itm); });
}

function addPropertyHolderDetails(container, key, section, baseProperties) {
    var propertyHolder = metadata[section][key];

    var title = $('<div class="titleBanner"/>').text(propertyHolder.name);
    container.append(title);

    var propertyKey = $('<div class="propertyKeys"/>').text('class: ' + propertyHolder.short_class);
    container.append(propertyKey);

    var description = $('<div class="propertyTypeDescription"/>');
    for (var i = 0; i < propertyHolder.description.length; i++) {
        description.append($('<div class="descriptionLine"/>').html(propertyHolder.description[i]));
    }
    container.append(description);

    if (propertyHolder.category != '') {
        var category = metadata.categories[propertyHolder.category];
        var categoryName = $('<div class="category"/>')
            .append($('<span class="prefix">').text('Category: '))
            .append($('<span>').text(category.name));
        container.append(categoryName);
    }

    if (propertyHolder.examples.length > 0) {
        var exampleDiv = $('<div/>');
        exampleDiv.addClass('exampleContainer');
        exampleDiv.append("Examples: ");
        for (var i = 0; i < propertyHolder.examples.length; i++) {
            var example = propertyHolder.examples[i];
            var exampleLink = $('<a target="_blank">');
            exampleLink.prop('href', 'https://github.com/elBukkit/MagicPlugin/blob/master/Magic/src/main/resources/defaults/spells/' + example + '.yml');
            exampleLink.text(example);
            exampleDiv.append(exampleLink);
        }
        container.append(exampleDiv);
    }

    var parameterContainer = $('<div class="parameterContainer">');
    var parameterListContainer = $('<div class="parameterList">');
    var parameterList = $('<ul>');
    var properties = metadata.properties;

    var defaultValues = propertyHolder.parameters;
    var parameters = Object.keys(defaultValues);
    sortProperties(parameters);
    for (var i = 0; i < parameters.length; i++) {
        var propertyKey = parameters[i];
        if (!properties.hasOwnProperty(propertyKey)) continue;
        var property = properties[propertyKey];

        var parameterItem = $('<li>').text(property.name);
        parameterItem.addClass('ui-widget-content');
        parameterItem.data('key', propertyKey);
        parameterItem.data('default', defaultValues[propertyKey]);
        parameterList.append(parameterItem);
    }
    baseProperties = metadata[baseProperties];
    var baseParameters = Object.keys(baseProperties);
    sortProperties(baseParameters);
    for (var i = 0; i < baseParameters.length; i++) {
        var propertyKey = baseParameters[i];
        if (!properties.hasOwnProperty(propertyKey)) continue;
        var property = properties[propertyKey];

        var parameterItem = $('<li class="baseProperty">').text(property.name);
        parameterItem.addClass('ui-widget-content');
        parameterItem.data('key', propertyKey);
        parameterItem.data('default', defaultValues.hasOwnProperty(propertyKey) ? defaultValues[propertyKey] : baseProperties[propertyKey]);
        parameterList.append(parameterItem);
    }
    var parameterDetails = jQuery('<div class="details">').text("Select a parameter for details");
    makeSelectable(parameterList, parameterDetails, addParameterDetails, 2);
    parameterListContainer.append(parameterList);
    parameterContainer.append(parameterListContainer);
    parameterContainer.append(parameterDetails);
    container.append(parameterContainer);
}

var _selectedDetails = [];
var _selectedTab = "spell_properties";
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

        // Kinda hacky but not sure how to work around this
        var currentHash = window.location.hash;
        if (currentHash != '') {
            _selectedTab = currentHash.split('.')[0];
            window.location.hash = _selectedTab;
        }
        // Create tab list
        $("#tabs").tabs({
            beforeActivate: function (event, ui) {
                _selectedTab = ui.newPanel.selector;
                window.location.hash = _selectedTab;
            }
        }).show();
        window.location.hash = currentHash;
    });
}