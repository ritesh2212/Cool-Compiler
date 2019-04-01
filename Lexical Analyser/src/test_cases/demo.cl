-- perfect program

class B {
	s : String <- "Hello";
	g (y:String) : Int {
		y.concat(s)
	};
	f (x:Int) : Int {
		x+1
	};
};
class A inherits B {
	a : Int;
	b : B <- new B;
	f(x:Int) : Int {
	x+a
	};
}
class Main {
	b : B;
	main():IO {
		b <- (new B).g("world");
		--new IO.out_string("Hello \0 world!\n")
		
	};
};
