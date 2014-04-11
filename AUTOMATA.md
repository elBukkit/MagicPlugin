# Automata

Automata are an experimental new aspect to Magic that can bring an interesting new variety of gameplay to a server.

*Warning* : Automota are in their early experimental stages, and not to be used in a world you don't want messed up!

TODO: Add more documentation!

## Glider

The first automaton, a dumb drone that always travels SE, following the 2D Life rules. 

Note that Glider is a bit different, it was the first experimental one, 
and the actual spell is just loading an obsidian glider schematic with the automaton "brain" embedded in.

The spells that follow will instead create an Automaton out of whatever you aim at- 
this may have mixed results, depending on what you cast it on, 
how many blocks of that type are around it, 
what pattern they're in, etc. 
It may be hard to get one going on natural terrain (casting blob a few times works though).

Do note that whatever material you cast the spell on (block you're pointing at), 
the automaton has free reign to destroy. It is otherwise normally non-destructive, 
in that you can clear away anything it leaves behind and everything is intact underneath.

## Virus

An unchecked experiment in 3D Life, is now on version 8.0 - 
it will track down nearby mobs, focusing on players and other Automaton- 
it leaves a huge swath of mess in its wake (not destruction, per se, it just encases things up)

## Hunter

A smaller, faster, more compact (about 5x5 blocks) version of the Virus, 
tracks down players and suffocates them. 
(The Virus is actually normally disperse enough that it does not kill you... a weakness!)

## Defender

A slightly larger and more disperse version of Hunter that tracks down nearby Automata and disables them.