# CEBNF
A general compiler that converts code to postfix notation given its grammatical structure document.
By Nikhil Singhal

  This program is a compiler that can convert code from strictly defined languages into postfix notation so that it can be easily run or simplified. It allows a user to specify a grammar file in "CEBNF" notation, a modified version of Extended Bacchus Nauer Form (EBNF) that also includes instructions about its postfix notation, and a file containing code. It then parses the code using the grammar file and outputs the postfix representation.

Directions for sample program:
  To run a sample program, add the entire CEBNF5 folder as a project in Eclipse or compile the src folder yourself. BitInterpreter.java contains the main method that will automatically interpret and run a program to add 2 inputted bits in the language BIT (http://www.dangermouse.net/esoteric/bit.html).
  
Grammar file:
  The grammar file must be in CEBNF notation. Each line defines a single term that will match some part of the input file, and is delimited by a semicolon. Java-style comments and whitespace are ignored. The grammar file must contain a line defining a term called INPUT, which will be used to match the entire input file. Lines are constructed as follows:

TERM_NAME = ITEM, ITEM, ... ITEM;

where TERM_NAME is made up of capital letters and underscores, and ITEM represents something that must be matched in the input file in order for the term to match. An ITEM can be an OR or an OTHER. ORs are defined as follows:

OTHER | OTHER | ... OTHER

while OTHERs are can be GROUPs, CONSTANTs (single quote strings), TERM_NAMEs (which match all the items that that term is defined by), PREDEFINED_TERMs (which are lowercase term names that match regular expressions) or CODE_GROUPS.
GROUPs can be any of the following:
A single pass group must be matched exactly once: (ITEM, ITEM, ... ITEM)
An optional group can be matched once or not at all: [ITEM, ITEM, ... ITEM]
A zero-plus group can be matched zero or more times: <ITEM, ITEM, ... ITEM>
A one-plus group must be matched at least once: {ITEM, ITEM, ... ITEM}

All groups will match greedily (matching as much as possible without falsely matching). CODE_GROUPs give instructions for forming the postfix representation of the code. They are formatted as follows:

/TEXT/

where TEXT is a string to print in the postfix notation in the same place in its order as this group is. Any constants can also be configured to print to the post fix notation, not print to post fix notation, or be completely ignored (and thus not need to be considered in this grammar file).

CODE_GROUPs can also contain REFERENCEs inside TEXT. REFERENCEs are of the form &name or #name where name is a lowercase term name. &name will paste the value saved as (name) into the TEXT, while #name will paste the number of children or number of loop passes in the outermost item of (name). #name is not yet functional, but is not necessary for the use of this system. To save values to (name), any OTHER can be followed by @name, in which case the OTHER's value and number of children/passes in the OTHER.

// add part about token generator and actually running the metaparser
