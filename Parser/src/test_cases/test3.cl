class Hello {

   foo:Int <- 89;
   bar:Int  --Error because there is no semicolon to end the statement.
   
   foo(a:Int, b:Int, c:String): Int  {
      a <- A    --Error because assignment to 'a' is ('A')keyword which is not acceptable.
   };
   
   bar() :Int {
      6
   };
   
   
};
