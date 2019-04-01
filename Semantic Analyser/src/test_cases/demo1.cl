class Main {
	main():IO {
		new IO.out_string("Hello This is second input file!\n")
	};
};

class List {
   x : Int;
   isFalse() : Bool { false };
 
};

class A2I {

     	 a2i(s : String) : Object {
		 (let int : Int <- 0 in	
           {	
               (let j : Int <- s.length() in
	          (let i : Int <- 0 in
		    while i < j loop
			{
			    int <- int * 10;
			    i <- i + 1;
			    
			}
		    pool
		  )
	       );
            
	    }
        )
     };

};
