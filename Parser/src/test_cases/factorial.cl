
class Main{
  main(): Object {{
    new IO.out_string("Enter the integer: ");

    let input: Int <- in_int() in                       -- this line take the input to varibale input
      if input < 0 then
        new IO.out_string("ERROR: You have entered the integer less than 0, please enter again\n")
      else {
        new IO.out_string("The factorial of integer ").out_int(input);
        new IO.out_string(" is : ").out_int(factorial(input));
        new IO.out_string("\n");
      }
      fi;
  }};
  
  
(*
    the below function recursively calculate the factorial of 
    number.    
*)
  factorial(num: Int): Int {                            
    if num = 0 then 1 else num * factorial(num - 1) fi  --this condition returns 1 if number reaches to zero else recursively calls the fun.
  };
};
