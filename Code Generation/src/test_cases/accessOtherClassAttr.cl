class Silly {
   a:Int;
   i_copy() : Int { a<-10 };
};

class Sally inherits Silly { 
   b : Int;
   f():Object{
   new IO@IO.out_int(a)
   };
};

class Main {
x : Int;
main() : Object { {
x <- new Sally@Silly.i_copy();
new Sally@Sally.f();
new IO@IO.out_int(x);
} };
};
