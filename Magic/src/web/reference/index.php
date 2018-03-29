<?php
require_once('../config.inc.php');
?>
<html>
<head>
    <title><?= $title ?> Reference</title>
    <link rel="shortcut icon" type="image/x-icon" href="../favicon.ico">
    <link rel="stylesheet" href="../css/smoothness/jquery-ui-1.10.3.custom.min.css"/>
    <link rel="stylesheet" href="../css/common.css" />
    <link rel="stylesheet" href="../css/loading.css" />
    <link rel="stylesheet" href="../css/reference.css"/>
    <script src="../js/jquery-1.10.2.min.js"></script>
    <script src="../js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="../js/reference.js"></script>
    <?php if ($analytics) echo $analytics; ?>
</head>
<body>
<div id="tabs" style="display:none">
    <ul id="tablist">
        <li><a href="#spellProperties">Spell Properties</a></li>
        <li><a href="#spellParameters">Spell Parameters</a></li>
        <li><a href="#actions">Actions</a></li>
        <li><a href="#effects">Effects</a></li>
        <li><a href="#effectlib">EffectLib</a></li>
        <li><a href="#wands">Wands</a></li>
        <li><a href="#mobs">Mobs</a></li>
    </ul>
    <div id="spellProperties" class="flextab">
        <div class="flexContainer">
            <div class="scrollingTab">
                <ol id="spellPropertyList">
                </ol>
            </div>
            <div class="details" id="spellPropertyDetails">
                These are the top-level configuration options for spells.
                <br/><br/>
                They cannot be overridded by wands or /cast parameters, and are generally considered immutable properties
                of a spell.
                <br/><br/>
                Some of these properties, such as "icon", are required for a spell to work properly in a wand.
                <br/><br/>
                Select a property for details.
            </div>
        </div>
    </div>
    <div id="spellParameters" class="flextab">
        <div class="flexContainer">
            <div class="scrollingTab">
                <ol id="spellParameterList">
                </ol>
            </div>
            <div class="details" id="spellParameterDetails">
                Spell parameters go in the "parameters" section of a spell's configuration.
                <br/><br/>
                These are general parameters that affect the basic workings of any spells.
                <br/><br>
                These can be modified directly in-game using the cast command, for instance:
                <br/><br>
                <span class="code">/cast blob undo 30000</span>
                <br/><br>
                They can also be modified by a wand with overrides on it, such as:
                <br/><br>
                <span class="code">/wand override blob.undo 0</span>
                <br/><br>
                Wand overrides can also be made to apply to all spells cast by that wand:
                <br/><br>
                <span class="code">/wand override undo 0</span>
                <br/><br>
                Select a parameter for details.
            </div>
        </div>
    </div>
    <div id="actions" class="flextab">
        <div class="flexContainer">
            <div class="scrollingTab">
                <ol id="actionList">
                </ol>
            </div>
            <div class="details" id="actionDetails">
                Actions are the building blocks of a spell's logic. Without actions, a spell does nothing except create
                particle effects or sounds.
                <br/></br/>
                Action parameters can go in the "parameters" section of a spell's configuration, alongside base spell
                parameters.<br/><br/>
                Action parameters in the "parameters" section can be overridden as normal by /cast commands or wand
                overrides, but actions placed directly in the actions list can not.
                <br/><br/>
                In general it is good practice to put all parameters in the "parameters" section for easy reading, but there
                are cases where it is necessary to put them in the actions list. Generally this would be because you have
                two of the same actions in the logic that you want to use different parameters.
                <br/><br/>
                They can also go directly underneath the corresponding action in the "actions" list.<br/><br/>
                Select an actions for details.
            </div>
        </div>
    </div>
    <div id="effects" class="flextab">
        <div class="flexContainer">
            <div class="scrollingTab">
                <ol id="effectParameterList">
                </ol>
            </div>
            <div class="details" id="effectParameterDetails">
                Effects are what make spells look and feel awesome. These are generally a combination of particle effects
                and sounds, though fireworks and base Minecraft effects (e.g. HURT) can also be used.
                <br/><br/>
                Here are the basic parameters that can be applied to top-level effects sections. These can be used for spawning
                individual particles or sounds.
                <br/><br/>
                For more complex effects, add an "effectlib" section and see the EffectLib tab for options.
                <br/><br/>
                Select a parameter for details.
            </div>
        </div>
    </div>
    <div id="effectlib" class="flextab">
        <div class="flexContainer">
            <div class="scrollingTab">
                <ol id="effectList">
                </ol>
            </div>
            <div class="details" id="effectDetails">
                EffectLib is integrated into Magic for complex special effects.
                <br/><br/>
                Simply add an "effectlib" section to any effect to create an EffectLib effect.
                <br/><br/>
                The only required parameter is "class", which will determine which effect is used.
                <br/><br/>
                Most affects have tweakable parameters, however, which will give your effects an extra customized look.
                <br/><br/>
                Use <span class="code">/cast fxdemo</span> in-game for a demo of all the builtin EffectLib effects.
                <br/><br/>
                Select an effect for details.
            </div>
        </div>
    </div>
    <div id="wands">
        <div class="flexContainer">
            <div class="scrollingTab">
                <ol id="wandParameterList">
                </ol>
            </div>
            <div class="details" id="wandParameterDetails">
                Wands in Magic are special items that can be used for casting spells, and may also grant the holder
                special effects or buffs.
                <br/><br/>
                Wands don't necessarily need to look or act like wands, they can take the form of armor, bows, swords or
                any other item.
                <br/><br/>
                Use the <span class="code">/wand configure</span> command in-game to directly modify properties of a wand
                item. Properties can also be added to wand template configurations in wands.yml, to make new wands that can
                be spawned in-game using the <span class="code">/mgive</span> command.
                <br/><br/>
                Select a wand property for details.
            </div>
        </div>
    </div>
    <div id="mobs">
        <div class="flexContainer">
            <div class="scrollingTab">
                <ol id="mobParameterList">
                </ol>
            </div>
            <div class="details" id="mobParameterDetails">
                Magic has a basic custom mob system for creating mobs that can cast spells or have other magical properties.
                <br/><br/>
                Mobs can be added to mobs.yml, and spawned in game using <span class="code">/mmob spawn</span>.
                <br/><br/>
                Select a mob property for details.
            </div>
        </div>
    </div>
</div>

<!-- Loading Indicator -->
<div class="modal"></div>
</body>
</html>
