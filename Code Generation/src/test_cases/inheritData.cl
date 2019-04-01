class Silly {
   a:Int;
   i_copy() : Int { a<-10 };
};

class Sally inherits Silly { 
   b : Int;
};

class Main {
x : Int;
main() : Object { {
x <- new Sally@Silly.i_copy();
new IO@IO.out_int(x);
} };
};
