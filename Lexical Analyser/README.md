# COOL Compiler #
COOL Compiler
Lexical Analyser

The Ultimate aim of this assignment is to write the Rules/Grammar of Lexical Analyser(tokenize the code) for Classroom Object Oriented Language (COOL) using Antlr, an automatic parser generator written in Java that we use to tokenize the COOL code according to given constraint that will be helpful for us, so that we understand how Lexical Analyser phase of Compiler works.

The code for the grammar is present in '*\lexer\src\grammar\CoolLexer.g4'

The grammar is written to extract the following structures in COOL: 
1. Integer and Bool constants 
2. Keywords and Identifiers 
3. Comments (inline, multi line, )
4. Whitespaces (end of line, tab)
5. Strings Constant

Integer and Bool:

According to cool manual Integer are from 0-9 and Bool is either true or false.


Keywords and Identifiers:

All Keywords must starts from Capital and Identifiers must starts with small letters.


Comments:

There are two type of comments: Inline and Multiple Lines.
Regular Expression for Inline comment are easy to taken care of, but in Multiple Lines comment we have to take care of certain constraint so that it will work accordingly. To handle Muliple lines comment we have two ways: Regex or stack in Antlr. I have taken Stack one as this is easier to handle. In this whenever opening '(*' happens we simply send it to stack, there we provided few option that will take care of EOF in comment or it is terminated properly.


Strings:

In String Constant we have to take care of multiple things like there is always escaped character whenever there is user hits enter from keyboard if it written deliberately by user then it is fine else it must throw exception. We also have to report EOF in  the string constant is EOF in the string before closing of the string quote. we also have to take care of string length that is it must be less that 1025 character and String contains escaped characters that needs to be taken care of as it may contain '\n','\t' etc. so it must be process by the processString function. 


Unmatched Character:

It must be detected whenever any token comes except any given tokens.
