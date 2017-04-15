## INF4230_TP3
Implémentation java du jeu, Scotland Yard qui est un jeu de société coopératif
Pour 3 à 6 joueurs, environ 30 minutes.

### Principe général

Un des joueurs est Mister X, gangster en fuite dans Londres. Les autres joueurs, incarnant des détectives de Scotland Yard, doivent le capturer en coordonnant leurs mouvements.

### Mise en place
Au début de la partie, chacun tire un jeton pour connaître son point de départ.

Mister X prend secrètement connaissance de son emplacement de départ. Il reçoit autant de black tickets qu'il y a de détectives, 4 tickets de taxi, 3 tickets de bus, 3 tickets de métro et les 2 cartes "coup double". Il prend également le tableau de parcours.

Chaque détective reçoit 10 tickets de taxi, 8 tickets de bus et 4 tickets de métro.

### But du jeu
Pour Mister X, le but est d'échapper aux détectives, l'objectif de ces derniers étant de le capturer avant qu'il n'effectue un 24e déplacement. Tous les joueurs se déplacent grâce à divers moyens de transport : taxi, bus et métro.

### Déroulement
La position de Mr. X n'est connue qu'à certains moments de la partie. Le reste du temps, les détectives ne connaissent que le moyen de transport (taxi, bus, métro) utilisé par Mr. X.

À tour de rôle, Mr. X puis les détectives déplacent leur pion :

- Mr. X, ne fait que noter sur une feuille ses déplacements (i.e. la case vers laquelle il se déplace) et cache cette destination par un ticket correspondant au moyen de transport utilisé.
- Les détectives donnent à Mr. X un ticket correspondant au moyen de transport utilisé pour effectuer leur déplacement qu'il effectue sur le plateau.
Après son troisième déplacement, puis tous les 5 déplacements, Mr. X doit révéler aux détectives la case sur laquelle il se trouve. Ceux-ci profitent de cette occasion pour affiner leur position et essayer d'encercler Mr. X. Comme entre chaque révélation de position de Mr. X, celui doit préciser quel moyen de transport il utilise, les détectives peuvent essayer de deviner où il se trouve à chaque instant.

Si pour un déplacement donné, Mr. X ne veut pas préciser quel moyen de transport il utilise, il place un ticket noir à la place du ticket de transport correspondant. Il peut également utiliser ce ticket noir pour se déplacer en bateau sur la Tamise qu'il est le seul à pouvoir emprunter.

Il peut également, lorsque vient son tour de se déplacer, utiliser une carte « coup double » pour effectuer deux déplacements en un tour. La carte « coup double » est alors remise à son voisin détective de droite qui pourra l'utiliser à son tour et qui la passera alors à nouveau à son voisin de droite, et ainsi de suite jusqu'à ce que la carte « coup double » soit jouée par le dernier détective.

### Fin de partie et vainqueur
La partie est gagnée par les détectives s'ils attrapent Mr. X avant son 24e mouvement, c’est-à-dire, si un détective se trouve à un moment de la partie sur la même case que Mr. X.

Mr. X, lui, gagne la partie s'il réussit à échapper aux détectives jusqu'à effectuer son 24e déplacement, car alors, les détectives n'ont plus de ticket pour se déplacer.
