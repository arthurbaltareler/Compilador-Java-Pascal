program fatorialFuncao;
var
  x: integer;

function fatorial(n: integer): integer;
var
  resultado, i: integer;
begin
  resultado := 1;
  i := 1;
  while i <= n do
  begin
    resultado := resultado * i;
    i := i + 1;
  end
  fatorial := resultado;
end;

begin
  x := fatorial(5);
  writeln(x);
end.