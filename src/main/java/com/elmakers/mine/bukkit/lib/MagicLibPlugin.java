package com.elmakers.mine.bukkit.lib;

import org.bukkit.plugin.java.JavaPlugin;


/*! \mainpage Magic Bukkit Utility Library
*
* \section intro_sec Introduction
*
* This is a common library used by elMakers plugins such as Magic and MagicWorlds.
* 
* This library relies on the Magic API, which is a lightweight set of common interfaces:
* 
* http://jenkins.elmakers.com/job/MagicAPI/doxygen/
* 
* If you wish to extend the Magic plugin, such as by adding a completely new Spell
* or EffectPlayer class, you will need to build against MagicPlugin directly:
* 
* http://jenkins.elmakers.com/job/MagicPlugin/doxygen/
* 
* \section issues_sec Issues
* 
* For issues with this Library, or suggestions, use our Issue Tracker:
* 
* http://jira.elmakers.com/browse/LIB/
* 
* \section start_sec Getting Started
* 
* If you haven't done so already, get started with Bukkit by getting a basic
* shell of a plugin working. You should at least have a working Plugin that
* loads in Bukkit (add a debug print to onEnable to be sure!) before you
* start trying to integrate with other Plugins. See here for general help:
* 
* http://wiki.bukkit.org/Plugin_Tutorial
* 
* \section maven_sec Building with Maven
* 
* Once you have a project set up, it is easy to build against the Magic API and Magic Lib
* with Maven. Simply add the elmakers repository to your repository list,
* and then add a dependency for MagicAPI and MagicLib. A typical setup would look like:
* 
* <pre>
* &lt;dependencies&gt;
* &lt;dependency&gt;
* 	&lt;groupId&gt;org.bukkit&lt;/groupId&gt;
* 	&lt;artifactId&gt;bukkit&lt;/artifactId&gt;
* 	&lt;version&gt;1.6.4-R2.0&lt;/version&gt;
* 	&lt;scope&gt;provided&lt;/scope&gt;
* &lt;/dependency&gt;
* &lt;dependency&gt;
* 	&lt;groupId&gt;com.elmakers.mine.bukkit&lt;/groupId&gt;
* 	&lt;artifactId&gt;MagicAPI&lt;/artifactId&gt;
* 	&lt;version&gt;1.2&lt;/version&gt;
* 	&lt;scope&gt;provided&lt;/scope&gt;
* &lt;/dependency&gt;
* &lt;dependency&gt;
* 	&lt;groupId&gt;com.elmakers.mine.bukkit&lt;/groupId&gt;
* 	&lt;artifactId&gt;MagicLib&lt;/artifactId&gt;
* 	&lt;version&gt;1.0&lt;/version&gt;
* 	&lt;scope&gt;provided&lt;/scope&gt;
* &lt;/dependency&gt;
* &lt;/dependencies&gt;
* &lt;repositories&gt;
* &lt;repository&gt;
*     &lt;id&gt;bukkit-repo&lt;/id&gt;
*     &lt;url&gt;http://repo.bukkit.org/content/groups/public/ &lt;/url&gt;
* &lt;/repository&gt;
* &lt;repository&gt;
*     &lt;id&gt;elmakers-repo&lt;/id&gt;
*     &lt;url&gt;http://maven.elmakers.com/repository/ &lt;/url&gt;
* &lt;/repository&gt;
* &lt;/repositories&gt;
* </pre>
* 
* \section shading Shading and Depending
* 
* You have a few options for how to use this lib:
* 
* - Shade both the API and the Lib into your plugin. Do this if you don't plan to interact with the Magic plugin. TODO: Provide Examples.
* - Mark both the API and Lib as "provided". Do this if you plan on hard-depending on Magic, or would like to soft-depend with
*   the option to use this library as a stand-alone lib provider.
*/

/**
 * This class is here so that this lib may be loaded as a plugin, to provide the implementation to other
 * plugins that use it.
 * 
 * Note that MagicPlugin is also a direct provider of this library, so the two should not be used in conjunction.
 * This library only exists as a stand-alone plugin in the event you want to use a plugin that uses MagicLib, but
 * you don't actually want to use Magic.
 * 
 */
public class MagicLibPlugin extends JavaPlugin {

}
