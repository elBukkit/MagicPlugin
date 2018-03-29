<?php
require_once('../config.inc.php');
?>
<html>
<head>
    <title><?= $title ?> Reference</title>
    <link rel="shortcut icon" type="image/x-icon" href="../favicon.ico">
    <link rel="stylesheet" href="../css/smoothness/jquery-ui-1.10.3.custom.min.css"/>
    <link rel="stylesheet" href="../css/magic.css"/>
    <link rel="stylesheet" href="../css/reference.css"/>
    <script src="../js/jquery-1.10.2.min.js"></script>
    <script src="../js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="../js/reference.js"></script>
    <?php if ($analytics) echo $analytics; ?>
</head>
<body>
<div id="tabs" style="display:none">
    <ul>
        <li><a href="#spellProperties">Spell Properties</a></li>
        <li><a href="#spellParameters">Spell Parameters</a></li>
        <li><a href="#actions">Actions</a></li>
        <li><a href="#effects">Effects</a></li>
        <li><a href="#effectlib">EffectLib</a></li>
        <li><a href="#wands">Wands</a></li>
        <li><a href="#mobs">Mobs</a></li>
    </ul>
    <div id="spellProperties">
        <div class="scrollingTab">
            <div class="navigation">
                <ol id="spellPropertyList">
                </ol>
            </div>
        </div>
        <div class="details" id="spellPropertyDetails">
            These are the top-level configuration options for spells.<br/><br/>
            They cannot be overridded by wands or /cast parameters, and are generally considered immutable properties
            of a spell.<br/><br/>
            Select a property for details.
        </div>
    </div>
    <div id="spellParameters">
        <div class="scrollingTab">
            <div class="navigation">
                <ol id="spellParameterList">
                </ol>
            </div>
        </div>
        <div class="details" id="spellParameterDetails">
            Spell parameters go in the "parameters" section of a spell's configuration.<br/><br/>
            These are general parameters that affect the basic workings of any spells.<br/><br>
            Select a parameter for details.
        </div>
    </div>
    <div id="actions">
        <div class="scrollingTab">
            <div class="navigation">
                <ol id="actionList">
                </ol>
            </div>
        </div>
        <div class="details" id="actionDetails">
            Action parameters can go in the "parameters" section of a spell's configuration, alongside base spell
            parameters.<br/><br/>
            They can also go directly underneath the corresponding action in the "actions" list.<br/><br/>
            Select an actions for details.
        </div>
    </div>
    <div id="effects">
        <div class="scrollingTab">
            <div class="navigation">
                <ol id="effectParameterList">
                </ol>
            </div>
        </div>
        <div class="details" id="effectParameterDetails">
            Select a parameter for details.
        </div>
    </div>
    <div id="effectlib">
        <div class="scrollingTab">
            <div class="navigation">
                <ol id="effectList">
                </ol>
            </div>
        </div>
        <div class="details" id="effectDetails">
            Select an effect for details.
        </div>
    </div>
    <div id="wands">
        <div class="scrollingTab">
            <div class="navigation">
                <ol id="wandParameterList">
                </ol>
            </div>
        </div>
        <div class="details" id="wandParameterDetails">
            Select a parameter for details.
        </div>
    </div>
    <div id="mobs">
        <div class="scrollingTab">
            <div class="navigation">
                <ol id="mobParameterList">
                </ol>
            </div>
        </div>
        <div class="details" id="mobParameterDetails">
            Select a parameter for details.
        </div>
    </div>
</div>

<!-- Loading Indicator -->
<div class="modal"></div>
</body>
</html>
