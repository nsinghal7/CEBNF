# CEBNF
A general compiler that converts code to postfix notation given its grammatical structure document.
By Nikhil Singhal

  This program is a compiler that can convert code from strictly defined languages into postfix notation so that it can be easily run or simplified. It allows a user to specify a grammar file in "CEBNF" notation, a modified version of Extended Backus Nauer Form (EBNF) that also includes instructions about its postfix notation, and a file containing code. It then parses the code using the grammar file and outputs the postfix representation.

Directions for sample program:
  To run a sample program, add the entire CEBNF5 folder as a project in Eclipse or compile the src folder yourself. BitInterpreter.java contains the main method that will use the general compiler with the grammatical structure for BIT (http://www.dangermouse.net/esoteric/bit.html) stored in the file named BIT to parse and run the program in the file BITCODE, which takes in two bits (either "ONE" or "ZERO") and outputs their sum.



To understand more about the specifications of the compiler inputs, continue reading below:

  
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

CODE_GROUPs can also contain REFERENCEs inside TEXT. REFERENCEs are of the form &name or #name where name is a lowercase term name. &name will paste the value saved as (name) into the TEXT, while #name will paste the number of children or number of loop passes in the outermost item of (name). #name is not yet functional, but its unavailability only limits the efficiency, not usefulness, of this system. To save values to (name), any OTHER can be followed by @name, in which case the OTHER's value and number of children/passes in the OTHER. Saving a value will work even if the ITEM is set to not print.


Running the compiler:
  To run the compiler, first create a new TokenGenerator with the empty constructor. Then use the registerRules(...) method to add regular expressions with names to the token generator. The names can be referenced in the grammar file through PREDEFINED_TERMs. Alternatively, predefined names can be inputted, and these names will automatically register predefined regular expressions (including INTEGER, DECIMAL, DIGIT, SQ_STRING, DQ_STRING, SQ_CHAR_STRING, DQ_CHAR_STRING, CHAR_STRING, LC_LETTER, UC_LETTER, LETTER, ANY_CHAR, PRINTING_CHAR, WHITE_SPACE, LINE_COMMENT, BLOCK_COMMENT). RegisterRules() also sets whether the given regular expression is ignored (thrown out after token matching) or otherwise if it is printed (outputted to postfix notation under standard circumstances). Various versions of registerRules() exist for ease of use. RegisterStandardRules() can be used to condense rule adding for similarly formatted rules. Rules are checked in the order they are added, and match and create tokens greedily. All characters that can legally appear in the code must be accounted for in tokens that match the inputted rules.
  Then, create a MetaParser with the TokenGenerator as input to the constructor and call

metaParser.parseGrammar(file)

  where file is the text of the grammar file. This fills the MetaParser's internal HashMap with a representation of the grammar file. Then run

metaParser.parseCode(code).toString()

  where code is the text of the code file to compile using the grammar. This returns a comma separated postfix notation string representing the code in the given file using the format specified in the grammar. This output can then be handled in the interpreter fairly mechanically by using a stack to produce the desired results.
