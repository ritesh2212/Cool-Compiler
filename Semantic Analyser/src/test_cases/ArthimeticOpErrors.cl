class Int1{
	
	a : Int<-1;
	main1(a:Int):IO {
		new IO.out_string("Hello world!\n")
	};
};
class Main  {
		as : IO;aa : Int; bb : Bool; cc :Bool; string : String;
	main():Object {{
		as <- new Test;
		aa <- 10;
		bb <- 15;
		cc <- aa + bb;
		cc <- aa * bb;
		cc <- aa / bb;
		cc <- aa - bb;				
		as.out_string("Hello world!\n");
		 bb <- ~cc;
		not aa;
		string <- aa;
	}
	};
};

class B inherits Main{
	b: Int; d:Bool; e:String; f : Int1; g : Int1 <- new Int1;h:IO;
	main2():Object {
		{new IO.out_string("Hello world!\n");
		aa <-10; 
		while 10 <= 100 loop 10 pool;
		b <- 10;
new IO.out_string("Hello world!\n");
	}};
};
