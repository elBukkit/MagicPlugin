region flag __global__ -w world blocked-spell-categories automata
region flag welcome_mobs -w world server-enter-command /castp @p recall unlock town
region flag capital -w world server-enter-command /castp @p recall unlock town
region flag lodge -w world server-enter-command /castp @p recall unlock lodge
region flag battle -w world server-enter-command /castp @p recall unlock battle
region flag engineering -w world server-enter-command /castp @p recall unlock engineering
region flag other -w world_other server-enter-command /castp @p recall unlock other
region flag bastion -w world_other server-enter-command /castp @p recall unlock bastion
region flag magic_spawn -w world server-enter-command /castp @p recall unlock black
region flag magic_spawn2 -w world server-enter-command /castp @p recall unlock white
region flag magic_spawn3 -w world server-enter-command /castp @p recall unlock blue
region flag magic_spawn5 -w world server-enter-command /castp @p recall unlock iron
region flag magic_spawn4 -w world server-enter-command /castp @p recall unlock frozen
region flag magic_spawn6 -w world server-enter-command /castp @p recall unlock brick
region flag refugee -w world server-enter-command /castp @p recall unlock refugee
region flag redwood -w world server-enter-command /castp @p recall unlock redwood
region flag gold -w world server-enter-command /castp @p recall unlock gold
region flag hall -w world server-enter-command /castp @p recall unlock hall
region flag seaside -w world server-enter-command /castp @p recall unlock seaside
region flag wolfcastle -w world server-enter-command /castp @p recall unlock wolfcastle
region flag spleef -w world server-enter-command /castp @p recall unlock spleef
region flag dueling -w world server-enter-command /castp @p recall unlock dueling
region flag end_temple -w world server-enter-command /castp @p recall unlock endtemple
region flag welcome -w world allowed-spells missile,recall,blast
region flag arena -w world blocked-spell-categories master,engineering
region flag arena -w world allowed-spells * blocked-spell-categories master,engineering
region flag dueling -w world allowed-spells *
region flag dueling -w world blocked-spell-categories master,engineering
region flag spleef -w world allowed-spells recall,day
region flag spleef -w world blocked-spells *
region flag spleef_arena -w world allowed-spells recall,blast,collapse,harden,laser,earthquake
region flag refugees -w world allowed-spells *

region flag dueling -w world  keep-inventory allow
region flag dueling -w world  keep-level allow
region flag arena -w world  keep-inventory allow
region flag arena -w world  keep-level allow
region flag spleef -w world  keep-inventory allow
region flag spleef -w world  keep-level allow
region flag refugees -w world  keep-inventory allow
region flag refugees -w world  keep-level allow
