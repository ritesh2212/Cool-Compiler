class Main inherits B{
	main():IO {
		new IO.out_string("Hello world!\n")
	};
};

class A inherits Main{
	foo():Int{
		new IO.out_string("Hello world!\n")
	};
};

class B inherits A{
	boo():Int{
		new IO.out_string("Hello world!\n")
	};
};
