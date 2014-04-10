As simpleDB only supports a small subset of SQL commands, the commands tested were not too complex.

Our program was tested using the provided StudentClient database and queries to ensure that no functionality of simpleDB was broken
by our own changes.

We created a similar database (CreatePkmnDB) consisting of two tables with a variety of values. The size of this database as a whole is slightly larger than that of the 
provided Student database. "Select" queries were run on this database as well, which can be run by running FlyingPkmn and NonAtkingMoves. In order to test updating records, 
the original Student database was used. In particular, the ChangeMajor class implements several update queries. The various queries run on both databases, as well as creating
tables to begin with, performs the extent of SQL allowed in simpleDB. All queries were tested before and after our modifications to simpleDB to ensure proper functionality.

The actual SQL commands are contained within the files. The output from each file is listed below.

CreatePkmnDB:
Table MOVES created.
MOVE records inserted.
Table SPECIES created.
SPECIES records inserted.

FlyingPkmn:
Flying Type Pokemon
Charizard
Pidgeot
Archeops
Tropius

NonAtkingMoves:
Non-Attacking Moves
Recover
Swords Dance

CreateStudentDB:
STUDENT records inserted.
Table DEPT created.
DEPT records inserted.
Table COURSE created.
COURSE records inserted.
Table SECTION created.
SECTION records inserted.
Table ENROLL created.
ENROLL records inserted.

FindMajors (various inputs):
Here are the math majors
Name	GradYear
amy	2004
sue	2005
kim	2001
pat	2001

Here are the drama majors
Name	GradYear
bob	2003
art	2004

StudentMajor:
Name	Major
joe	compsci
max	compsci
lee	compsci
amy	math
sue	math
kim	math
pat	math
bob	drama
art	drama

ChangeMajor:
Amy is now a drama major.

FindMajors (drama):
Here are the drama majors
Name	GradYear
amy	2004
bob	2003
art	2004
