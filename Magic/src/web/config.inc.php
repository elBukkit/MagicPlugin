<?php 

// Magic configuration file path
// You can set up a symlink for this, or handle it however you like
$magicRootFolder = dirname(__FILE__) . '/../main/resources';

// This is for the live editor, if you have a sandbox server set up the file path here.
$sandboxServer = '';
// And set the URL to your standbox server here to direct players to log in
$sandboxServerURL = '';

// This is mainly used in testing, normally doesn't need to be changed
$magicDefaultsFolder = $magicRootFolder . '/defaults';

// Resource Pack folder
$resourcePackFolder = $magicRootFolder . '../resource-pack/';

// Configure InfoBook integration (external plugin)
$infoBookRootConfig = dirname(__FILE__) . '/../main/resources/examples/InfoBook/config.yml';

// Page title
$title = "elMakers Magic Development Site";

// Instructional YouTube video id
$youTubeVideo = '8rjY8pjjPM8';

// How players get wands, other than view the configured ways in magic.yml (crafting, random chests)
$howToGetWands = array('You can purchase wands in an Essentials Shop', 'You can find wands on the ground using the dynmap');

// Page overview - this will get put in a Header at the top of the page.
$pageOverview = <<<EOT
	<div style="margin-left: 128px;">
		Welcome to the development server for the Magic plugin by elMakers!<br/><br/>
		This is a plugin for the <a href="http://www.bukkit.org" target="_new">Bukkit</a> minecraft server.
		For more information, <a href="http://dev.bukkit.org/bukkit-plugins/magic/" target="_new">click here.</a>
		<br/><br/>
		While this is just a development server, you are free to log in and play at
		<span class="minecraftServer">mine.elmakers.com</span>. You may also view our <a href="http://mine.elmakers.com:8080"/>dynmap here</a>, the world is a bit of a mess.
		<br/><br/>
		Thanks for looking!
	</div>
EOT;

$analytics = <<<EOT
<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-17131761-5', 'elmakers.com');
  ga('send', 'pageview');

</script>
EOT;

?>