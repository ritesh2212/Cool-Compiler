--Let expression
class A inherits Main {
  
   	 a(i : Int) : Bool {{
	 let x:Int <- 5 in x;
	 true;
    }};
	
	foo():Int { {  "hi"; 1; } };

};
class Main { main () : Int { 6 }; };

