class A {
  fb(x:Int):Int {       --No error will occur in this line as there is no syntactical error in this line
    self
  };
};

class B {
  ASSIGN : Int;         --Error will occur here as Identifier name cannot start with capital letter.
};
