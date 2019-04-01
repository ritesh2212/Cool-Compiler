class Int1{
	
	a : Int<-1;
	main1(a:Int):IO {
		new IO.out_string("Hello world!\n")
	};
};
class Main  {
		as : IO;
	main():IO {
		--new IO.out_string("Hello world!\n")
		as.out_string("Hello world!\n")
	};
};

class B inherits Main{
	b: Int; d:Bool; e:String; f : Int1; g : Int1 <- new Int1;h:IO;
	main2():Object {
		--new IO.out_string("Hello world!\n")
		f.main1(b)
		
		--while 10 <= 100 loop 10 pool
		
		--b <- 10
--new IO.out_string("Hello world!\n")
	};
	(* main(i : Int):IO {
		new IO.out_string("Hello world!\n")
	}; *)
};
