class Main {
	x : Int;
	main():IO {
		new IO.out_string("Hello This is second input file!\n")
	};
};

class A inherits Main {
	c :Int;
	foo():Object {
		c <-x+1
	};
};

class B inherits A{
	b :Int;
	boo():Object {
		b <- x+1
	};
};

