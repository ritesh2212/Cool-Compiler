--Multiple method declaration
class Main {
	main():IO {
		new IO.out_string("Hello world!\n")
	};
	
};

class B inherits Main{
	foo():Int{
		1
	};
	foo():Int{
		1
	};
	main():IO {
		new IO.out_string("Hello world!\n")
	};
};
