##!test1
if condition
  return true
else
  return false
end
##!test2
var = 5
return var
##!test3
return true
var = 5
var
##!test4
var = 5
var
##!test5
var = 2 ** 10
return var
##!test6
2 ** 10
##!test7
do_something
def some_method; end
(1..10).inject(1) { |i, j| i * j }
##!test8
var = 2 ** 10
return 5