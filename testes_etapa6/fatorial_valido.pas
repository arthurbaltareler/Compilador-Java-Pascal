program fatorial;
var
  n, resultado, i: integer;
begin
  n := 5;
  resultado := 1;
  i := 1;
  while i <= n do
  begin
    resultado := resultado * i;
    i := i + 1;
  end;
  writeln(resultado);
end.