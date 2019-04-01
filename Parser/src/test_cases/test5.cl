class A {
  f(x:Int) : Object {{ 
    x=5+3;
    x=;     --Error because nothing is on right side of x
    x=5     --Error because there is no semicolon at the end of the line to terminate the line 
    x=5+;   --Error because there is no 2nd argument for the binary equation
   }};
};
