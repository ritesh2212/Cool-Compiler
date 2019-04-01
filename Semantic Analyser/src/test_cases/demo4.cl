--Comparator 
class Main {
	main():IO {
		new IO.out_string("Hello world!\n")
	};
};

class A {
   foo(a:A):Bool { {6 < 7; 6 <= 7; not true;} };

};


class B inherits Main {
x :Int;
foo():Object{
	x<- ~ 10
};
};
class C inherits B {
a:A;
b:B;

compare():Bool {
a=b
};
};
class D{

};
