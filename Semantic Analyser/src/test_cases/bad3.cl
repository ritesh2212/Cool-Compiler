-- Case error
class Main { main() : Int {0 }; };

class A inherits Main{
  foo(x:Int) : Object {case x of a:Int => a; b:Int => b; esac};
};
