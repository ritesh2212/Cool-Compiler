class Main {
x : Int;
y : Int;
z : Bool;
main() : Int { {
x <- new IO@IO.in_int();
y <- new IO@IO.in_int();
z <- x<y;
new IO@IO.out_int(z);
10;
} };
};
