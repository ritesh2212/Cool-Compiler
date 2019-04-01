-- Loop 
class Main {
	 
	main():IO {
		new IO.out_string("Hello world!\n")
		
	};
};

class B inherits Main{
i : Int; j : Int;
	foo():Object{
		while i < j loop
		{
			    new IO.out_string("Hello world!\n");
			    i <- i + 1;
		}
		pool
		
	};
};
